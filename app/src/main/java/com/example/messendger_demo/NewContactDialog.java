package com.example.messendger_demo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

public class NewContactDialog extends DialogFragment {
    private Button btnSearch;
    private EditText searchingName;

    public interface OnContactFoundListener {
        void onContactFound(String name, String uid);
    }

    private OnContactFoundListener listener;

    public void setOnContactFoundListener(OnContactFoundListener listener) {
        this.listener = listener;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_contact, container, false);
        btnSearch = view.findViewById(R.id.btnSearch);
        searchingName = view.findViewById(R.id.editText_searching_name);

        btnSearch.setOnClickListener(view1 -> searchNewContact());
        return view;
    }

    public void searchNewContact() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String sName = searchingName.getText().toString().trim();
        if(sName.isEmpty()) {
            Toast.makeText(requireContext(), "Введи имя", Toast.LENGTH_SHORT).show();
            return;
        }
        String userName = sName.substring(0, 1).toUpperCase() + sName.substring(1);
        db.collection("users")
                .whereEqualTo("name", userName)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            if(listener != null) {
                                listener.onContactFound(userName, task.getResult().getDocuments().get(0).getId());
                            }
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Ошибка проверки имени", task.getException());
                    }
                });
    }
}
