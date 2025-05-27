package com.example.test_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.test_app.fragments.HomeFragment;
import com.example.test_app.fragments.InputFragment;
import com.example.test_app.fragments.PaymentFragment;
import com.example.test_app.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Устанавливаем Toolbar как ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = getString(R.string.app_name); // По умолчанию

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Главная";
            } else if (itemId == R.id.nav_input) {
                selectedFragment = new InputFragment();
                title = "Ввод";
            } else if (itemId == R.id.nav_payment) {
                selectedFragment = new PaymentFragment();
                title = "Оплата";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Профиль";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                // Используем ActionBar для смены заголовка
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }

            return false;
        });

        // Фрагмент по умолчанию
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Главная");
        }
    }
}
