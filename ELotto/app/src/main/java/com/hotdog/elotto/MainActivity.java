package com.hotdog.elotto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.hotdog.elotto.databinding.ActivityMainBinding;
import com.hotdog.elotto.ui.home.EventCreationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup bottom navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        Button bruh = findViewById(R.id.bruh);
        bruh.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventCreationView.class);
            startActivity(intent);
        });
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }
}