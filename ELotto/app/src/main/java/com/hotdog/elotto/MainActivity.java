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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.hotdog.elotto.databinding.ActivityMainBinding;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.model.User;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            initAfterLogin();
                        } else {
                            // user cancelled login, handle if needed
                        }
                    });

    private User curUser;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        curUser = new User(getApplicationContext(), true);

        Log.d("USER NAME", curUser.getName());
        Log.d("CUR USER", "" + curUser);

        if (!curUser.exists()) {
            loginLauncher.launch(new Intent(this, LoginActivity.class));
        } else {
            initAfterLogin();
        }
    }

    private void initAfterLogin() {
        curUser.reload(true);

        if (curUser.getType() == UserType.Organizer) {
            Organizer org = new Organizer(getApplicationContext());
        }

        Log.d("USER EXISTS", "" + curUser.exists());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.eventHistoryFragment,
                R.id.navigation_calendar,
                R.id.navigation_my_events
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
