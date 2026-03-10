package com.example.messendger_demo;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    void insert(LocalMessage message);
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    LiveData<List<LocalMessage>> getMessagesForChat(String chatId);

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    void deleteChatHistory(String chatId);

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    LocalMessage getLastMessageForChat(String chatId);

    @Query("SELECT * FROM messages GROUP BY chatId ORDER BY timestamp DESC")
    LiveData<List<LocalMessage>> getAllLastMessagesLive();
}

