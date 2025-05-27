package com.example.test_app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.test_app.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment {

    private TextView welcomeText, featuresText, statsText, tipText;
    private MaterialCardView statsCard;
    private Handler notificationHandler = new Handler(Looper.getMainLooper());
    private Runnable notificationRunnable;
    private static final int FIRST_DELAY_MS = 10000; // 10 секунд
    private static final int PERIOD_MS = 60000; // 1 минута

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Для ActionBar
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.tvWelcome);
        featuresText = view.findViewById(R.id.tvFeatures);
        statsText = view.findViewById(R.id.tvStats);
        tipText = view.findViewById(R.id.tvTip);

        setWelcomeMessage();
        setFeaturesList();
        loadUserStats();
        setTip();

        startNotificationSchedule(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notificationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_notifications, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            showNotification(getView(), "У вас нет новых уведомлений");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWelcomeMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.child("name").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    if (name != null) {
                        welcomeText.setText("Добро пожаловать в приложение для оплаты коммунальных услуг, " + name + "!");
                    } else {
                        welcomeText.setText("Добро пожаловать в приложение для оплаты коммунальных услуг!");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    welcomeText.setText("Добро пожаловать в приложение для оплаты коммунальных услуг!");
                }
            });
        } else {
            welcomeText.setText("Добро пожаловать в приложение для оплаты коммунальных услуг!");
        }
    }

    private void setFeaturesList() {
        String features = "Здесь вы можете:\n" +
                "• Вводить показания счетчиков\n" +
                "• Оплачивать коммунальные услуги онлайн\n" +
                "• Просматривать историю платежей\n" +
                "• Следить за статистикой расходов\n" +
                "• Управлять своим профилем";
        featuresText.setText(features);
    }

    private void loadUserStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            statsText.setText("Статистика недоступна");
            return;
        }
        String uid = user.getUid();
        DatabaseReference statsRef = FirebaseDatabase.getInstance().getReference("user_stats").child(uid);
        statsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                long paymentsCount = snapshot.child("payments_count").getValue(Long.class) != null ?
                        snapshot.child("payments_count").getValue(Long.class) : 0;
                double paymentsSum = snapshot.child("payments_sum").getValue(Double.class) != null ?
                        snapshot.child("payments_sum").getValue(Double.class) : 0.0;
                boolean isTopCount = snapshot.child("is_top_count").getValue(Boolean.class) != null ?
                        snapshot.child("is_top_count").getValue(Boolean.class) : false;
                boolean isTopSum = snapshot.child("is_top_sum").getValue(Boolean.class) != null ?
                        snapshot.child("is_top_sum").getValue(Boolean.class) : false;

                StringBuilder stats = new StringBuilder();
                stats.append("Совершено платежей: ").append(paymentsCount).append("\n");
                stats.append("Общая сумма: ").append(String.format("%.2f ₽", paymentsSum)).append("\n");
                if (isTopCount) stats.append("Вы в топе по количеству платежей!\n");
                if (isTopSum) stats.append("Вы в топе по сумме платежей!\n");
                statsText.setText(stats.toString());
            } else {
                statsText.setText("Статистика недоступна");
            }
        });
    }

    private void setTip() {
        tipText.setText("Совет: Вносите показания вовремя, чтобы избежать переплат!");
    }

    private void startNotificationSchedule(View view) {
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                showNotification(view, "Напоминание: не забудьте внести показания или оплатить услуги!");
                notificationHandler.postDelayed(this, PERIOD_MS);
            }
        };
        notificationHandler.postDelayed(notificationRunnable, FIRST_DELAY_MS);
    }

    private void showNotification(View view, String message) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }
}
