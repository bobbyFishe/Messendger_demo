package com.example.messendger_demo;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {LocalMessage.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
}

