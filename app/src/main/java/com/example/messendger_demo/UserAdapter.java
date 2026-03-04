package com.example.messendger_demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<String> userList;

    public UserAdapter(List<String> userList) {
        this.userList = userList;
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
        String name = userList.get(position);
        if (!name.isEmpty()) {
            holder.avatarLetter.setText(name.substring(0, 1).toUpperCase());
            holder.user_name.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView avatarLetter, user_name, user_messages;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarLetter = itemView.findViewById(R.id.text_avatar_placeholder);
            user_name = itemView.findViewById(R.id.item_user_name);
            user_messages = itemView.findViewById(R.id.item_user_messages);
        }
    }

}
