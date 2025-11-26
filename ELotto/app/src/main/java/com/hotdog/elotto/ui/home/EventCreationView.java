package com.hotdog.elotto.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.EventCreationController;
import com.hotdog.elotto.repository.EventRepository;


import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;

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
    private Button deleteButton;  // ← ADD THIS LINE
    private ImageButton backButton;
    private ImageView bannerInput;
    private Uri selectedBannerUri;
    private EditText tags;
    private ArrayList<String> tagList = new ArrayList<>();

    private String currentMode;  // ← ADD THIS
    private String currentEventId;  // ← ADD THIS

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
        deleteButton = findViewById(R.id.Delete_Event_Button);  // ← ADD THIS LINE
        locationInput = findViewById(R.id.Event_Location_Input);
        priceInput = findViewById(R.id.Event_Price_Input);
        tags = findViewById(R.id.Tag_Input);


        // ← ADD THIS ENTIRE BLOCK HERE ↓

        currentMode = getIntent().getStringExtra("MODE");
        currentEventId = getIntent().getStringExtra("EVENT_ID");

        if ("EDIT".equals(currentMode) && currentEventId != null) {
            // Edit mode
            TextView headerText = findViewById(R.id.Create_Event_Header);
            headerText.setText("Edit Event");
            confirmButton.setText("Save Changes");
            deleteButton.setVisibility(View.VISIBLE);

            // TODO: Load event data (we'll implement this next)
            loadEventData(currentEventId);
        } else {
            // Create mode (default)
            deleteButton.setVisibility(View.GONE);
        }


        EditText[] fields = {
                eventNameInput, eventDescriptionInput, timeInput, dateInput,
                openPeriodInput, closePeriodInput, entrantLimitInput, locationInput, priceInput
        };

        bannerInput.setOnClickListener(v -> openGallery());

        cancelButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());

        // ← ADD THIS ENTIRE BLOCK HERE ↓
        deleteButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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
     * @param tagList, a list of all tags for the event
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
        EventCreationController controller = new EventCreationController(this);
        String encodedString = controller.EncodeImage(selectedBannerUri);
        int maxFirestoreSize = 900000;
        int encodedSize = encodedString.getBytes().length;
        if (encodedSize > maxFirestoreSize) {
            Toast.makeText(this, "Image too large! Please choose a smaller image.", Toast.LENGTH_LONG).show();
            Log.e("EventCreationView", "Encoded image size: " + encodedSize + " bytes (too large for Firestore)");
            return;
        }
        if ("EDIT".equals(currentMode) && currentEventId != null) {
            // Update existing event
            controller.UpdateEvent(currentEventId, eventName, eventDescription, dateTime, openPeriodDate,
                    closePeriodDate, entrantLimit, waitListSize, location, price, requireGeo, encodedString, tagList);
        } else {
            // Create new event
            controller.SaveEvent(eventName, eventDescription, dateTime, openPeriodDate, closePeriodDate,
                    entrantLimit, waitListSize, location, price, requireGeo, encodedString, tagList);
        }

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

    /**
     * Loads event data and pre-fills all fields for editing
     */
    private void loadEventData(String eventId) {
        EventRepository eventRepository = new EventRepository();
        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                // Set event name
                eventNameInput.setText(event.getName());

                // Set event description
                eventDescriptionInput.setText(event.getDescription());

                // Set event location
                if (event.getLocation() != null) {
                    locationInput.setText(event.getLocation());
                }

                // Set event price
                priceInput.setText(String.valueOf(event.getPrice()));

                // Set event date and time
                if (event.getEventDateTime() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    dateInput.setText(dateFormat.format(event.getEventDateTime()));
                    timeInput.setText(timeFormat.format(event.getEventDateTime()));
                }

                // Set registration period
                if (event.getRegistrationStartDate() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    openPeriodInput.setText(dateFormat.format(event.getRegistrationStartDate()));
                }

                if (event.getRegistrationEndDate() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    closePeriodInput.setText(dateFormat.format(event.getRegistrationEndDate()));
                }

                // Set max entrants
                entrantLimitInput.setText(String.valueOf(event.getMaxEntrants()));

                if (event.getWaitlistLimit() != null && event.getWaitlistLimit() > 0) {
                    waitListSizeInput.setText(String.valueOf(event.getWaitlistLimit()));
                }

                // Set geolocation
                geolocation.setChecked(event.isGeolocationRequired());

                // Set event poster image
                // Set event poster image
                if (event.getPosterImageUrl() != null && !event.getPosterImageUrl().isEmpty()) {
                    try {
                        String encodedImage = event.getPosterImageUrl();
                        byte[] decodedBytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        bannerInput.setImageBitmap(bitmap);
                        // Don't set selectedBannerUri - we loaded from existing data
                    } catch (Exception e) {
                        Log.e("EventCreationView", "Error decoding poster image: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EventCreationView.this, "Error loading event: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }


        });
    }

    /**
     * Deletes the current event
     */
    private void deleteEvent() {
        if (currentEventId == null) {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        EventRepository eventRepository = new EventRepository();
        eventRepository.deleteEvent(currentEventId, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventCreationView.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EventCreationView.this, "Error deleting event: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
