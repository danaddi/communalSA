package com.example.test_app.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.test_app.MainActivity;
import com.example.test_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvName;
    private Button btnLogout, btnDelete;

    private FirebaseAuth mAuth;

    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvEmail = view.findViewById(R.id.tvEmail);
        tvName = view.findViewById(R.id.tvName);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDelete = view.findViewById(R.id.btnDelete);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference("payments").child(user.getUid());
            // Загружаем имя пользователя
            loadUserName();
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        return view;
    }

    private void loadUserName() {
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getView() == null) return;

                String name = snapshot.getValue(String.class);

                if (name != null && !name.isEmpty()) {
                    tvName.setText(name);
                } else {
                    tvName.setText("Имя не указано");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getView() != null) {
                    Toast.makeText(getContext(), "Ошибка загрузки имени", Toast.LENGTH_SHORT).show();
                    tvName.setText("Ошибка загрузки");
                }
            }
        });
    }



    private void showDeleteConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Удалить аккаунт")
                .setMessage("Вы уверены, что хотите удалить аккаунт?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteUser())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Удаление из Realtime Database
            FirebaseDatabase.getInstance().getReference("utilities").child(uid).removeValue();

            // Удаление аккаунта
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Аккаунт удален", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
