package com.example.messendger_demo;

import com.google.firebase.Timestamp;

public class MessageModel {
    public String text;
    public String senderId;
    public Object timestamp;

    public MessageModel() {}

    public MessageModel(String text, String senderId, Object timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }
}
