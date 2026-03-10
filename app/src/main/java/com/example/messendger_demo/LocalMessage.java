package com.example.messendger_demo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class LocalMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String chatId;
    public String text;
    public String senderId;
    public long timestamp;

    public LocalMessage(String chatId, String text, String senderId, long timestamp) {
        this.chatId = chatId;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }
}
