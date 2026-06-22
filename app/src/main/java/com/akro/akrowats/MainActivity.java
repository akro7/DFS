package com.akro.akrowats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private List<ChatItem> chatList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recycler = findViewById(R.id.recyclerChats);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatList, item -> openChat(item));
        recycler.setAdapter(chatAdapter);

        // Get saved server URL
        SharedPreferences prefs = getSharedPreferences("akrowats", MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", "http://192.168.1.5:3000");

        apiClient = new ApiClient();
        apiClient.setServerUrl(serverUrl);

        loadChats();

        // Listen for incoming messages via WebSocket
        apiClient.connectWebSocket(
            new ApiClient.QrCallback() {
                @Override public void onQr(String q) {}
                @Override public void onReady(String n) {}
                @Override public void onError(String msg) {}
            },
            (from, body, timestamp) -> {
                // Update or add chat
                boolean found = false;
                for (ChatItem c : chatList) {
                    if (c.id.equals(from)) {
                        c.lastMessage = body;
                        c.unreadCount++;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    chatList.add(0, new ChatItem(from, from.replace("@c.us", ""), body, "now", 1));
                }
                chatAdapter.notifyDataSetChanged();
            }
        );
    }

    private void loadChats() {
        apiClient.getChats(new ApiClient.ChatsCallback() {
            @Override
            public void onChats(String jsonArray) {
                try {
                    JsonArray arr = new Gson().fromJson(jsonArray, JsonArray.class);
                    chatList.clear();
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject obj = arr.get(i).getAsJsonObject();
                        String id = obj.get("id").getAsString();
                        String name = obj.has("name") ? obj.get("name").getAsString() : id.replace("@c.us", "");
                        String last = obj.has("lastMessage") ? obj.get("lastMessage").getAsString() : "";
                        String time = obj.has("time") ? obj.get("time").getAsString() : "";
                        int unread = obj.has("unread") ? obj.get("unread").getAsInt() : 0;
                        chatList.add(new ChatItem(id, name, last, time, unread));
                    }
                    chatAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(ChatItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chat_id", item.id);
        intent.putExtra("chat_name", item.name);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "New Chat");
        menu.add(0, 2, 0, "Disconnect");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showNewChatDialog();
        } else if (item.getItemId() == 2) {
            disconnect();
        }
        return true;
    }

    private void showNewChatDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(getString(R.string.enter_number));

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.new_chat))
            .setView(input)
            .setPositiveButton("Open", (d, w) -> {
                String number = input.getText().toString().trim();
                if (!number.isEmpty()) {
                    ChatItem temp = new ChatItem(number + "@c.us", number, "", "", 0);
                    openChat(temp);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void disconnect() {
        getSharedPreferences("akrowats", MODE_PRIVATE).edit().remove("server_url").apply();
        startActivity(new Intent(this, QrActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiClient != null) apiClient.disconnect();
    }
}
