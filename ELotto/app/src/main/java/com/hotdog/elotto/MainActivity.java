package com.hotdog.elotto;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hotdog.elotto.databinding.ActivityMainBinding;
import com.hotdog.elotto.ui.AdminDashboardActivity;
import com.hotdog.elotto.ui.home.EventCreationView;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.helpers.UserType;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    /**
     * ADMIN DEVICE ID - Hardcoded device ID for the admin device
     */
    private static final String ADMIN_DEVICE_ID = "39a4f2a6299a89c5";

    /**
     * Launcher to launch the login activity if the user has never logged in on this device before.
     */
    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            initAfterLogin(); // The user now exists and we can continue
                        } else {
                            // What to do if the user cancelled, whatever we want here
                        }
                    });

    /**
     * The current user of this app session.
     */
    private User curUser;

    private ActivityMainBinding binding;

    /**
     * Admin Floating Action Button
     */
    private FloatingActionButton adminFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        curUser = new User(getApplicationContext(), true);

        Log.d("USER NAME", curUser.getName());
        Log.d("DEVICE_ID", "Current Device ID: " + curUser.getId());

        if (!curUser.exists()) {
            // Either simple finish() or clear the task so back won't escape login
            loginLauncher.launch(new Intent(this, LoginActivity.class));
        } else {
            initAfterLogin();
        }

        return;
    }

    private void initAfterLogin() {
        // User is already loaded atomically in onCreate, no need to reload

        Log.d("USER EXISTS", "" + curUser.exists());

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

        // Initialize Admin FAB
        adminFab = findViewById(R.id.admin_fab);

        // Check if current device is admin device
        checkAdminAccess();
    }

    /**
     * Checks if the current device is the admin device and shows/hides admin button accordingly
     */
    private void checkAdminAccess() {
        String currentDeviceId = curUser.getId();

        Log.d("ADMIN_CHECK", "Current Device ID: " + currentDeviceId);
        Log.d("ADMIN_CHECK", "Admin Device ID: " + ADMIN_DEVICE_ID);

        // Check if current device matches admin device ID
        if (currentDeviceId != null && currentDeviceId.equals(ADMIN_DEVICE_ID)) {
            Log.d("ADMIN_CHECK", "Admin device detected! Showing admin button.");

            // Show admin FAB
            adminFab.setVisibility(View.VISIBLE);

            // Set click listener to open admin dashboard
            adminFab.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            });

            // Optionally update user type to Admin if not already set
            if (curUser.getType() != UserType.Administrator) {
                curUser.updateType(UserType.Administrator);
            }
        } else {
            Log.d("ADMIN_CHECK", "Regular user device. Hiding admin button.");
            // Hide admin FAB for non-admin users
            adminFab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check admin access when returning to this activity
        if (adminFab != null) {
            checkAdminAccess();
        }
    }
}