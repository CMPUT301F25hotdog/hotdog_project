package com.hotdog.elotto.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.EventCreationController;
import com.hotdog.elotto.repository.EventRepository;

import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.model.Event;
import com.google.android.libraries.places.api.*;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Activity responsible for handling user interaction during the event creation
 * process. Then takes the
 * input data and sends it to the EventController class to be saved
 *
 */
public class EventCreationView extends AppCompatActivity {
    private TextInputEditText eventNameInput;
    private TextInputLayout eventNameLayout;
    private EditText eventDescriptionInput;
    private TextInputEditText eventTimeInput;
    private TextInputLayout eventTimeLayout;
    private TextInputLayout eventDateLayout;
    private TextInputEditText eventDateInput;
    private TextInputLayout openPeriodLayout;
    private TextInputEditText openPeriodInput;
    private TextInputLayout closePeriodLayout;
    private TextInputEditText closePeriodInput;
    private TextInputEditText maxEntrantInput;
    private TextInputLayout maxEntrantLayout;
    private EditText waitListSizeInput;
    private SwitchCompat geolocation;
    private TextInputEditText eventPriceInput;
    private TextInputLayout eventPriceLayout;
    private TextInputLayout locationLayout;
    private AutoCompleteTextView locationInput;
    private Button cancelButton;
    private Button confirmButton;
    private Button deleteButton; // ← ADD THIS LINE
    private ImageButton backButton;
    private ImageView bannerInput;
    private Uri selectedBannerUri;
    private Button tags;
    private ArrayList<String> tagList = new ArrayList<>();

    private String currentMode; // ← ADD THIS
    private String currentEventId; // ← ADD THIS

    // For location shtuff
    private PlacesClient client;
    private AutocompleteSessionToken token;
    private PlaceAutoSuggestAdapter placeAutoSuggestAdapter;

    private static class DecimalInputFilter implements InputFilter {
        private final Pattern inPattern;

        public DecimalInputFilter(int digitsAfterZero) {
            // Ballin i love regex muah regex my baby I love you
            inPattern = Pattern.compile("[0-9]*+((\\.[0-9]{0," + digitsAfterZero + "})?)");
        }

