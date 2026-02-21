package com.example.messendger_demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextInputLayout inputEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView title = findViewById(R.id.title);
        TextInputLayout inputUser = findViewById(R.id.inputUserLayout);
        inputEmail = findViewById(R.id.inputEmailLayout);
        TextInputLayout inputPass = findViewById(R.id.inputPassLayout);
        MaterialButton btnReg = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();

        btnReg.setOnClickListener(view -> {
            inputUser.setError(null);
            inputEmail.setError(null);
            assert inputUser.getEditText() != null;
            String nameInput = inputUser.getEditText().getText().toString().trim();
            String nameStr;
            if (!nameInput.isEmpty()) {
                nameStr = nameInput.substring(0, 1).toUpperCase() + nameInput.substring(1);
            } else {
                nameStr = "";
            }
            assert inputEmail.getEditText() != null;
            String emailStr = inputEmail.getEditText().getText().toString().trim();
            assert inputPass.getEditText() != null;
            String passStr = inputPass.getEditText().getText().toString().trim();

            if(nameStr.isEmpty() || emailStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }
            String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d).+$";
            if (passStr.length() < 6) {
                inputPass.setError("Пароль должен быть не менее 6 символов");
                return;
            } else if (!passStr.matches(passwordPattern)) {
                inputPass.setError("Пароль должен содержать латинские буквы и цифры");
                return;
            } else {
                inputPass.setError(null);
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("name", nameStr)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                inputUser.setError("Это имя уже занято");
                            } else {
                                registerUser(emailStr, passStr, nameStr, db);
                            }
                        } else {
                            Log.e("Firestore", "Ошибка проверки имени", task.getException());
                        }
                    });

        });
        animate(title, inputUser, inputEmail, inputPass, btnReg);
    }

    void animate(TextView title, TextInputLayout inputUser, TextInputLayout inputEmail, TextInputLayout inputPass, MaterialButton btnReg) {
        List<View> viewsToAnimate = Arrays.asList(title, inputUser, inputEmail, inputPass, btnReg);
        for (int i = 0; i < viewsToAnimate.size(); i++) {
            View view = viewsToAnimate.get(i);
            view.setAlpha(0f);
            view.setTranslationY(100f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(1000)
                    .setStartDelay(150L * i)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void registerUser(String email, String password, String name, FirebaseFirestore db) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->  {
                    if(task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("uid", userId);

                        db.collection("users").document(userId)
                                .set(userMap)
                                .addOnCompleteListener(aVoid -> {
                                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        String error = Objects.requireNonNull(task.getException()).getMessage();
                        assert error != null;
                        if (error.contains("email address is already in use")) {
                            inputEmail.setError("Эта почта уже зарегистрирована");
                        } else {
                            Toast.makeText(this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}