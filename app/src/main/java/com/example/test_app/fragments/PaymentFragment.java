package com.example.test_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.test_app.R;
import com.example.test_app.models.UtilityData;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaymentFragment extends Fragment {

    private TextView statusView, amountView, detailsView, statsTextView;
    private Button payButton;
    private ProgressBar progressBar;
    private LinearLayout historyContainer;
    private final List<UtilityData> paymentHistory = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        requireActivity().setTitle("Оплата");
        FirebaseApp.initializeApp(requireContext());

        mAuth = FirebaseAuth.getInstance();

        statusView = view.findViewById(R.id.status_text);
        amountView = view.findViewById(R.id.amount_text);
        detailsView = view.findViewById(R.id.details_text);
        payButton = view.findViewById(R.id.pay_button);
        progressBar = view.findViewById(R.id.progress_bar);
        statsTextView = view.findViewById(R.id.stats_text);
        historyContainer = view.findViewById(R.id.history_container);


        ensureAuthenticatedAndLoadData();

        return view;
    }

    private void ensureAuthenticatedAndLoadData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mAuth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadPaymentHistory();
                    setupFragmentData();
                } else {
                    statusView.setText("Ошибка аутентификации");
                }
            });
        } else {
            loadPaymentHistory();
            setupFragmentData();
        }
    }

    private void setupFragmentData() {
        Bundle args = getArguments();
        if (args != null) {
            String month = args.getString("month", "");
            int electricity = args.getInt("electricity", 0);
            int water = args.getInt("water", 0);
            double bill = args.getDouble("bill", 0.0);

            updatePaymentInfo(month, electricity, water, bill);

            if (isAlreadyPaid(month)) {
                statusView.setText(getString(R.string.payment_status) + month);
                statusView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                payButton.setEnabled(false);
            } else {
                payButton.setEnabled(true);
                payButton.setOnClickListener(v -> processPayment(bill, month, electricity, water));
            }
        }
    }

    private void updatePaymentInfo(String month, int electricity, int water, double bill) {
        amountView.setText(String.format("К оплате: %.2f ₽", bill));
        detailsView.setText(String.format("Период: %s\nЭлектричество: %d кВт·ч\nВода: %d м³", month, electricity, water));
    }

    private void processPayment(double amount, String month, int electricity, int water) {
        payButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        statusView.setText("Обработка платежа...");

        new Handler().postDelayed(() -> {
            if (!isAdded()) return; // Фрагмент отсоединён — ничего не делаем

            boolean isSuccess = new Random().nextInt(10) < 8;

            if (isSuccess) {
                statusView.setText("Оплачено успешно!");
                Context context = getContext();
                if (context != null) {
                    statusView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("payments")
                            .child(uid);

                    UtilityData data = new UtilityData(
                            uid, month, electricity, water, amount, true
                    );

                    userRef.push().setValue(data).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FIREBASE", "Платёж сохранён");
                        } else {
                            Log.e("FIREBASE", "Ошибка сохранения", task.getException());
                        }
                    });

                    paymentHistory.add(0, data);
                    updateStatistics();
                }
            } else {
                statusView.setText("Ошибка оплаты!");
                Context context = getContext();
                if (context != null) {
                    statusView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }
            }

            progressBar.setVisibility(View.GONE);
            payButton.setEnabled(true);

        }, 2000);
    }


    private boolean isAlreadyPaid(String month) {
        for (UtilityData data : paymentHistory) {
            if (data.getMonth().equalsIgnoreCase(month) && data.isPaid()) {
                return true;
            }
        }
        return false;
    }

    private void updateStatistics() {
        if (paymentHistory.isEmpty()) {
            statsTextView.setText("Нет данных для статистики");
            return;
        }

        double total = 0;
        int count = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (UtilityData data : paymentHistory) {
            if (data.isPaid()) {
                double bill = data.getBill();
                total += bill;
                max = Math.max(max, bill);
                min = Math.min(min, bill);
                count++;
            }
        }

        if (count == 0) {
            statsTextView.setText("Нет оплаченных счетов");
        } else {
            statsTextView.setText(String.format(
                    "Статистика:\n\n" +
                            "• Платежей: %d\n" +
                            "• Средний: %.2f ₽\n" +
                            "• Макс: %.2f ₽\n" +
                            "• Мин: %.2f ₽",
                    count, total / count, max, min));
        }
    }

    private void loadPaymentHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("payments").child(uid);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentHistory.clear();
                historyContainer.removeAllViews();

                for (DataSnapshot data : snapshot.getChildren()) {
                    UtilityData entry = data.getValue(UtilityData.class);
                    if (entry != null) {
                        paymentHistory.add(0, entry);
                        addPaymentView(entry);
                    }
                }
                updateStatistics();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusView.setText("Ошибка загрузки истории: " + error.getMessage());
            }
        });
    }

    private void addPaymentView(UtilityData data) {
        Context context = getContext();
        if (context == null) return;

        View itemView = LayoutInflater.from(context).inflate(R.layout.item_payment_history, historyContainer, false);

        TextView monthText = itemView.findViewById(R.id.tvMonth);
        TextView electricityText = itemView.findViewById(R.id.tvElectricity);
        TextView waterText = itemView.findViewById(R.id.tvWater);
        TextView billText = itemView.findViewById(R.id.tvBill);
        TextView statusText = itemView.findViewById(R.id.tvStatus);
        Button btnDelete = itemView.findViewById(R.id.delete_button);

        monthText.setText(data.getMonth());
        electricityText.setText("Электричество: " + data.getElectricity() + " кВт·ч");
        waterText.setText("Вода: " + data.getWater() + " м³");
        billText.setText("Сумма: " + data.getBill() + " ₽");
        statusText.setText(data.isPaid() ? "Оплачено" : "Ожидает оплаты");

        statusText.setTextColor(ContextCompat.getColor(context,
                data.isPaid() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        // Функция удаления
        btnDelete.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("payments")
                        .child(uid);

                // Найдём запись в базе, сравнивая поля
                userRef.orderByChild("month").equalTo(data.getMonth())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                                    UtilityData entry = entrySnapshot.getValue(UtilityData.class);
                                    if (entry != null &&
                                            entry.getElectricity() == data.getElectricity() &&
                                            entry.getWater() == data.getWater() &&
                                            entry.getBill() == data.getBill()) {

                                        entrySnapshot.getRef().removeValue()
                                                .addOnSuccessListener(unused -> {
                                                    historyContainer.removeView(itemView);
                                                    paymentHistory.remove(data);
                                                    updateStatistics();
                                                });
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("DELETE", "Ошибка при удалении: " + error.getMessage());
                            }
                        });
            }
        });
        historyContainer.addView(itemView);
    }


}
