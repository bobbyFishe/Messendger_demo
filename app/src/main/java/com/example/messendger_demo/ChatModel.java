package com.example.messendger_demo;

public class ChatModel {
    public String chatName;
    public String chatId;
    public String lastMessage;

    public ChatModel(String chatName, String chatId, String lastMessage) {
        this.chatName = chatName;
        this.chatId = chatId;
        this.lastMessage = lastMessage;
    }
}
