package com.akro.akrowats;

public class ChatItem {
    public String id;        // WhatsApp ID e.g. 201XXXXXXXXX@c.us
    public String name;
    public String lastMessage;
    public String time;
    public int unreadCount;

    public ChatItem(String id, String name, String lastMessage, String time, int unreadCount) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
    }
}
