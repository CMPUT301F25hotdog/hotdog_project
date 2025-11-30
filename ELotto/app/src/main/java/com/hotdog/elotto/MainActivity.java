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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.hotdog.elotto.databinding.ActivityMainBinding;
import com.hotdog.elotto.helpers.UserStatus;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.ui.home.EventCreationView;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.ui.home.MyEventsView;
import com.hotdog.elotto.ui.home.OrganizerEventEntrantsFragment;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    /**
     * The current user of this app session.
     */
    private User curUser;

    private ActivityMainBinding binding;

    /**
     * Launcher to launch the login activity if the user has never logged in on this device before.
     */
    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // Make sure user object is up to date with any new information
                            curUser.atomicReload(() -> {
                                setLoading(false);
                                initAfterLogin(); // The user now exists and we can continue
                            });
                        } else {
                            // What to do if the user cancelled, whatever we want here
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        curUser = new User(getApplicationContext(), this::gotUser);
    }

    private void initAfterLogin() {

        if(curUser.getType()==UserType.Organizer){
            Organizer org = new Organizer(getApplicationContext());
        }

        Log.d("USER EXISTS", "" + curUser.exists());

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup bottom navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    /**
     * Set the main activity to show the loading screen
     * @param value Whether or not we are loading
     */
    private void setLoading(Boolean value) {
        ConstraintLayout loadingScreen = this.findViewById(R.id.MainActivityLoading);
        loadingScreen.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void gotUser(User user) {
        Log.d("USER NAME", user.getName());
        Log.d("USER STATUS", user.exists().toString());
        Log.d("CUR USER", ""+user);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setLoading(false);

        if (user.exists()!= UserStatus.Existent) {
            loginLauncher.launch(new Intent(this, LoginActivity.class));
        } else {
            initAfterLogin();
        }
    }
}