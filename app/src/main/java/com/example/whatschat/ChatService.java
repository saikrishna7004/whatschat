package com.example.whatschat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatService extends Service {

    private static final String TAG = "ChatService";
    public static final String CHANNEL_ID = "ChatServiceChannel";
    public static final String CHANNEL_ID_SILENT = "ServiceRunning";
    private static boolean running;
    public static final String ACTION_REPLY = "com.example.whatschat.REPLY";

    private Socket socket;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        running = true;

        createNotificationChannel();

        // Create socket.io client and connect to server
        try {
            socket = SocketManager.getSocket();
            socket.connect();
            socket.on("receive", onReceive);
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to socket.io server: " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "ChatService OnStartCommand", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onStartCommand: Action is not null");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_SILENT)
                .setContentTitle("Chat Service")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        running = false;

        // Disconnect from socket.io server
        if (socket != null) {
            socket.off("receive", onReceive);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel msgChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            msgChannel.enableLights(true);
            msgChannel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(msgChannel);

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_SILENT,
                    "Service Running",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);

        }
    }

    private final Emitter.Listener onReceive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // Handle "receive" event
            JSONObject data = (JSONObject) args[0];
            String message, sender;
            try {
                message = data.getString("message");
                sender = data.getString("name");
            } catch (JSONException e) {
                return;
            }

            Log.i(TAG, "call: Msg vachindi roiii");

            RealmConfiguration config = new RealmConfiguration.Builder()
                    .allowWritesOnUiThread(true)
                    .build();
            Realm realm = Realm.getInstance(config);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatMessage chatMessage = realm.createObject(ChatMessage.class);
                    chatMessage.setMessage(message);
                    chatMessage.setSender(sender);
                    chatMessage.setTimestamp(System.currentTimeMillis());
                }
            });

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(ChatService.this, "vachindi vachindi", Toast.LENGTH_SHORT).show();
                }
            });

            Intent activityIntent = new Intent(ChatService.this, ChatWindow.class);
            PendingIntent contentIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                contentIntent = PendingIntent.getActivity(ChatService.this, 0, activityIntent, PendingIntent.FLAG_MUTABLE);
            }

            // Create a remote input for the reply action
            RemoteInput remoteInput = new RemoteInput.Builder(ACTION_REPLY).setLabel("Reply").build();
            Intent chatIntent = new Intent(ChatService.this, DirectReplyReceiver.class);
            PendingIntent chatPendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                chatPendingIntent = PendingIntent.getBroadcast(ChatService.this, 0, chatIntent, PendingIntent.FLAG_MUTABLE);
            }

            // Create a reply action with the remote input
            NotificationCompat.Action replyAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_send,
                            "Reply", chatPendingIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            // Show the notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Get existing notifications
            StatusBarNotification[] activeNotifications = manager.getActiveNotifications();

            int msgId = 0;

            // Build notification with previous messages
            String updatedText = message;
            for (StatusBarNotification mNotification : activeNotifications) {
                Notification currentNotification = mNotification.getNotification();
                int currentNotificationId = 2;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (currentNotification != null && currentNotification.getChannelId().equals(CHANNEL_ID)) {
                        currentNotificationId = currentNotification.extras.getInt(Notification.EXTRA_NOTIFICATION_ID);
                        if (currentNotificationId == msgId) {
                            // Combine the existing message with the new message
                            updatedText = mNotification.getNotification().extras.getString(Notification.EXTRA_BIG_TEXT) + "\n" + message;
                            Bundle extras = mNotification.getNotification().extras;
                            break;
                        }
                    }
                }
            }

            // Create the notification with the reply action
            Notification notification = new NotificationCompat.Builder(ChatService.this, CHANNEL_ID)
                    .setContentTitle("New message")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(updatedText))
                    .setContentIntent(contentIntent) // Set content intent here
                    .setAutoCancel(true)
                    .addAction(replyAction)
                    .build();

            manager.notify(msgId, notification);

        }
    };

    public static boolean isRunning() {
        return running;
    }

}