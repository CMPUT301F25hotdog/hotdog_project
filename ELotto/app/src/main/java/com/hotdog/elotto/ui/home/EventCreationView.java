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
 * Activity for creating and editing events.
 *
 * <p>This activity provides a comprehensive form for event creation and editing with
 * extensive input validation, image selection, location autocomplete via Google Places
 * API, and date/time pickers. Supports two modes: CREATE (new event) and EDIT (modify
 * existing event).</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Event name, description, location, date, time, and price input fields</li>
 *     <li>Material Design date and time pickers for event timing and registration periods</li>
 *     <li>Google Places API integration for location autocomplete suggestions</li>
 *     <li>Image picker for event poster selection with Base64 encoding</li>
 *     <li>Multi-select tag picker for event categorization</li>
 *     <li>Max entrants and waitlist size configuration</li>
 *     <li>Geolocation requirement toggle</li>
 *     <li>Real-time input validation with error highlighting</li>
 *     <li>Price input with decimal filter (2 decimal places max)</li>
 *     <li>Image size validation (900KB max for Firestore)</li>
 *     <li>Event deletion with confirmation dialog (edit mode only)</li>
 *     <li>Pre-filled forms when editing existing events</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh
 * @version 1.0
 * @since 2025-11-01
 */
public class EventCreationView extends AppCompatActivity {
    /**
     * Input field for event name.
     */
    private TextInputEditText eventNameInput;

    /**
     * Layout wrapper for event name input.
     */
    private TextInputLayout eventNameLayout;

    /**
     * Input field for event description.
     */
    private EditText eventDescriptionInput;

    /**
     * Input field for event time (HH:mm format).
     */
    private TextInputEditText eventTimeInput;

    /**
     * Layout wrapper for event time input.
     */
    private TextInputLayout eventTimeLayout;

    /**
     * Layout wrapper for event date input.
     */
    private TextInputLayout eventDateLayout;

    /**
     * Input field for event date (MM/dd/yyyy format).
     */
    private TextInputEditText eventDateInput;

    /**
     * Layout wrapper for registration opening date input.
     */
    private TextInputLayout openPeriodLayout;

    /**
     * Input field for registration opening date.
     */
    private TextInputEditText openPeriodInput;

    /**
     * Layout wrapper for registration closing date input.
     */
    private TextInputLayout closePeriodLayout;

    /**
     * Input field for registration closing date.
     */
    private TextInputEditText closePeriodInput;

    /**
     * Input field for maximum number of entrants.
     */
    private TextInputEditText maxEntrantInput;

    /**
     * Layout wrapper for max entrants input.
     */
    private TextInputLayout maxEntrantLayout;

    /**
     * Input field for waitlist size limit (optional).
     */
    private EditText waitListSizeInput;

    /**
     * Switch toggle for requiring geolocation verification.
     */
    private SwitchCompat geolocation;

    /**
     * Input field for event price with decimal filtering.
     */
    private TextInputEditText eventPriceInput;

    /**
     * Layout wrapper for price input.
     */
    private TextInputLayout eventPriceLayout;

    /**
     * Layout wrapper for location autocomplete input.
     */
    private TextInputLayout locationLayout;

    /**
     * Autocomplete input field for event location with Google Places suggestions.
     */
    private AutoCompleteTextView locationInput;

    /**
     * Button to cancel event creation/editing and close the activity.
     */
    private Button cancelButton;

    /**
     * Button to confirm and save event (text changes based on create/edit mode).
     */
    private Button confirmButton;

    /**
     * Button to delete the event (visible only in edit mode).
     */
    private Button deleteButton;

    /**
     * Back button to close the activity.
     */
    private ImageButton backButton;

    /**
     * ImageView for displaying and selecting the event poster image.
     */
    private ImageView bannerInput;

    /**
     * URI of the selected event poster image from device gallery.
     */
    private Uri selectedBannerUri;

    /**
     * Button to open tag selection dialog.
     */
    private Button tags;

