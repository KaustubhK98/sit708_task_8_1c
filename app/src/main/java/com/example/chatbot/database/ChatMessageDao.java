package com.example.chatbot.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    void insertMessage(ChatMessage chatMessage);

    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForUser(String username);

    @Query("DELETE FROM chat_messages WHERE username = :username")
    void clearMessagesForUser(String username);
}