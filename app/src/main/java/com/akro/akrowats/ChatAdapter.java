package com.akro.akrowats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    public interface OnChatClick {
        void onClick(ChatItem item);
    }

    private final List<ChatItem> items;
    private final OnChatClick listener;

    public ChatAdapter(List<ChatItem> items, OnChatClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ChatItem item = items.get(pos);
        h.tvName.setText(item.name);
        h.tvLastMessage.setText(item.lastMessage);
        h.tvTime.setText(item.time);

        if (item.unreadCount > 0) {
            h.tvUnread.setVisibility(View.VISIBLE);
            h.tvUnread.setText(String.valueOf(item.unreadCount));
        } else {
            h.tvUnread.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, tvUnread;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTime = v.findViewById(R.id.tvTime);
            tvUnread = v.findViewById(R.id.tvUnread);
        }
    }
}