    /**
     * List of selected tags for the event.
     */
    private ArrayList<String> tagList = new ArrayList<>();

    /**
     * Current operating mode: "CREATE" for new events, "EDIT" for existing events.
     */
    private String currentMode;

    /**
     * Event ID when in edit mode, null when creating new event.
     */
    private String currentEventId;


    // For location shtuff
    /**
     * Google Places API client for location autocomplete.
     */
    private PlacesClient client;

    /**
     * Session token for Places API autocomplete requests.
     */
    private AutocompleteSessionToken token;

    /**
     * Adapter for displaying location autocomplete suggestions.
     */
    private PlaceAutoSuggestAdapter placeAutoSuggestAdapter;

    /**
     * Input filter for decimal number fields with configurable precision.
     *
     * <p>Uses regex pattern matching to allow only valid decimal numbers with
     * a specified number of digits after the decimal point.</p>
     */
    private static class DecimalInputFilter implements InputFilter {
        /**
         * Regex pattern for validating decimal input.
         */
        private final Pattern inPattern;

        /**
         * Constructs a DecimalInputFilter with specified decimal places.
         *
         * @param digitsAfterZero maximum number of digits allowed after decimal point
         */
        public DecimalInputFilter(int digitsAfterZero) {
            // Ballin i love regex muah regex my baby I love you
            inPattern = Pattern.compile("[0-9]*+((\\.[0-9]{0,"+digitsAfterZero+"})?)");
        }

        /**
         * Filters input to match the decimal pattern.
         *
         * @param src the new characters being inserted
         * @param start the start position of the new characters
         * @param end the end position of the new characters
         * @param dst the current text in the field
         * @param dstart the start position where the new characters will be inserted
         * @param dend the end position where the new characters will be inserted
         * @return null if the result is valid, empty string to reject the input
         */
        @Override
        public CharSequence filter (CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
            String result = dst.subSequence(0, dstart) + src.toString() + dst.subSequence(dend, dst.length());
            return inPattern.matcher(result).matches() ? null : "";
        }
    }

