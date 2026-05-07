package com.example.chatbot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbot.adapter.ChatAdapter;
import com.example.chatbot.database.AppDatabase;
import com.example.chatbot.database.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private TextView welcomeText;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private AppDatabase database;
    private String username;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = getIntent().getStringExtra("username");

        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        welcomeText = findViewById(R.id.textViewWelcome);
        messagesRecyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);

        welcomeText.setText("Welcome, " + username);

        database = AppDatabase.getInstance(this);

        chatAdapter = new ChatAdapter(messageList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(chatAdapter);

        loadChatHistory();

        sendButton.setOnClickListener(view -> sendUserMessage());
    }

    private void loadChatHistory() {
        databaseExecutor.execute(() -> {
            List<ChatMessage> savedMessages = database.chatMessageDao().getMessagesForUser(username);

            if (savedMessages.isEmpty()) {
                ChatMessage welcomeMessage = new ChatMessage(username, "Welcome " + username + "! How can I help you today?", false, System.currentTimeMillis());

                database.chatMessageDao().insertMessage(welcomeMessage);
                savedMessages.add(welcomeMessage);
            }

            mainHandler.post(() -> {
                messageList.clear();
                messageList.addAll(savedMessages);
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
            });
        });
    }

    private void sendUserMessage() {
        String userText = messageInput.getText().toString().trim();

        if (userText.isEmpty()) {
            Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show();
            return;
        }

        messageInput.setText("");

        ChatMessage userMessage = new ChatMessage(username, userText, true, System.currentTimeMillis());

        addMessageToScreen(userMessage);
        saveMessageToDatabase(userMessage);

        sendButton.setEnabled(false);
        sendButton.setText("...");

        GeminiBackendClient.sendMessageToChatbot(userText, new GeminiBackendClient.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                ChatMessage botMessage = new ChatMessage(username, reply, false, System.currentTimeMillis());

                mainHandler.post(() -> {
                    addMessageToScreen(botMessage);
                    saveMessageToDatabase(botMessage);
                    sendButton.setEnabled(true);
                    sendButton.setText("➤");
                });
            }

            @Override
            public void onError(String errorMessage) {
                ChatMessage errorBotMessage = new ChatMessage(username, "Sorry, I could not connect to the Gemini backend. " + errorMessage, false, System.currentTimeMillis());

                mainHandler.post(() -> {
                    addMessageToScreen(errorBotMessage);
                    saveMessageToDatabase(errorBotMessage);
                    sendButton.setEnabled(true);
                    sendButton.setText("➤");
                });
            }
        });
    }

    private void addMessageToScreen(ChatMessage chatMessage) {
        messageList.add(chatMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void saveMessageToDatabase(ChatMessage chatMessage) {
        databaseExecutor.execute(() -> database.chatMessageDao().insertMessage(chatMessage));
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
    }
}