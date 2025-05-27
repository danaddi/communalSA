package com.example.test_app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.test_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InputFragment extends Fragment {

    private EditText electricityInput, waterInput, monthInput;
    private TextView billTextView;
    private Button submitButton;

    private final double ELECTRICITY_RATE = 5.0; // тариф за электричество (руб/кВт·ч)
    private final double WATER_RATE = 30.0;     // тариф за воду (руб/м³)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, container, false);
        requireActivity().setTitle("Ввод");
        initViews(view);
        setupTextWatchers();
        setupSubmitButton();

        return view;
    }

    private void initViews(View view) {
        electricityInput = view.findViewById(R.id.editTextElectricity);
        waterInput = view.findViewById(R.id.editTextWater);
        monthInput = view.findViewById(R.id.editTextMonth);
        billTextView = view.findViewById(R.id.textViewBill);
        submitButton = view.findViewById(R.id.buttonSubmit);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateBill();
            }
        };

        electricityInput.addTextChangedListener(watcher);
        waterInput.addTextChangedListener(watcher);
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateInput()) {
                processAndTransferData();
            }
        });
    }

    private boolean validateInput() {
        String month = monthInput.getText().toString().trim();
        String electricityStr = electricityInput.getText().toString().trim();
        String waterStr = waterInput.getText().toString().trim();

        if (month.isEmpty() || electricityStr.isEmpty() || waterStr.isEmpty()) {
            showToast("Заполните все поля");
            return false;
        }

        try {
            Integer.parseInt(electricityStr);
            Integer.parseInt(waterStr);
            return true;
        } catch (NumberFormatException e) {
            showToast("Некорректные числовые данные");
            return false;
        }
    }

    private void processAndTransferData() {
        String month = monthInput.getText().toString().trim();
        int electricity = Integer.parseInt(electricityInput.getText().toString().trim());
        int water = Integer.parseInt(waterInput.getText().toString().trim());
        double bill = calculateBillAmount(electricity, water);

        Bundle args = new Bundle();
        args.putString("month", month);
        args.putInt("electricity", electricity);
        args.putInt("water", water);
        args.putDouble("bill", bill);

        navigateToPaymentFragment(args);
    }

    private double calculateBillAmount(int electricity, int water) {
        return (electricity * ELECTRICITY_RATE) + (water * WATER_RATE);
    }

    private void navigateToPaymentFragment(Bundle args) {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_payment);

        PaymentFragment paymentFragment = new PaymentFragment();
        paymentFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, paymentFragment)
                .addToBackStack(null)
                .commit();
    }

    private void calculateBill() {
        try {
            int electricity = electricityInput.getText().toString().isEmpty() ? 0
                    : Integer.parseInt(electricityInput.getText().toString());
            int water = waterInput.getText().toString().isEmpty() ? 0
                    : Integer.parseInt(waterInput.getText().toString());

            double bill = calculateBillAmount(electricity, water);
            updateBillText(bill);
        } catch (NumberFormatException e) {
            updateBillText(0);
        }
    }

    private void updateBillText(double amount) {
        billTextView.setText(String.format("Сумма к оплате: %.2f ₽", amount));
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}