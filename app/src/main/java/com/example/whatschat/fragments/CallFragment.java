package com.example.whatschat.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.whatschat.CallListAdapter;
import com.example.whatschat.CallModel;
import com.example.whatschat.Chat;
import com.example.whatschat.ChatListAdapter;
import com.example.whatschat.R;
import com.example.whatschat.SocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CallFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CallListAdapter mAdapter;
    private Socket socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_call, container, false);

        // Initialize RecyclerView
        mRecyclerView = view.findViewById(R.id.callRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create ChatListAdapter and set to RecyclerView
        List<CallModel> callList = createCallList();
        mAdapter = new CallListAdapter(getContext(), callList);
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
                                    callList.clear();
                                    Toast.makeText(getContext(), "Adding chats...", Toast.LENGTH_SHORT).show();
                                    Iterator<String> keys = userList.keys();
                                    while (keys.hasNext()) {
                                        String id = keys.next();
                                        String name = userList.getString(id);
                                        if (id.equals(socket.id())) continue;
                                        callList.add(0, new CallModel(name, "10:30 AM", CallModel.INCOMING_CALL, "2:15", R.drawable.person, CallModel.INCOMING_CALL, id));
                                    }
                                    mAdapter.notifyDataSetChanged();
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

    private List<CallModel> createCallList() {
        List<CallModel> callList = new ArrayList<>();
        callList.add(new CallModel("John Smith", "10:30 AM", CallModel.INCOMING_CALL, "2:15", R.drawable.person, CallModel.INCOMING_CALL, "empty"));
        return callList;
    }

}