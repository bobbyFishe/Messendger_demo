package com.example.messendger_demo;

public class ChatModel {
    public String chatName;
    public String chatId;
    public String lastMessage;
    public String partnerUid;

    public ChatModel(String chatName, String chatId, String lastMessage, String partnerUid) {
        this.chatName = chatName;
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.partnerUid = partnerUid;
    }
}
