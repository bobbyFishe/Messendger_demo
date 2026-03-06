package com.example.messendger_demo;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<ChatModel> chatList;

    public UserAdapter(List<ChatModel> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        ChatModel chat = chatList.get(position);
        holder.avatarLetter.setText(chat.chatName.substring(0, 1).toUpperCase());
        holder.user_name.setText(chat.chatName);
        holder.last_messages.setText(chat.lastMessage);          //// последенее сообщение
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ChatActivity.class);
            intent.putExtra("chat_id", chat.chatId);
            intent.putExtra("chat_name", chat.chatName);
            view.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView avatarLetter, user_name, last_messages;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarLetter = itemView.findViewById(R.id.text_avatar_placeholder);
            user_name = itemView.findViewById(R.id.item_user_name);
            last_messages = itemView.findViewById(R.id.item_user_messages);
        }
    }

}
