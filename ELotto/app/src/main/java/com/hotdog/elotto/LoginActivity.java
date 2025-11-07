package com.hotdog.elotto;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.User;


/**
 * Activity for handling user login.
 * @author Layne Pitman
 * @version 1.0.0
 */
public class LoginActivity extends AppCompatActivity {

    EditText inputName, inputEmail, inputPhone;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputName = findViewById(R.id.login_name);
        inputEmail = findViewById(R.id.login_email);
        inputPhone = findViewById(R.id.login_phone);
        btnSubmit  = findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim().replaceAll("[()\\- /.+#]", "");

            // basic required validation
            if (name.isEmpty()) {
                inputName.setError("Name required");
                return;
            }
            if (email.isEmpty()) {
                inputEmail.setError("Email required");
                return;
            }
            if (!(phone.length()==10 || phone.isEmpty())) {
                inputPhone.setError("Phone number must be 10 digits");
                return;
            }

            User user = new User(getApplicationContext(), true);

            user.updateEmail(email);
            user.updateName(name);
            user.updatePhone(phone);
            user.updateType(UserType.Entrant);
            Log.d("USER TYPE", user.getType().toString());

            Log.d("LOGIN USER", user.getName());

            setResult(RESULT_OK);

            setResult(RESULT_OK);

            finish();
        });
    }
}
