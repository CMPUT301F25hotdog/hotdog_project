package com.hotdog.elotto.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.hotdog.elotto.R;
import com.hotdog.elotto.controller.EventCreationController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Activity responsible for handling user interaction during the event creation process. Then takes the
 * input data and sends it to the EventController class to be saved
 *
 */
public class EventCreationView extends AppCompatActivity {
    private EditText eventNameInput;
    private EditText eventDescriptionInput;
    private EditText timeInput;
    private EditText dateInput;
    private EditText openPeriodInput;
    private EditText closePeriodInput;
    private EditText entrantLimitInput;
    private EditText waitListSizeInput;
    private SwitchCompat geolocation;
    private EditText priceInput;
    private EditText locationInput;
    private Button cancelButton;
    private Button confirmButton;
    private ImageButton backButton;
    private ImageView bannerInput;
    private Uri selectedBannerUri;
    private EditText tags;
    private ArrayList<String> tagList = new ArrayList<>();
    /**
     * Initializes the activity and sets up UI event listeners.
     *
     * @param savedInstanceState the saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creation);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        backButton = findViewById(R.id.Back_button);
        bannerInput = findViewById(R.id.Event_Poster_Input);
        eventNameInput = findViewById(R.id.Event_Name_Input);
        eventDescriptionInput = findViewById(R.id.Event_Description_Input);
        timeInput = findViewById(R.id.Time_Input);
        dateInput = findViewById(R.id.Date_Input);
        openPeriodInput = findViewById(R.id.Open_Period_Input);
        closePeriodInput = findViewById(R.id.Close_Period_Input);
        entrantLimitInput = findViewById(R.id.Entrant_Limit_Input);
        waitListSizeInput = findViewById(R.id.Waitlist_Size_Input);
        geolocation = findViewById(R.id.Geolocation_Toggle);
        cancelButton = findViewById(R.id.Cancel_Creation_Button);
        confirmButton = findViewById(R.id.Confirm_Creation_Button);
        locationInput = findViewById(R.id.Event_Location_Input);
        priceInput = findViewById(R.id.Event_Price_Input);
        tags = findViewById(R.id.Tag_Input);


        EditText[] fields = {
                eventNameInput, eventDescriptionInput, timeInput, dateInput,
                openPeriodInput, closePeriodInput, entrantLimitInput, locationInput, priceInput
        };

        bannerInput.setOnClickListener(v -> openGallery());

        cancelButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
        tags.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String tag = tags.getText().toString().trim();
                    if (!tag.isEmpty()) {
                        tagList.add(tag);
                        tags.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
        confirmButton.setOnClickListener(v -> {
            if (!validateAllEditTexts(fields)) {
                return;
            }
            confirmationPass(tagList);
        });
    }

    /**
     * Validates all input fields, and gets all the strings then passes to the Controller
     */
    private void confirmationPass(ArrayList<String> tagList) {
        String eventName = eventNameInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String timeString = timeInput.getText().toString().trim();
        String dateString = dateInput.getText().toString().trim();
        String priceString = priceInput.getText().toString().trim();
        boolean hasError = false;

        double price = 0;
        priceString = priceString.replace("$", "");
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            priceInput.setError("Please enter a valid price");
            hasError = true;
        }

        String location = locationInput.getText().toString().trim();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy'T'HH:mm", Locale.getDefault());
        Date dateTime = null;
        try {
            String combined = dateString + "T" + timeString;
            dateTime = dateTimeFormat.parse(combined);
        } catch (ParseException e) {
            dateInput.setError("Please follow the format for Date");
            timeInput.setError("Please follow the format for Time");
            hasError = true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date openPeriodDate = null;
        Date closePeriodDate = null;

        try {
            openPeriodDate = dateFormat.parse(openPeriodInput.getText().toString().trim());
        } catch (ParseException e) {
            openPeriodInput.setError("Please follow the format for Date");
            hasError = true;
        }

        try {
            closePeriodDate = dateFormat.parse(closePeriodInput.getText().toString().trim());
        } catch (ParseException e) {
            closePeriodInput.setError("Please follow the format for Date");
            hasError = true;
        }

        int entrantLimit = 0;
        try {
            entrantLimit = Integer.parseInt(entrantLimitInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            entrantLimitInput.setError("Please enter a valid entrant limit");
            hasError = true;
        }

        int waitListSize = 0;
        String waitListSizeString = waitListSizeInput.getText().toString().trim();
        if (!waitListSizeString.isEmpty()) {
            try {
                waitListSize = Integer.parseInt(waitListSizeString);
            } catch (NumberFormatException e) {
                waitListSizeInput.setError("Please enter a valid waitlist size");
                hasError = true;
            }
        }

        boolean requireGeo = geolocation.isChecked();

        if (selectedBannerUri == null) {
            Toast.makeText(this, "Please select an event banner image", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(this, "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUser = getIntent().getStringExtra("CURRENT_USER_ID");
        EventCreationController controller = new EventCreationController(this);
        controller.EncodeImage(currentUser,eventName, eventDescription, dateTime, openPeriodDate, closePeriodDate,
                entrantLimit, waitListSize, location, price, requireGeo, selectedBannerUri,tagList);

        finish();
    }

    /**
     * Launcher for selecting an image from the device’s gallery, then the image is saved as a uri
     * and displayed for the user to see how it looks
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedBannerUri = uri;
                    bannerInput.setImageURI(uri);
                }
            });

    /**
     * Opens the device’s gallery to allow the user to pick a banner image, limits valid items to only
     * images
     */
    private void openGallery() {
        imagePickerLauncher.launch("image/*");
    }

    /**
     * Validates that all required EditTexts are filled in, if any EditTexts are empty
     * they are highlighted and the user is prompted to fill them in
     *
     * @param fields an array of EditTexts to validate.
     * @return true if all fields are filled, false otherwise.
     */
    private boolean validateAllEditTexts(EditText[] fields) {
        boolean allFilled = true;
        for (EditText field : fields) {
            String text = field.getText().toString().trim();
            if (text.isEmpty()) {
                allFilled = false;
                field.setBackgroundResource(R.drawable.edit_text_error);
            } else {
                field.setBackgroundResource(android.R.drawable.editbox_background);
            }
        }
        if (!allFilled) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
        }
        return allFilled;
    }
}
