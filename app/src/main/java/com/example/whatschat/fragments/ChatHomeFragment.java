package com.example.whatschat.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.whatschat.Chat;
import com.example.whatschat.ChatListAdapter;
import com.example.whatschat.ChatMessage;
import com.example.whatschat.R;
import com.example.whatschat.SocketManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatHomeFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ChatListAdapter mAdapter;
    private Socket socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_home, container, false);

        // Initialize RecyclerView
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create ChatListAdapter and set to RecyclerView
        List<Chat> chatList = createChatList();
        mAdapter = new ChatListAdapter(chatList);
        mRecyclerView.setAdapter(mAdapter);
        try {
            socket = SocketManager.getSocket();
            socket.emit("get-users");
            socket.on("left", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("get-users");
                }
            });
            socket.on("user-joined", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("get-users");
                }
            });
            socket.on("users-in-room", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject userList = (JSONObject) args[0];
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject userList = (JSONObject) args[0];
                                    chatList.clear();
                                    Toast.makeText(getContext(), "Adding chats...", Toast.LENGTH_SHORT).show();
                                    Iterator<String> keys = userList.keys();
                                    while (keys.hasNext()) {
                                        String id = keys.next();
                                        String name = userList.getString(id);
                                        if (id.equals(socket.id())) continue;
                                        chatList.add(0, new Chat(name, "Hey, there!", R.drawable.person));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    chatList.add(0, new Chat("General Stream", "Yo bros.. Sigma boys...", R.drawable.person));
                                } catch (JSONException ignored) {
                                }
                            }
                        });
                    }
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return view;
    }

    private List<Chat> createChatList() {
        List<Chat> chatList = new ArrayList<>();
        chatList.add(new Chat("General Stream", "Yo bros.. Sigma boys...", R.drawable.person));
//        chatList.add(new Chat("Alice", "See you later!", R.drawable.person));
//        chatList.add(new Chat("Bob", "Thanks for the help.", R.drawable.person));
//        chatList.add(new Chat("Eva", "Can you call me now?", R.drawable.person));

        return chatList;
    }

}