package com.example.messendger_demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
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
    private String partnerPublicKey;
    private String partnerUid;
    private DatabaseReference messageRef;
    private AppDatabase localDb;
    private MessageDao messageDao;

    @SuppressLint("NotifyDataSetChanged")
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
        partnerUid = getIntent().getStringExtra("partner_uid");
        loadPartnerPublicKey();
        messageList = new ArrayList<>();
        myUid = FirebaseAuth.getInstance().getUid();
        localDb = androidx.room.Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "chat_database").build();
        messageDao = localDb.messageDao();
        if (chatId != null) {
            messageRef = FirebaseDatabase.getInstance("https://messendger-demo-default-rtdb.europe-west1.firebasedatabase.app/").getReference("chats")
                    .child(chatId).child("messages");
        }
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

        messageDao.getMessagesForChat(chatId).observe(this, localMessages -> {
            messageList.clear();
            for (LocalMessage lm : localMessages) {
                messageList.add(new MessageModel(lm.text, lm.senderId, lm.timestamp));
            }
            adapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });


        btnSend.setOnClickListener(view -> {
            String text = Objects.requireNonNull(editMessage.getText()).toString().trim();
            if(!text.isEmpty() && partnerPublicKey != null) {
                long now = System.currentTimeMillis();

                // Если в списке уже есть сообщения, проверяем время последнего
                if (!messageList.isEmpty()) {
                    long lastMsgTime = (long) messageList.get(messageList.size() - 1).timestamp;
                    if (now <= lastMsgTime) {
                        now = lastMsgTime + 1; // Делаем на 1 мс больше последнего
                    }
                }

                long finalNow = now;
                new Thread(() -> {
                    messageDao.insert(new LocalMessage(chatId, text, myUid, finalNow));
                }).start();
                String encryptedText = CryptoManager.encrypt(text, partnerPublicKey);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("text", encryptedText);
                msgMap.put("senderId", myUid);
                msgMap.put("timestamp", ServerValue.TIMESTAMP);
                messageRef.push().setValue(msgMap);
                FirebaseFirestore.getInstance().collection("chats").document(chatId);
                editMessage.setText("");
            } else if (partnerPublicKey == null) {
                Toast.makeText(this, "Ключ еще не загружен...", Toast.LENGTH_SHORT).show();
            }
        });
        listenForIncomingMessages(chatId);
    }

    private void listenForIncomingMessages(String currentChatId) {
        if (currentChatId == null) return;

        messageRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                MessageModel cloudMsg = snapshot.getValue(MessageModel.class);
                if (cloudMsg == null) return;
                // Если сообщение пришло от СОБЕСЕДНИКА
                if (!cloudMsg.senderId.equals(myUid)) {

                    // 1. РАСШИФРОВЫВАЕМ
                    String decrypted = CryptoManager.decrypt(cloudMsg.text);
                    String finalText = (decrypted != null) ? decrypted : "[Ошибка расшифровки]";

                    // 2. БЕРЕМ ВРЕМЯ (из облака или текущее)
                    long msgTime;
                    if (cloudMsg.timestamp instanceof Long) {
                        msgTime = (Long) cloudMsg.timestamp;
                    } else {
                        // Если вдруг в облаке пусто, используем время телефона
                        msgTime = System.currentTimeMillis();
                    }

                    new Thread(() -> {
                        messageDao.insert(new LocalMessage(
                                currentChatId,
                                finalText,
                                cloudMsg.senderId,
                                msgTime));
                    }).start();

                    // 4. УДАЛЯЕМ ИЗ ОБЛАКА (зачищаем очередь)
                    snapshot.getRef().removeValue();
                } else {
                    snapshot.getRef().removeValue();
                }
            }

            // Эти методы должны быть, но мы оставляем их пустыми
            @Override public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }


    private void loadPartnerPublicKey() {
        if (partnerUid == null) return;
        FirebaseFirestore.getInstance().collection("users").document(partnerUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        partnerPublicKey = documentSnapshot.getString("publicKey");
                    }
                });
    }

    private void listenForMessages() {
        if (messageRef == null) return;

        messageRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Превращаем JSON из RTDB в модель
                    MessageModel msg = ds.getValue(MessageModel.class);

                    if (msg != null) {
                        // Расшифровываем, если сообщение не от нас
                        if (!msg.senderId.equals(myUid)) {
                            String decrypted = CryptoManager.decrypt(msg.text);
                            msg.text = (decrypted != null) ? decrypted : "[Ошибка расшифровки]";
                        }
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Ошибка прослушивания: " + error.getMessage());
            }
        });
    }


}