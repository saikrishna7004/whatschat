package com.example.whatschat;

public class Chat {
    private String mName;
    private String mLastMessage;
    private int mAvatarResId;
    private String mRoomId;

    public Chat(String name, String lastMessage, int avatarResId) {
        mName = name;
        mLastMessage = lastMessage;
        mAvatarResId = avatarResId;
    }

    public Chat(String name, String lastMessage, String roomId, int avatarResId) {
        mName = name;
        mLastMessage = lastMessage;
        mRoomId = roomId;
        mAvatarResId = avatarResId;
    }

    public String getName() {
        return mName;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public int getAvatarResId() {
        return mAvatarResId;
    }

    public String getRoomId(){
        return mRoomId;
    }
}