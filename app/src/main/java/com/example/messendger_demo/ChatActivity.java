package com.example.messendger_demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private List<MessageModel> messageList;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private TextInputEditText editMessage;
    private FloatingActionButton btnSend;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String chatId = getIntent().getStringExtra("chat_id");
        messageList = new ArrayList<>();
        myUid = FirebaseAuth.getInstance().getUid();
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.recycler_messages);

        String partnerName = getIntent().getStringExtra("chat_name");
        toolbar = findViewById(R.id.chatToolbar);
        toolbar.setTitle(partnerName);
        toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new MessageAdapter(messageList, myUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) { // Если нижняя граница поднялась (открылась клавиатура)
                if (!messageList.isEmpty()) {
                    recyclerView.postDelayed(() -> {
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }, 100); // Небольшая задержка для плавности
                }
            }
        });


        btnSend.setOnClickListener(view -> {
            String text = Objects.requireNonNull(editMessage.getText()).toString().trim();
            if(!text.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("text", text);
                msgMap.put("senderId", myUid);
                msgMap.put("timestamp", FieldValue.serverTimestamp());
                db.collection("chats").document(chatId)
                        .update("lastMessage", text, "timestamp", FieldValue.serverTimestamp())
                        .addOnFailureListener(e -> Log.e("Firestore", "Ошибка обновления превью", e));
                db.collection("chats").document(chatId)
                        .collection("messages")
                        .add(msgMap)
                        .addOnSuccessListener(documentReference -> {
                            editMessage.setText(""); // Очищаем поле только после успеха
                        });

                recyclerView.scrollToPosition(messageList.size() - 1);
                editMessage.setText("");
            }
        });
        listenForMessages(chatId);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listenForMessages(String chatId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Слушаем ВЛОЖЕННУЮ коллекцию messages внутри конкретного чата
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING) // Сортировка по времени
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Ошибка прослушивания сообщений", error);
                        return;
                    }

                    if (value != null) {
                        messageList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            // Превращаем документ из базы в объект MessageModel
                            MessageModel msg = doc.toObject(MessageModel.class);
                            messageList.add(msg);
                        }

                        // Обновляем список на экране
                        adapter.notifyDataSetChanged();

                        // Автоматически прокручиваем в самый низ к новому сообщению
                        if (messageList.size() > 0) {
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }


}