    /**
     * Initializes the activity and sets up UI components.
     *
     * <p>This method performs the following initialization:</p>
     * <ul>
     *     <li>Binds all view components by their IDs</li>
     *     <li>Determines operating mode (CREATE or EDIT) from intent extras</li>
     *     <li>Loads existing event data if in EDIT mode</li>
     *     <li>Sets up date pickers with Material Design components</li>
     *     <li>Configures time picker with 24-hour format</li>
     *     <li>Initializes Google Places API for location autocomplete</li>
     *     <li>Sets up price input with decimal filter (2 places)</li>
     *     <li>Configures tag selection dialog with multi-choice options</li>
     *     <li>Sets up input validation and error handling</li>
     *     <li>Configures button click listeners (confirm, cancel, delete, back)</li>
     * </ul>
     *
     * @param savedInstanceState the saved state of the activity, if any
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
        deleteButton = findViewById(R.id.Delete_Event_Button);
        locationLayout = findViewById(R.id.EventAddressLayout);
        locationInput = findViewById(R.id.EventAddressInput);
        eventPriceInput = findViewById(R.id.EventPriceInput);
        eventPriceLayout = findViewById(R.id.EventPriceLayout);
        tags = findViewById(R.id.Tag_Button);


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
                            if (selected[i]) tagList.add(tag_options[i]);
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

        // Calendar Dialog
        MaterialDatePicker<Long> openDatePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Opening Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
        MaterialDatePicker<Long> closeDatePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Closing Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
        MaterialDatePicker<Long> eventDatePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Event Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        View.OnClickListener openOpen = v -> openDatePicker.show(getSupportFragmentManager(), "DATE");
        openPeriodInput.setOnClickListener(openOpen);
        openPeriodLayout.setOnClickListener(openOpen);

        View.OnClickListener closeOpen = v -> closeDatePicker.show(getSupportFragmentManager(), "DATE");
        closePeriodInput.setOnClickListener(closeOpen);
        closePeriodLayout.setOnClickListener(closeOpen);

        View.OnClickListener dateOpen = v -> eventDatePicker.show(getSupportFragmentManager(), "DATE");
        eventDateInput.setOnClickListener(dateOpen);
        eventDateLayout.setOnClickListener(dateOpen);


        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        openDatePicker.addOnPositiveButtonClickListener(selectionMillis -> {
            Date date = new Date(selectionMillis);
            format.setTimeZone(TimeZone.getTimeZone("UTC")); // Prevent tz difference from shifting date
            openPeriodInput.setText(format.format(date));
        });

        closeDatePicker.addOnPositiveButtonClickListener(selectionMillis -> {
            Date date = new Date(selectionMillis);
            format.setTimeZone(TimeZone.getTimeZone("UTC")); // Prevent tz difference from shifting date
            closePeriodInput.setText(format.format(date));
        });

        eventDatePicker.addOnPositiveButtonClickListener(selectionMillis -> {
            Date date = new Date(selectionMillis);
            format.setTimeZone(TimeZone.getTimeZone("UTC")); // Prevent tz difference from shifting date
            eventDateInput.setText(format.format(date));
        });

        // Time picker!
        MaterialTimePicker eventTimePicker =
                new MaterialTimePicker.Builder()
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
        eventPriceInput.setFilters(new InputFilter[]{new DecimalInputFilter(2)});
        eventPriceInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) return;
            TextInputEditText editText = (TextInputEditText) v;
            Editable editable = editText.getText();
            if (editable == null) return;
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
            if (hasFocus) return;
            int entrantLimit = 0;
            try {
                entrantLimit = Integer.parseInt(maxEntrantInput.getText() == null ? "" : maxEntrantInput.getText().toString().trim());
                if (entrantLimit <= 0) throw new NumberFormatException("Negative");
                maxEntrantInput.setError(null);
            } catch (NumberFormatException e) {
                if (e.getMessage() != null && e.getMessage().equals("Negative"))
                    maxEntrantInput.setError("Max Entrants Must Be Positive");
                else maxEntrantInput.setError("Max Entrants is Required");
            }
        };
        maxEntrantInput.setOnFocusChangeListener(maxEntrantListener);

        // Location time!
        this.initPlaces("AIzaSyB1WXzUjkY-JxcAhppv5wCJ8kH81lbwpME");

    }

    /**
     * Initializes the Google Places API for location autocomplete.
     *
     * <p>Sets up the Places client, creates a session token, initializes the custom
     * autocomplete adapter, and configures the location input field with autocomplete
     * functionality and validation.</p>
     *
     * @param apiKey the Google Places API key for authentication
     */
    private void initPlaces(String apiKey) {
        if(!Places.isInitialized()) Places.initializeWithNewPlacesApiEnabled(this, apiKey);

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
            if(!hasFocus) {
                String input = locationInput.getText().toString();
                if(input.isEmpty()) locationInput.setError("Address is required.");
                else locationInput.setError(null);
            }
        });
    }

    /**
     * Custom ArrayAdapter for displaying Google Places autocomplete suggestions.
     *
     * <p>This adapter fetches place predictions from the Google Places API as the user
     * types and displays them in a dropdown list. Uses the Places API with a session
     * token for efficient request batching.</p>
     */
    private class PlaceAutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
        /**
         * List of autocomplete prediction strings to display.
         */
        private List<String> resultList = new ArrayList<>();

        /**
         * Constructs a PlaceAutoSuggestAdapter.
         *
         * @param context the application context
         * @param resource the layout resource for dropdown items
         */
        public PlaceAutoSuggestAdapter(Context context, int resource) {
            super(context, resource);
        }

        /**
         * Gets the number of items in the result list.
         *
         * @return the size of the result list
         */
        @Override
        public int getCount() {
            return resultList.size();
        }

        /**
         * Gets the prediction string at the specified position.
         *
         * @param position the position in the result list
         * @return the prediction string at that position
         */
        @Override
        public String getItem(int position) {
            return resultList.get(position);
        }

        /**
         * Returns the filter for performing autocomplete searches.
         *
         * @return a Filter that queries the Places API and publishes results
         */
        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterRes = new FilterResults();
                    if(constraint != null) {
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
                    if(results != null && results.count > 0) {
                        resultList = (List<String>) results.values;
                        notifyDataSetChanged();
                    } else {
                        // If the results were fucked we kill ourselves
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        /**
         * Fetches place predictions from the Google Places API.
         *
         * <p>Uses a synchronous API call with 60-second timeout. Extracts full text
         * from each prediction and returns as a list of strings.</p>
         *
         * @param constraint the search query text
         * @return list of place prediction strings, or empty list on error
         */
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
                        TimeUnit.SECONDS
                );

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
     * Validates all input fields and processes event creation or update.
     *
     * <p>This method performs comprehensive validation of all fields including:</p>
     * <ul>
     *     <li>Price parsing and validation</li>
     *     <li>Date and time format validation and parsing</li>
     *     <li>Registration period date validation</li>
     *     <li>Max entrants validation (must be positive integer)</li>
     *     <li>Waitlist size validation (optional)</li>
     *     <li>Banner image validation (required, max 900KB)</li>
     * </ul>
     *
     * <p>On successful validation, encodes the image to Base64 and calls the
     * EventCreationController to either save a new event or update an existing one
     * based on the current mode. Displays error messages for validation failures.</p>
     *
     * @param tagList the list of selected tags for the event
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

        int entrantLimit = 0;
        try {
            entrantLimit = Integer.parseInt(maxEntrantInput.getText()==null ? "" : maxEntrantInput.getText().toString().trim());
            if(entrantLimit <= 0) throw new NumberFormatException("Negative");
            maxEntrantInput.setError(null);
        } catch (NumberFormatException e) {
            if(e.getMessage() != null && e.getMessage().equals("Negative")) maxEntrantInput.setError("Max Entrants Must Be Positive");
            else maxEntrantInput.setError("Max Entrants is Required");
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
     * Activity result launcher for selecting an image from the device gallery.
     *
     * <p>When an image is selected, stores the URI and displays the image in the
     * banner ImageView for preview.</p>
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedBannerUri = uri;
                    bannerInput.setImageURI(uri);
                }
            });

    /**
     * Opens the device gallery for image selection.
     *
     * <p>Launches the image picker activity with MIME type filter "image/*" to
     * show only image files.</p>
     */
    private void openGallery() {
        imagePickerLauncher.launch("image/*");
    }

    /**
     * Validates that all required input fields are filled.
     *
     * <p>Checks each EditText and TextInputLayout for empty values. Highlights
     * invalid fields with error styling (red border for TextInputLayouts, error
     * background for EditTexts). Shows a toast message if any fields are empty.</p>
     *
     * @param fields array of EditText fields to validate
     * @param layouts array of TextInputLayout fields to validate
     * @return true if all fields are filled, false otherwise
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
                layout.setBoxStrokeColorStateList( new ColorStateList(new int[][] {new int[]{android.R.attr.state_focused}, new int[] {}}, new int[] {getColor(R.color.error_red), getColor(R.color.error_red)}));
            }
        }
        if (!allFilled) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
        }
        return allFilled;
    }

    /**
     * Loads existing event data and pre-fills all form fields for editing.
     *
     * <p>Fetches the event from Firestore by ID and populates all input fields with
     * the existing values including name, description, location, price, dates, times,
     * max entrants, waitlist size, geolocation setting, and poster image. Decodes
     * the Base64 poster image and displays it in the ImageView.</p>
     *
     * <p>Shows an error toast and closes the activity if the event cannot be loaded.</p>
     *
     * @param eventId the ID of the event to load
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
     * Deletes the current event from Firestore after confirmation.
     *
     * <p>Validates that a currentEventId exists, then calls the EventRepository
     * to delete the event. Shows a success toast and closes the activity on
     * successful deletion, or an error toast on failure.</p>
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