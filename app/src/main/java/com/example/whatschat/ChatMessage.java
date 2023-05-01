package com.example.whatschat;

import io.realm.RealmObject;

public class ChatMessage extends RealmObject {
    private String text;
    private String sender;
    private long timestamp;
    private int type;
    private String roomId;

    public ChatMessage(){}

    public ChatMessage(String text, String sender, long timestamp, int type) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getMessage() {
        return text;
    }

    public void setMessage(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRoomId(){
        return roomId;
    }

    public void setRoomId(String rid){
        this.roomId = rid;
    }

    public static final int TYPE_LEFT = 0;
    public static final int TYPE_RIGHT = 1;
    public static final int TYPE_INFO = 2;
}
