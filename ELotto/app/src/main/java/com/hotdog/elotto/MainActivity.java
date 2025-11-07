package com.hotdog.elotto;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.hotdog.elotto.databinding.ActivityMainBinding;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.ui.home.EventCreationView;
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
        Log.d("CUR USER", ""+curUser);

        if (!curUser.exists()) {
            // Either simple finish() or clear the task so back won’t escape login
            loginLauncher.launch(new Intent(this, LoginActivity.class));
        } else {
            initAfterLogin();
        }

        return;
    }

    private void initAfterLogin() {
        // Make sure user object is up to date with any new information
        curUser.reload(true);

        if(curUser.getType()==UserType.Organizer){
            Organizer org = new Organizer(getApplicationContext());
        }

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
        Button bruh = findViewById(R.id.CreateEventButton);
        bruh.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventCreationView.class);
            intent.putExtra("CURRENT_USER_ID", curUser.getId());
            startActivity(intent);
        });
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }
}