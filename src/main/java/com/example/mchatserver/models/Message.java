package com.example.mchatserver.models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {
    private int id;
    private String msgContent;
    private Timestamp timestamp;
    private int userId;

    public Message(String msgContent, Timestamp timestamp, int userId) {
        this.msgContent = msgContent;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public Message() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
