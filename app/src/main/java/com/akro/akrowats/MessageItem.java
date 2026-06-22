package com.akro.akrowats;

public class MessageItem {
    public String body;
    public long timestamp;
    public boolean isOutgoing;

    public MessageItem(String body, long timestamp, boolean isOutgoing) {
        this.body = body;
        this.timestamp = timestamp;
        this.isOutgoing = isOutgoing;
    }
}
