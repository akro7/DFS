package com.akro.akrowats;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private List<MessageItem> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private String chatId;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chat_id");
        String chatName = getIntent().getStringExtra("chat_name");

        TextView tvName = findViewById(R.id.tvName);
        tvName.setText(chatName);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recyclerMessages);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messages);
        recycler.setAdapter(messageAdapter);

        EditText etMessage = findViewById(R.id.etMessage);
        ImageView btnSend = findViewById(R.id.btnSend);

        SharedPreferences prefs = getSharedPreferences("akrowats", MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", "http://192.168.1.5:3000");

        apiClient = new ApiClient();
        apiClient.setServerUrl(serverUrl);

        // Listen for incoming messages
        apiClient.connectWebSocket(
            new ApiClient.QrCallback() {
                @Override public void onQr(String q) {}
                @Override public void onReady(String n) {}
                @Override public void onError(String msg) {}
            },
            (from, body, timestamp) -> {
                if (from.equals(chatId)) {
                    messages.add(new MessageItem(body, timestamp, false));
                    messageAdapter.notifyItemInserted(messages.size() - 1);
                    recycler.scrollToPosition(messages.size() - 1);
                }
            }
        );

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;

            long now = System.currentTimeMillis() / 1000;
            messages.add(new MessageItem(text, now, true));
            messageAdapter.notifyItemInserted(messages.size() - 1);
            recycler.scrollToPosition(messages.size() - 1);
            etMessage.setText("");

            apiClient.sendMessage(chatId, text, new ApiClient.SendCallback() {
                @Override public void onSuccess() {}
                @Override public void onError(String msg) {
                    Toast.makeText(ChatActivity.this, "Failed: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiClient != null) apiClient.disconnect();
    }
}
