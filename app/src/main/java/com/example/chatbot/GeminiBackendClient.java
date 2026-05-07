package com.example.chatbot;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiBackendClient {

    private static final String API_URL = "http://10.0.2.2:3000/chat";

    private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface ChatCallback {
        void onSuccess(String reply);

        void onError(String errorMessage);
    }

    public static void sendMessageToChatbot(String userMessage, ChatCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("message", userMessage);

            RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder().url(API_URL).addHeader("Content-Type", "application/json").post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Backend connection failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        callback.onError("Backend error: " + response.code() + " " + responseBody);
                        return;
                    }

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String reply = jsonResponse.optString("reply", "No reply received from Gemini.");
                        callback.onSuccess(reply);
                    } catch (Exception e) {
                        callback.onError("Failed to read backend response: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request creation error: " + e.getMessage());
        }
    }
}