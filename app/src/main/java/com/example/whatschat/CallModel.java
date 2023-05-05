package com.example.whatschat;

public class CallModel {
    private String callerName;
    private int callerAvatar;
    private String callTime;
    private int callType;
    private String callDuration;
    private String socketId;

    public static final int INCOMING_CALL = 0;
    public static final int OUTGOING_CALL = 1;
    public static final int MISSED_CALL = 2;
    public static final int REJECTED_CALL = 3;
    public static final int BLOCKED_CALL = 4;
    public static final int VOICEMAIL = 5;

    public CallModel(String callerName, String callTime, int callType, String callDuration, int callerAvatar, int type, String socketId) {
        this.callerName = callerName;
        this.callTime = callTime;
        this.callType = callType;
        this.callDuration = callDuration;
        this.callerAvatar = callerAvatar;
        this.callType = type;
        this.socketId = socketId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public int getCallerAvatar(){
        return callerAvatar;
    }

    public void setCallerAvatar(int id){
        callerAvatar = id;
    }

    public void setSocketId(String socketId){
        this.socketId = socketId;
    }

    public String getSocketId(){
        return socketId;
    }
}
