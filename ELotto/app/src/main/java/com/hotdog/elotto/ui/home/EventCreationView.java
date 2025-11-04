package com.hotdog.elotto.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.hotdog.elotto.R;
import com.hotdog.elotto.controller.EventCreationController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity responsible for handling user interaction during the event creation process.
 * <p>
 * {@code EventCreationView} allows users to:
 * <ul>
 *     <li>Input event details (name, description, date, location, etc.).</li>
 *     <li>Select a banner image from their device’s gallery.</li>
 *     <li>Toggle geolocation requirements for the event.</li>
 *     <li>Validate and submit data to {@link EventCreationController} for backend storage.</li>
 * </ul>
 *
 * Once all inputs are validated and the user confirms, the event is created and
 * the app transitions to the QR code view.
 */
public class EventCreationView extends AppCompatActivity {

    /** Input field for the event's name. */
    private EditText eventNameInput;

    /** Input field for the event's description. */
    private EditText eventDescriptionInput;

    /** Input field for the event time (HH:mm format). */
    private EditText timeInput;

    /** Input field for the event date (MM/dd/yyyy format). */
    private EditText dateInput;

    /** Input field for the registration open date. */
    private EditText openPeriodInput;

    /** Input field for the registration close date. */
    private EditText closePeriodInput;

    /** Input field for maximum allowed entrants. */
    private EditText entrantLimitInput;

    /** Input field for the size of the waitlist. */
    private EditText waitListSizeInput;

    /** Toggle switch for enabling or disabling geolocation requirement. */
    private SwitchCompat geolocation;

    /** Input field for event price. */
    private EditText priceInput;

    /** Input field for event location. */
    private EditText locationInput;

    /** Button to cancel event creation and close the view. */
    private Button cancelButton;

    /** Button to confirm event creation after validation. */
    private Button confirmButton;

    /** Navigation button to go back to the previous screen. */
    private ImageButton backButton;

    /** Image view for displaying and selecting the banner image. */
    private ImageView bannerInput;

    /** Stores the URI of the selected banner image. */
    private Uri selectedBannerUri;

    /**
     * Initializes the activity and sets up UI event listeners.
     *
     * @param savedInstanceState the saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creation);

        // UI component bindings
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

        EditText[] fields = {
                eventNameInput, eventDescriptionInput, timeInput, dateInput,
                openPeriodInput, closePeriodInput, entrantLimitInput, locationInput, priceInput
        };

        // Image picker setup
        bannerInput.setOnClickListener(v -> openGallery());

        // Cancel and navigation buttons
        cancelButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());

        // Confirmation handler
        confirmButton.setOnClickListener(v -> {
            if (!validateAllEditTexts(fields)) {
                return;
            }
            confirmationPass();
        });
    }

    /**
     * Validates all input fields, parses user input into valid data types,
     * and sends the data to {@link EventCreationController} to create the event.
     * <p>
     * If validation fails (e.g., incorrect formats, missing fields, or no image),
     * this method highlights invalid fields and displays a Toast message to the user.
     * </p>
     */
    private void confirmationPass() {
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
            priceInput.setBackgroundResource(R.drawable.edit_text_error);
            hasError = true;
        }

        String location = locationInput.getText().toString().trim();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy'T'HH:mm", Locale.getDefault());
        Date dateTime = null;
        try {
            String combined = dateString + "T" + timeString;
            dateTime = dateTimeFormat.parse(combined);
        } catch (ParseException e) {
            dateInput.setBackgroundResource(R.drawable.edit_text_error);
            timeInput.setBackgroundResource(R.drawable.edit_text_error);
            hasError = true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date openPeriodDate = null;
        Date closePeriodDate = null;

        try {
            openPeriodDate = dateFormat.parse(openPeriodInput.getText().toString().trim());
        } catch (ParseException e) {
            openPeriodInput.setBackgroundResource(R.drawable.edit_text_error);
            hasError = true;
        }

        try {
            closePeriodDate = dateFormat.parse(closePeriodInput.getText().toString().trim());
        } catch (ParseException e) {
            closePeriodInput.setBackgroundResource(R.drawable.edit_text_error);
            hasError = true;
        }

        int entrantLimit = 0;
        try {
            entrantLimit = Integer.parseInt(entrantLimitInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            entrantLimitInput.setBackgroundResource(R.drawable.edit_text_error);
            hasError = true;
        }

        int waitListSize = 0;
        String waitListSizeString = waitListSizeInput.getText().toString().trim();
        if (!waitListSizeString.isEmpty()) {
            try {
                waitListSize = Integer.parseInt(waitListSizeString);
            } catch (NumberFormatException e) {
                waitListSizeInput.setBackgroundResource(R.drawable.edit_text_error);
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

        EventCreationController controller = new EventCreationController(this);
        controller.CreateEvent(eventName, eventDescription, dateTime, openPeriodDate, closePeriodDate,
                entrantLimit, waitListSize, location, price, requireGeo, selectedBannerUri);

        finish();
    }

    /**
     * Launcher for selecting an image from the device’s gallery.
     * <p>
     * When an image is selected, its URI is stored and displayed in the {@code bannerInput}.
     * </p>
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedBannerUri = uri;
                    bannerInput.setImageURI(uri);
                }
            });

    /**
     * Opens the device’s gallery to allow the user to pick a banner image.
     */
    private void openGallery() {
        imagePickerLauncher.launch("image/*");
    }

    /**
     * Validates that all required {@link EditText} fields are filled.
     * <p>
     * Empty fields are highlighted, and a Toast message is displayed if any field is missing.
     * </p>
     *
     * @param fields an array of {@link EditText} inputs to validate.
     * @return {@code true} if all fields are filled; {@code false} otherwise.
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
