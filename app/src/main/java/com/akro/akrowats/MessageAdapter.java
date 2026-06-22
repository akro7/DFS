package com.akro.akrowats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private final List<MessageItem> items;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<MessageItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        MessageItem msg = items.get(pos);
        String time = sdf.format(new Date(msg.timestamp * 1000));

        if (msg.isOutgoing) {
            h.layoutOut.setVisibility(View.VISIBLE);
            h.layoutIn.setVisibility(View.GONE);
            h.tvMsgOut.setText(msg.body);
            h.tvTimeOut.setText(time);
        } else {
            h.layoutIn.setVisibility(View.VISIBLE);
            h.layoutOut.setVisibility(View.GONE);
            h.tvMsgIn.setText(msg.body);
            h.tvTimeIn.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View layoutOut, layoutIn;
        TextView tvMsgOut, tvTimeOut, tvMsgIn, tvTimeIn;

        VH(View v) {
            super(v);
            layoutOut = v.findViewById(R.id.layoutOut);
            layoutIn = v.findViewById(R.id.layoutIn);
            tvMsgOut = v.findViewById(R.id.tvMsgOut);
            tvTimeOut = v.findViewById(R.id.tvTimeOut);
            tvMsgIn = v.findViewById(R.id.tvMsgIn);
            tvTimeIn = v.findViewById(R.id.tvTimeIn);
        }
    }
}
