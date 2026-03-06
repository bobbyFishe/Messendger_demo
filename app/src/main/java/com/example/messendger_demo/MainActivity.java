package com.example.messendger_demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageButton btn_menu;
    private RecyclerView chats;
    private FloatingActionButton newChat;
    private List<ChatModel> chatList = new ArrayList<>();
    private UserAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvName = findViewById(R.id.text_view_name);
        chats = findViewById(R.id.recycler_view_contacts);
        btn_menu = findViewById(R.id.imageButton_menu);
        newChat = findViewById(R.id.floatingActionButton_new_chat);

        adapter = new UserAdapter(chatList);
        chats.setLayoutManager(new LinearLayoutManager(this));
        chats.setAdapter(adapter);
        tvName.setText(authentication());

        btn_menu.setOnClickListener(this::popup_show);
        newChat.setOnClickListener(view -> {
            NewContactDialog dialog = new NewContactDialog();
            dialog.setOnContactFoundListener((name, uid) -> {
                createNewChatInFirestore(name, uid);
            });
            dialog.show(getSupportFragmentManager(), "newContactDialog");
        });
        listenForChats();
    }

    private void createNewChatInFirestore(String partnerName, String partnerUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String myUID = FirebaseAuth.getInstance().getUid();

        if(myUID == null) {
            return;
        }
        if(myUID.equals(partnerUid)) {
            Toast.makeText(this, "Чат с самим собой не создается", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] uids = {myUID, partnerUid};
        Arrays.sort(uids);
        String chatId = uids[0] + "_" + uids[1];
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String myName = prefs.getString("user_name", "Пользователь");

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("members", Arrays.asList(myUID,partnerUid));
        chatData.put("lastMessage", "");
        chatData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        chatData.put("name_" + myUID, myName);
        chatData.put("name_" + partnerUid, partnerName);

        db.collection("chats").document(chatId)
                .set(chatData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Чат с " + partnerName + " готов!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Ошибка создания чата", e);
                });
    }

    private void popup_show(View view) {
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.my_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if(id == R.id.item_option) {
                Toast.makeText(this, "Опции", Toast.LENGTH_SHORT).show();
                return true;
            } else if( id == R.id.item_exit) {
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private String authentication() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("user_name", "Гость");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listenForChats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String myUID = FirebaseAuth.getInstance().getUid();

        if(myUID == null) return;

        db.collection("chats")
                .whereArrayContains("members", myUID)
                .addSnapshotListener(((value, error) -> {
                    if(error != null) {
                        Log.e("Firestore", "Ошибка прослушивания", error);
                        return;
                    }
                    if(value != null) {
                        chatList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            List<String> members = (List<String>) doc.get("members");
                            if(members != null) {
                                for (String mUid : members) {
                                    if (!mUid.equals(myUID)) {
                                        String partnerName = doc.getString("name_" + mUid);
                                        String name = partnerName != null ? partnerName : "Собеседник";
                                        String lastMsg = doc.getString("lastMessage");
                                        String id = doc.getId();
                                        chatList.add(new ChatModel(name, id, lastMsg != null ? lastMsg : ""));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }));
    }
}