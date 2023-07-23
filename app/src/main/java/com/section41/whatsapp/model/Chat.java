package com.section41.whatsapp.model;

import java.sql.Timestamp;
import java.util.Date;

public class Chat {
private  String id;
private  String sender;
private  String receiver;
private  String message;
private  boolean isSeen;
    private long timestamp;

    public Chat(String id,String sender, String receiver, String message, boolean isSeen, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.timestamp = timestamp;
        this.id = id;
    }

    public Chat() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsSeen() {

        return isSeen;
    }

    public void seIsSeen(boolean seen) {
        this.isSeen = seen;
    }
}
