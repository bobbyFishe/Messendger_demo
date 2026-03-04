package com.example.messendger_demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ImageButton btn_menu;
    private RecyclerView chats;
    private FloatingActionButton newChat;

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
//        loadUserData();
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Гость");

        List<String> names = Arrays.asList("Андрей", "Борис", "Виктор", "Геннадий");
        chats.setLayoutManager(new LinearLayoutManager(this));
        UserAdapter adapter = new UserAdapter(names);
        chats.setAdapter(adapter);

        tvName.setText(userName);
        btn_menu.setOnClickListener(this::popup_show);
        newChat.setOnClickListener(view -> {
            NewContactDialog dialog = new NewContactDialog();
            dialog.show(getSupportFragmentManager(), "newContactDialog");
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

//    private void loadUserData() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//        TextView tvName = findViewById(R.id.text_view_name);
//
//        db.collection("users").document(uid).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String name = documentSnapshot.getString("name");
//                        tvName.setText(name);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Ошибка загрузки данных", e);
//                });
//    }
}