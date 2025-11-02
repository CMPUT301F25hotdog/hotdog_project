package com.hotdog.elotto;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hotdog.elotto.databinding.ActivityMainBinding;
import com.hotdog.elotto.model.User;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        curUser = new User(getApplicationContext(), true);

        Log.d("USER NAME", curUser.getName());

        if (!curUser.exists()) {
            // Either simple finish() or clear the task so back won’t escape login
            loginLauncher.launch(new Intent(this, LoginActivity.class));
        }

        initAfterLogin();

        return;
    }

    private void initAfterLogin() {
        // Make sure user object is up to date with any new information
        curUser.reload();

        Log.d("USER_E", "" + curUser.exists());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}