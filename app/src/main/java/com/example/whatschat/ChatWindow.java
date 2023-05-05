package com.example.whatschat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatWindow extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private ArrayList<ChatMessage> mChatMessageList;
    String name;
    EditText messageEditText;
    ImageButton sendButton;
    private Socket socket;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
            toolbar.setTitle(name);
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.msg);
        Realm.init(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .build();
        Realm realm = Realm.getInstance(config);

        // Initialize the RecyclerView and set its layout manager
        mRecyclerView = findViewById(R.id.recyclerView_chat);
        sendButton = findViewById(R.id.button_chatBox_send);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        messageEditText = findViewById(R.id.editText_chatBox);

        // Initialize the chat message list and adapter
        mChatMessageList = new ArrayList<>();
        RealmResults<ChatMessage> chatMessages = realm.where(ChatMessage.class).findAll();

        for (ChatMessage msg:chatMessages) {
            int type = !TextUtils.isEmpty(msg.getSender()) ? ChatMessage.TYPE_LEFT : ChatMessage.TYPE_RIGHT;
            mChatMessageList.add(new ChatMessage(msg.getMessage(), msg.getSender(), msg.getTimestamp(), type));
        }

        mAdapter = new ChatAdapter(mChatMessageList);

        // Set the adapter for the RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        // Notify the adapter that the data has changed
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        layoutManager.scrollToPosition(mAdapter.getItemCount() - 1);

        try {
            socket = SocketManager.getSocket();
            if (socket.connected()){
                Toast.makeText(this, "Socket Connected!!",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Socket not Connected!! "+socket.io().toString(),Toast.LENGTH_SHORT).show();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on("user-joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String name = "";
                String userId = "";
                try {
                    name = data.getString("name");
                    userId = data.getString("userId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String finalName = name;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatMessageList.add(new ChatMessage(finalName + " joined the chat", finalName, System.currentTimeMillis(), ChatMessage.TYPE_INFO));
                        mAdapter.notifyItemInserted(mChatMessageList.size() - 1);
                        mRecyclerView.scrollToPosition(mChatMessageList.size() - 1);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        socket.off("receive");
        socket.on("receive", msgReceive);
        socket.on("left", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String name = (String) args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatMessageList.add(new ChatMessage(name + " left the chat", name, System.currentTimeMillis(), ChatMessage.TYPE_INFO));
                        mAdapter.notifyItemInserted(mChatMessageList.size() - 1);
                        mRecyclerView.scrollToPosition(mChatMessageList.size() - 1);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptSend();
                    Toast.makeText(ChatWindow.this, "try chestunna", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatWindow.this, "msg pole ra pulka", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void attemptSend() {
        String message = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        mChatMessageList.add(new ChatMessage(message, name, System.currentTimeMillis(), ChatMessage.TYPE_RIGHT));
        mAdapter.notifyItemInserted(mChatMessageList.size() - 1);
        mRecyclerView.scrollToPosition(mChatMessageList.size() - 1);
        mAdapter.notifyDataSetChanged();

        messageEditText.setText("");
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "Phoneodu");

            JSONObject data = new JSONObject();
            data.put("message", message);
            data.put("roomId", "general");
            data.put("from", username);
            socket.emit("send", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .build();
        Realm realm = Realm.getInstance(config);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ChatMessage chatMessage = realm.createObject(ChatMessage.class);
                chatMessage.setMessage(message);
                chatMessage.setSender("");
                chatMessage.setTimestamp(System.currentTimeMillis());
            }
        });

    }

    private final Emitter.Listener msgReceive = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, sender;
                    try {
                        message = data.getString("message");
                        sender = data.getString("name");
                    } catch (JSONException e) {
                        return;
                    }
                    Log.d("Hiiiiiiiiiiiiiii", "call: "+message);
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

                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                    mediaPlayer = MediaPlayer.create(ChatWindow.this, R.raw.msg);
                    mediaPlayer.start();

                    // display the received message
                    mChatMessageList.add(new ChatMessage(message, sender, System.currentTimeMillis(), ChatMessage.TYPE_LEFT));
                    mAdapter.notifyItemInserted(mChatMessageList.size() - 1);
                    mRecyclerView.scrollToPosition(mChatMessageList.size() - 1);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // handle back button click here
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        socket.off("receive");

        ChatService chatService = new ChatService();

        // Start the service to listen for messages
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, new Intent(this, ChatService.class));
        } else {
            this.startService(new Intent(this, ChatService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ChatService.isRunning()){
            Intent serviceIntent = new Intent(this, ChatService.class);
            this.stopService(serviceIntent);
        }
    }

}