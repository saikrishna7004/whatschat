package com.example.whatschat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.socket.client.Socket;

public class DirectReplyReceiver extends BroadcastReceiver {

    String TAG = "DirectReplyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        Socket socket = null;
        try {
            socket = SocketManager.getSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "onReceive: Started DirectReplyReceiver");

        if(remoteInput != null){
            String replyMessage = remoteInput.getCharSequence(ChatService.ACTION_REPLY).toString();
            Log.i(TAG, "onReceive: "+replyMessage);
            JSONObject data = new JSONObject();
            try {
                data.put("message", replyMessage);
                data.put("roomId", "general");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("send", data);

            // Toast.makeText(context, "Reply sent: " + replyMessage, Toast.LENGTH_SHORT).show();

            RealmConfiguration config = new RealmConfiguration.Builder()
                    .allowWritesOnUiThread(true)
                    .build();
            Realm realm = Realm.getInstance(config);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatMessage chatMessage = realm.createObject(ChatMessage.class);
                    chatMessage.setMessage(replyMessage);
                    chatMessage.setSender("");
                    chatMessage.setTimestamp(System.currentTimeMillis());
                }
            });

            // Toast.makeText(context, "Reply sent: " + replyMessage, Toast.LENGTH_SHORT).show();

            Intent activityIntent = new Intent(context, ChatService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

            // Create a remote input for the reply action
            RemoteInput mRemoteInput = new RemoteInput.Builder(ChatService.ACTION_REPLY)
                    .setLabel("Reply")
                    .build();

            // Create intent to launch chat activity
            Intent chatIntent = new Intent(context, DirectReplyReceiver.class);
            PendingIntent chatPendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                chatPendingIntent = PendingIntent.getBroadcast(context, 0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            }

            // Create a reply action with the remote input
            NotificationCompat.Action replyAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_send,
                            "Reply", chatPendingIntent)
                            .addRemoteInput(mRemoteInput)
                            .setAllowGeneratedReplies(false) // disable suggestions
                            .build();

            // Show the notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Get existing notifications
            StatusBarNotification[] activeNotifications = manager.getActiveNotifications();

            int msgId = 0;

            // Build notification with previous messages
            String updatedText = replyMessage;
            for (StatusBarNotification mNotification : activeNotifications) {
                Notification currentNotification = mNotification.getNotification();
                int currentNotificationId = 2;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (currentNotification != null && currentNotification.getChannelId().equals(ChatService.CHANNEL_ID)) {
                        currentNotificationId = currentNotification.extras.getInt(Notification.EXTRA_NOTIFICATION_ID);
                        if (currentNotificationId == msgId) {
                            // Combine the existing message with the new message
                            updatedText = mNotification.getNotification().extras.getString(Notification.EXTRA_BIG_TEXT) + "\nMe: " + replyMessage;
                            Bundle extras = mNotification.getNotification().extras;
                            break;
                        }
                    }
                }
            }

            // Create the notification with the reply action
            Notification notification = new NotificationCompat.Builder(context, ChatService.CHANNEL_ID)
                    .setContentTitle("New message")
                    .setContentText("Me: "+replyMessage)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .addAction(replyAction)
                    .setOngoing(true)
                    .setNumber(0)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(updatedText))
                    .setContentIntent(contentIntent) // Set content intent here
                    .setAutoCancel(true)
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            manager.notify(msgId, notification);

        }
    }
}
