package com.example.messendger_demo;

import com.google.firebase.Timestamp;

public class MessageModel {
    public String text;
    public String senderId;
    public Timestamp timestamp;

    public MessageModel() {}

    public MessageModel(String text, String senderId, Timestamp timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }
}
