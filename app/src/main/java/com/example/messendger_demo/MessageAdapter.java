package com.example.messendger_demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MessageModel> messages;
    private String myUid;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    public MessageAdapter(List<MessageModel> messages, String myUid) {
        this.messages = messages;
        this.myUid = myUid;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).senderId.equals(myUid)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).messageText.setText(message.text);
        } else {
            ((ReceivedViewHolder) holder).messageText.setText(message.text);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        SentViewHolder(View itemView) { super(itemView); messageText = itemView.findViewById(R.id.text_message); }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ReceivedViewHolder(View itemView) { super(itemView); messageText = itemView.findViewById(R.id.text_message); }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
