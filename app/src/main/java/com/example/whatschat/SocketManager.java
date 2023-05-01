package com.example.whatschat;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;

public class SocketManager {
    private static Socket socket;
    public static final String SOCKET_URL = "https://nodechat-x5xs.onrender.com";

    public static Socket getSocket() throws URISyntaxException {
        if (socket == null) {
            socket = IO.socket(SOCKET_URL);
        }
        return socket;
    }
}
