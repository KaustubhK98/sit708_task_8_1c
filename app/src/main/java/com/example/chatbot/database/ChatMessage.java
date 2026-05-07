package com.example.chatbot.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "message_text")
    public String messageText;

    @ColumnInfo(name = "is_user_message")
    public boolean isUserMessage;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public ChatMessage(String username, String messageText, boolean isUserMessage, long timestamp) {
        this.username = username;
        this.messageText = messageText;
        this.isUserMessage = isUserMessage;
        this.timestamp = timestamp;
    }
}