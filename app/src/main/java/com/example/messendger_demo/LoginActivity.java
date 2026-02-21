package com.example.messendger_demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView title = findViewById(R.id.title);
        TextInputLayout email = findViewById(R.id.inputEmailLayout);
        TextInputLayout pass = findViewById(R.id.inputPassLayout);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        animate(title, email, pass, btnLogin, tvGoToRegister);

        btnLogin.setOnClickListener(view -> {
            assert email.getEditText() != null;
            String emailStr = email.getEditText().getText().toString().trim();
            assert pass.getEditText() != null;
            String passStr = pass.getEditText().getText().toString().trim();
            if (emailStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(this, "Введите почту и пароль", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(emailStr, passStr)
                    .addOnCompleteListener(this, task -> {
                        if(task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Ошибка входа";
                            Log.e("AUTH", "Ошибка: " + errorMsg);
                            if (errorMsg.contains("password")) {
                                pass.setError("Неверный пароль");
                            } else {
                                email.setError("Пользователь не найден или данные неверны");
                            }
                        }
                    });
        });

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    void animate(View tit, View e, View p, View b, TextView t) {
        List<View> views = Arrays.asList(tit, e, p, b, t);
        for (int i = 0; i < views.size(); i++) {
            View v = views.get(i);
            v.setAlpha(0f);
            v.setTranslationY(80f);
            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(1000)
                    .setStartDelay(150L * i)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}

