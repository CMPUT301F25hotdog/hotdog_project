package com.hotdog.elotto;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.User;

import java.util.HashMap;
import java.util.Map;


/**
 * Activity for handling user login.
 * @author Layne Pitman
 * @version 1.0.0
 */
public class LoginActivity extends AppCompatActivity {

    EditText inputName, inputEmail, inputPhone;
    Button loginButton;
    Map<String, String> inputs = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputName = findViewById(R.id.login_name);
        inputEmail = findViewById(R.id.login_email);
        inputPhone = findViewById(R.id.login_phone);
        loginButton = findViewById(R.id.continueLoginButton);

        loginButton.setOnClickListener(v -> {
            setLoading(true);
            inputs.put("name", inputName.getText().toString().trim());
            inputs.put("email", inputEmail.getText().toString().trim());
            inputs.put("phone", inputPhone.getText().toString().trim().replaceAll("[()\\- /.+#]", ""));

            // basic required validation
            if (inputs.get("name")==null || inputs.get("name").isEmpty()) {
                inputName.setError("Name required");
                setLoading(false);
                return;
            }
            if (inputs.get("email")==null || inputs.get("email").isEmpty()) {
                inputEmail.setError("Email required");
                setLoading(false);
                return;
            }
            if (inputs.get("phone")==null || !(inputs.get("phone").length()==10 || inputs.get("phone").isEmpty())) {
                inputPhone.setError("Phone number must be 10 digits");
                setLoading(false);
                return;
            }

            // Get
            User user = new User(getApplicationContext(), this::finished);

            setResult(RESULT_OK);
        });
    }

    /**
     * Sets whether the login screen is loading and subsequently shows or hides the progress bar.
     * @param value Whether or not we are loading.
     */
    private void setLoading(boolean value) {
        Button loginBtn = findViewById(R.id.continueLoginButton);
        ProgressBar loadingBar = findViewById(R.id.loginLoadingWheel);
        if(value) {
            loginBtn.setText("");
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            loginBtn.setText("Continue");
            loadingBar.setVisibility(View.GONE);
        }
    }

    /**
     * A function to start the finishing sequence after we have received a login response.
     */
    private void finished(User user) {
        this.setLoading(false);

        user.updateEmail(inputs.get("email"));
        user.updateName(inputs.get("name"));
        user.updatePhone(inputs.get("phone"));
        user.updateType(UserType.Entrant);
        Log.d("USER TYPE", user.getType().toString());

        Log.d("LOGIN USER", user.getName());
        finish();
    }
}
