package com.akro.akrowats;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ApiClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WebSocket webSocket;

    public interface QrCallback {
        void onQr(String base64Qr);
        void onReady(String number);
        void onError(String msg);
    }

    public interface MessageCallback {
        void onMessage(String from, String body, long timestamp);
    }

    public interface SendCallback {
        void onSuccess();
        void onError(String msg);
    }

    private String serverUrl = "http://192.168.1.5:3000";

    public void setServerUrl(String url) {
        this.serverUrl = url;
    }

    // --- Connect via WebSocket for QR and events ---
    public void connectWebSocket(QrCallback qrCallback, MessageCallback messageCallback) {
        String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws";

        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {}

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    JsonObject json = gson.fromJson(text, JsonObject.class);
                    String type = json.get("type").getAsString();

                    mainHandler.post(() -> {
                        switch (type) {
                            case "qr":
                                qrCallback.onQr(json.get("data").getAsString());
                                break;
                            case "ready":
                                qrCallback.onReady(json.get("number").getAsString());
                                break;
                            case "message":
                                String from = json.get("from").getAsString();
                                String body = json.get("body").getAsString();
                                long ts = json.get("timestamp").getAsLong();
                                messageCallback.onMessage(from, body, ts);
                                break;
                        }
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> qrCallback.onError(e.getMessage()));
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                mainHandler.post(() -> qrCallback.onError(t.getMessage()));
            }
        });
    }

    // --- Send message via REST ---
    public void sendMessage(String to, String body, SendCallback callback) {
        new Thread(() -> {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("to", to);
                json.addProperty("body", body);

                RequestBody requestBody = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(serverUrl + "/send")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        mainHandler.post(callback::onSuccess);
                    } else {
                        mainHandler.post(() -> callback.onError("HTTP " + response.code()));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    // --- Get chat list ---
    public interface ChatsCallback {
        void onChats(String jsonArray);
        void onError(String msg);
    }

    public void getChats(ChatsCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(serverUrl + "/chats")
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        mainHandler.post(() -> callback.onChats(body));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed"));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    public void disconnect() {
        if (webSocket != null) webSocket.cancel();
    }
}
