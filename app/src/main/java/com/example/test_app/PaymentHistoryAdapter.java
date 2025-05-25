package com.example.test_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_app.models.UtilityData;
import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {

    private final List<UtilityData> dataList;
    private final List<String> paymentKeys;
    private final OnPaymentDeleteListener deleteListener;

    public PaymentHistoryAdapter(List<UtilityData> dataList, List<String> paymentKeys, OnPaymentDeleteListener listener) {
        this.dataList = dataList;
        this.paymentKeys = paymentKeys;
        this.deleteListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView monthText, electricityText, waterText, billText, statusText;
        Button deleteButton;

        public ViewHolder(View view) {
            super(view);
            monthText = view.findViewById(R.id.tvMonth);
            electricityText = view.findViewById(R.id.tvElectricity);
            waterText = view.findViewById(R.id.tvWater);
            billText = view.findViewById(R.id.tvBill);
            statusText = view.findViewById(R.id.tvStatus);
            deleteButton = view.findViewById(R.id.delete_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UtilityData item = dataList.get(position);

        holder.monthText.setText(item.getMonth());
        holder.electricityText.setText("Электричество: " + item.getElectricity() + " кВт·ч");
        holder.waterText.setText("Вода: " + item.getWater() + " м³");
        holder.billText.setText("Сумма: " + item.getBill() + " ₽");
        holder.statusText.setText(item.isPaid() ? "Оплачено" : "Ожидает оплаты");
        holder.statusText.setTextColor(item.isPaid() ? 0xFF4CAF50 : 0xFFF44336); // зеленый или красный

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item, paymentKeys.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface OnPaymentDeleteListener {
        void onDelete(UtilityData payment, String firebaseKey);
    }
}