        @Override
        public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
            String result = dst.subSequence(0, dstart) + src.toString() + dst.subSequence(dend, dst.length());
            return inPattern.matcher(result).matches() ? null : "";
        }
    }

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
        eventNameInput = findViewById(R.id.EventNameInput);
        eventNameLayout = findViewById(R.id.EventNameLayout);
        eventDescriptionInput = findViewById(R.id.Event_Description_Input);
        eventTimeInput = findViewById(R.id.EventTimeInput);
        eventTimeLayout = findViewById(R.id.EventTimeLayout);
        openPeriodLayout = findViewById(R.id.EventOpensLayout);
        openPeriodInput = findViewById(R.id.EventOpensSelector);
        closePeriodLayout = findViewById(R.id.EventClosesLayout);
        closePeriodInput = findViewById(R.id.EventClosesSelector);
        eventDateLayout = findViewById(R.id.EventDateLayout);
        eventDateInput = findViewById(R.id.EventDateInput);
        maxEntrantInput = findViewById(R.id.MaxEntrantInput);
        maxEntrantLayout = findViewById(R.id.MaxEntrantLayout);
        waitListSizeInput = findViewById(R.id.WaitlistSizeInput);
        geolocation = findViewById(R.id.Geolocation_Toggle);
        cancelButton = findViewById(R.id.Cancel_Creation_Button);
        confirmButton = findViewById(R.id.Confirm_Creation_Button);
        deleteButton = findViewById(R.id.Delete_Event_Button); // ← ADD THIS LINE
        locationLayout = findViewById(R.id.EventAddressLayout);
        locationInput = findViewById(R.id.EventAddressInput);
        eventPriceInput = findViewById(R.id.EventPriceInput);
        eventPriceLayout = findViewById(R.id.EventPriceLayout);
        tags = findViewById(R.id.Tag_Button);

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
                eventDescriptionInput,
        };

        TextInputLayout[] layouts = {
                eventTimeLayout, eventDateLayout, locationLayout,
                closePeriodLayout, openPeriodLayout, eventNameLayout
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

        String[] tag_options = getResources().getStringArray(R.array.tag_options);
        boolean[] selected = new boolean[tag_options.length];
        tags.setOnClickListener(v -> {
            boolean[] tempSelected = selected.clone();
            new AlertDialog.Builder(this)
                    .setTitle("Select Tags")
                    .setMultiChoiceItems(tag_options, tempSelected, (dialog, position, isChecked) -> {
                        tempSelected[position] = isChecked;
                    })
                    .setPositiveButton("OK", (dialog, position) -> {
                        System.arraycopy(tempSelected, 0, selected, 0, tag_options.length);
                        tagList.clear();
                        for (int i = 0; i < tag_options.length; i++) {
                            if (selected[i])
                                tagList.add(tag_options[i]);
                        }
                        if (tagList.isEmpty()) {
                            tags.setText("Select Event Tags");
                            return;
                        }
                        tags.setText(String.join(", ", tagList));
                    })
                    .setNegativeButton("Cancel", (dialog, position) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
        confirmButton.setOnClickListener(v -> {
            if (!validateAllEditTexts(fields, layouts)) {
                return;
            }
            confirmationPass(tagList);
        });

        // Calendar Dialogs with Constraints

        // Helper to parse date from string
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        View.OnClickListener openOpen = v -> {
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            ArrayList<CalendarConstraints.DateValidator> validators = new ArrayList<>();

            validators.add(DateValidatorPointForward.now());

            // Constraint: Opening date must be before Closing Date (if set)
            String closeDateString = closePeriodInput.getText().toString().trim();
            if (!closeDateString.isEmpty()) {
                try {
                    Date closeDate = dateFormat.parse(closeDateString);
                    if (closeDate != null) {
                        validators.add(DateValidatorPointBackward.before(closeDate.getTime()));
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            // Constraint: Opening date must be before Event Date (if set)
            String eventDateString = eventDateInput.getText().toString().trim();
            if (!eventDateString.isEmpty()) {
                try {
                    Date eventDate = dateFormat.parse(eventDateString);
                    if (eventDate != null) {
                        validators.add(DateValidatorPointBackward.before(eventDate.getTime()));
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));

            MaterialDatePicker<Long> openDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Opening Date")
                    .setSelection(today)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            openDatePicker.addOnPositiveButtonClickListener(selectionMillis -> {
                Date date = new Date(selectionMillis);
                openPeriodInput.setText(dateFormat.format(date));
            });
            openDatePicker.show(getSupportFragmentManager(), "DATE_OPEN");
        };
        openPeriodInput.setOnClickListener(openOpen);
        openPeriodLayout.setOnClickListener(openOpen);

        View.OnClickListener closeOpen = v -> {
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            ArrayList<CalendarConstraints.DateValidator> validators = new ArrayList<>();

            validators.add(DateValidatorPointForward.now());

            // Constraint: Closing date must be after Opening Date (if set)
            String openDateString = openPeriodInput.getText().toString().trim();
            if (!openDateString.isEmpty()) {
                try {
                    Date openDate = dateFormat.parse(openDateString);
                    if (openDate != null) {
                        validators.add(DateValidatorPointForward.from(openDate.getTime()));
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            // Constraint: Closing date must be before Event Date (if set)
            String eventDateString = eventDateInput.getText().toString().trim();
            if (!eventDateString.isEmpty()) {
                try {
                    Date eventDate = dateFormat.parse(eventDateString);
                    if (eventDate != null) {
                        validators.add(DateValidatorPointBackward.before(eventDate.getTime()));
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            if (!validators.isEmpty()) {
                constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));
            }

            MaterialDatePicker<Long> closeDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Closing Date")
                    .setSelection(today)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            closeDatePicker.addOnPositiveButtonClickListener(selectionMillis -> {
                Date date = new Date(selectionMillis);
                closePeriodInput.setText(dateFormat.format(date));
            });
            closeDatePicker.show(getSupportFragmentManager(), "DATE_CLOSE");
        };
        closePeriodInput.setOnClickListener(closeOpen);
        closePeriodLayout.setOnClickListener(closeOpen);

        View.OnClickListener dateOpen = v -> {
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            ArrayList<CalendarConstraints.DateValidator> validators = new ArrayList<>();

            validators.add(DateValidatorPointForward.now());

            // Constraint: Event Date must be after Opening Date (if set)
            String openDateString = openPeriodInput.getText().toString().trim();
            if (!openDateString.isEmpty()) {
                try {
                    Date openDate = dateFormat.parse(openDateString);
                    if (openDate != null) {
                        validators.add(DateValidatorPointForward.from(openDate.getTime()));
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            // Constraint: Event Date must be after Closing Date (if set)
            String closeDateString = closePeriodInput.getText().toString().trim();
            if (!closeDateString.isEmpty()) {
                try {
                    Date closeDate = dateFormat.parse(closeDateString);
                    if (closeDate != null) {
                        validators.add(DateValidatorPointForward.from(closeDate.getTime()));
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));

            MaterialDatePicker<Long> eventDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .setSelection(today)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            eventDatePicker.addOnPositiveButtonClickListener(selectionMillis -> {
                Date date = new Date(selectionMillis);
                eventDateInput.setText(dateFormat.format(date));
            });
            eventDatePicker.show(getSupportFragmentManager(), "DATE_EVENT");
        };
        eventDateInput.setOnClickListener(dateOpen);
        eventDateLayout.setOnClickListener(dateOpen);

        // Time picker!
        MaterialTimePicker eventTimePicker = new MaterialTimePicker.Builder()
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Event Time")
                .build();
        View.OnClickListener timeOpen = v -> eventTimePicker.show(getSupportFragmentManager(), "TIME");
        eventTimeInput.setOnClickListener(timeOpen);
        eventTimeLayout.setOnClickListener(timeOpen);

        eventTimePicker.addOnPositiveButtonClickListener(v -> {
            int hour = eventTimePicker.getHour();
            int minute = eventTimePicker.getMinute();

            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            eventTimeInput.setText(formattedTime);
        });

        // Money Man
        eventPriceInput.setFilters(new InputFilter[] { new DecimalInputFilter(2) });
        eventPriceInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                return;
            TextInputEditText editText = (TextInputEditText) v;
            Editable editable = editText.getText();
            if (editable == null)
                return;
            String raw = editable.toString().trim();
            if (raw.isEmpty()) {
                return;
            }
            raw = raw.replaceFirst("^0+(?=[0-9])", "");
            if (raw.startsWith(".")) {
                raw = "0" + raw;
            }
            if (raw.endsWith(".")) {
                raw = raw.substring(0, raw.length() - 1);
            }
            if (!raw.equals(editable.toString())) {
                editText.setText(raw);
            }
        });

        // Max Entrant time
        View.OnFocusChangeListener maxEntrantListener = (v, hasFocus) -> {
            if (hasFocus)
                return;
            int entrantLimit = 0;
            try {
                entrantLimit = Integer
                        .parseInt(maxEntrantInput.getText() == null ? "" : maxEntrantInput.getText().toString().trim());
                if (entrantLimit <= 0)
                    throw new NumberFormatException("Negative");
                maxEntrantInput.setError(null);
            } catch (NumberFormatException e) {
                if (e.getMessage() != null && e.getMessage().equals("Negative"))
                    maxEntrantInput.setError("Max Entrants Must Be Positive");
                else
                    maxEntrantInput.setError("Max Entrants is Required");
            }
        };
        maxEntrantInput.setOnFocusChangeListener(maxEntrantListener);

        // Location time!
        this.initPlaces("AIzaSyB1WXzUjkY-JxcAhppv5wCJ8kH81lbwpME");

    }

    private void initPlaces(String apiKey) {
        if (!Places.isInitialized())
            Places.initializeWithNewPlacesApiEnabled(this, apiKey);

        client = Places.createClient(this);
        token = AutocompleteSessionToken.newInstance();
        placeAutoSuggestAdapter = new PlaceAutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);

        locationInput.setAdapter(placeAutoSuggestAdapter);
        locationInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAddress = placeAutoSuggestAdapter.getItem(position);
            locationLayout.setError(null);
            locationInput.setText(selectedAddress);
        });

        // Event needs some address, but custom is allowed too
        locationInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = locationInput.getText().toString();
                if (input.isEmpty())
                    locationInput.setError("Address is required.");
                else
                    locationInput.setError(null);
            }
        });
    }

    private class PlaceAutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
        private List<String> resultList = new ArrayList<>();

        public PlaceAutoSuggestAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int position) {
            return resultList.get(position);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterRes = new FilterResults();
                    if (constraint != null) {
                        List<String> predictions = getPlacePredictions(constraint);
                        if (predictions != null) {
                            filterRes.values = predictions;
                            filterRes.count = predictions.size();
                        }
                    }
                    return filterRes;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        resultList = (List<String>) results.values;
                        notifyDataSetChanged();
                    } else {
                        // If the results were fucked we kill ourselves
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        private List<String> getPlacePredictions(CharSequence constraint) {
            List<String> resultStrings = new ArrayList<>();

            // Create the request
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(constraint.toString())
                    .build();

            try {
                FindAutocompletePredictionsResponse response = Tasks.await(
                        client.findAutocompletePredictions(request),
                        60,
                        TimeUnit.SECONDS);

                if (response != null) {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        resultStrings.add(prediction.getFullText(null).toString());
                    }
                }
            } catch (Exception e) {
                Log.e("PlaceAdapter", "Error fetching predictions", e);
            }

            return resultStrings;
        }
    }

    /**
     * Validates all input fields, and gets all the strings then passes to the
     * Controller
     * 
     * @param tagList, a list of all tags for the event
     */
    private void confirmationPass(ArrayList<String> tagList) {
        String eventName = eventNameInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String timeString = eventTimeInput.getText().toString().trim();
        String dateString = eventDateInput.getText().toString().trim();
        String priceString = (eventPriceInput.getText() != null) ? eventPriceInput.getText().toString().trim() : "0";
        boolean hasError = false;

        double price = 0;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            eventPriceInput.setError("Please enter a valid price");
            hasError = true;
        }

        String location = locationInput.getText().toString().trim();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy'T'HH:mm", Locale.getDefault());
        Date dateTime = null;
        try {
            String combined = dateString + "T" + timeString;
            dateTime = dateTimeFormat.parse(combined);
        } catch (ParseException e) {
            eventDateInput.setError("Please follow the format for Date");
            eventTimeInput.setError("Please follow the format for Time");
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

        if (dateTime != null && openPeriodDate != null) {
            if (!openPeriodDate.before(dateTime)) {
                openPeriodInput.setError("Opening date must be before the event date");
                Toast.makeText(this, "Opening date must be before the event date", Toast.LENGTH_SHORT).show();
                hasError = true;
            }
        }

        Date now = new Date();
        if (dateTime != null && dateTime.before(now)) {
            eventDateInput.setError("Event date cannot be in the past");
            Toast.makeText(this, "Event date cannot be in the past", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (openPeriodDate != null && openPeriodDate.before(now)) {
            openPeriodInput.setError("Opening date cannot be in the past");
            Toast.makeText(this, "Opening date cannot be in the past", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (openPeriodDate != null && closePeriodDate != null) {
            if (closePeriodDate.before(openPeriodDate)) {
                closePeriodInput.setError("Closing date must be after the opening date");
                Toast.makeText(this, "Closing date must be after the opening date", Toast.LENGTH_SHORT).show();
                hasError = true;
            }
        }

        if (dateTime != null && closePeriodDate != null) {
            if (closePeriodDate.after(dateTime)) {
                closePeriodInput.setError("Closing date must be before the event date");
                Toast.makeText(this, "Closing date must be before the event date", Toast.LENGTH_SHORT).show();
                hasError = true;
            }
        }

        int entrantLimit = 0;
        try {
            entrantLimit = Integer
                    .parseInt(maxEntrantInput.getText() == null ? "" : maxEntrantInput.getText().toString().trim());
            if (entrantLimit <= 0)
                throw new NumberFormatException("Negative");
            maxEntrantInput.setError(null);
        } catch (NumberFormatException e) {
            if (e.getMessage() != null && e.getMessage().equals("Negative"))
                maxEntrantInput.setError("Max Entrants Must Be Positive");
            else
                maxEntrantInput.setError("Max Entrants is Required");
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
     * Launcher for selecting an image from the device’s gallery, then the image is
     * saved as a uri
     * and displayed for the user to see how it looks
     */
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedBannerUri = uri;
                    bannerInput.setImageURI(uri);
                }
            });

    /**
     * Opens the device’s gallery to allow the user to pick a banner image, limits
     * valid items to only
     * images
     */
    private void openGallery() {
        imagePickerLauncher.launch("image/*");
    }

    /**
     * Validates that all required EditTexts are filled in, if any EditTexts are
     * empty
     * they are highlighted and the user is prompted to fill them in
     *
     * @param fields an array of EditTexts to validate.
     * @return true if all fields are filled, false otherwise.
     */
    private boolean validateAllEditTexts(EditText[] fields, TextInputLayout[] layouts) {
        boolean allFilled = true;
        for (EditText field : fields) {
            String text = field.getText().toString().trim();
            if (text.isEmpty()) {
                allFilled = false;
                field.setBackgroundResource(R.drawable.edit_text_error);
            }
        }

        for (TextInputLayout layout : layouts) {
            String text = layout.getEditText().getText().toString().trim();
            if (text.isEmpty()) {
                Log.e("PENIS", "WEINOR");
                allFilled = false;
                layout.setBoxStrokeColorStateList(
                        new ColorStateList(new int[][] { new int[] { android.R.attr.state_focused }, new int[] {} },
                                new int[] { getColor(R.color.error_red), getColor(R.color.error_red) }));
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
                eventPriceInput.setText(String.valueOf(event.getPrice()));

                // Set event date and time
                if (event.getEventDateTime() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    eventDateInput.setText(dateFormat.format(event.getEventDateTime()));
                    eventTimeInput.setText(timeFormat.format(event.getEventDateTime()));
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
                maxEntrantInput.setText(String.valueOf(event.getMaxEntrants()));

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
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0,
                                decodedBytes.length);
                        bannerInput.setImageBitmap(bitmap);
                        // Don't set selectedBannerUri - we loaded from existing data
                    } catch (Exception e) {
                        Log.e("EventCreationView", "Error decoding poster image: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EventCreationView.this, "Error loading event: " + errorMessage, Toast.LENGTH_SHORT)
                        .show();
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
                Toast.makeText(EventCreationView.this, "Error deleting event: " + errorMessage, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
