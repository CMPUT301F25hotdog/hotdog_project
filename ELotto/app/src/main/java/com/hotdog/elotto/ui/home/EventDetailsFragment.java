package com.hotdog.elotto.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.GeoPoint;
import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.LocationController;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Fragment for displaying detailed information about an event and managing waitlist participation.
 *
 * <p>This fragment provides comprehensive event details including date, time, location, description,
 * and poster image. Users can join or leave the event waitlist with confirmation dialogs and
 * rollback error handling. The UI dynamically updates based on the user's registration status
 * (pending, selected, accepted, waitlisted, cancelled) and whether the lottery has been drawn.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Event details display (title, date, time, location, description, entries count)</li>
 *     <li>Registration period and lottery drawn date information</li>
 *     <li>Base64 event poster image decoding with fallback placeholder</li>
 *     <li>Dynamic UI based on user status and lottery state</li>
 *     <li>Join waitlist functionality with optimistic updates and rollback</li>
 *     <li>Leave waitlist functionality with confirmation dialogs</li>
 *     <li>Special waitlisted state display after lottery draw (red badge and info card)</li>
 *     <li>Status-specific action buttons (Enter Lottery, Leave Waitlist, Cancel Acceptance)</li>
 *     <li>Error handling with toast notifications</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> Lottery drawn date needs to be added to Event model</p>
 *
 * @author Bhuvnesh
 * @version 1.0
 * @since 2025-11-01
 */
public class EventDetailsFragment extends Fragment {

    /**
     * The event being displayed in this fragment.
     */
    private Event event;

    /**
     * The currently logged-in user viewing the event.
     */
    private User currentUser;

    /**
     * Button for navigating back to the previous screen.
     */
    private ImageButton backButton;

    /**
     * ImageView displaying the event poster image.
     */
    private ImageView eventImageView;

    /**
     * TextView displaying the event title.
     */
    private TextView eventTitleTextView;

    /**
     * TextView displaying the event date.
     */
    private TextView eventDateTextView;

    /**
     * TextView displaying the event time.
     */
    private TextView eventTimeTextView;

    /**
     * TextView displaying the event location.
     */
    private TextView eventLocationTextView;

    /**
     * TextView displaying the current entries count vs max entries.
     */
    private TextView entriesCountTextView;

    /**
     * TextView label for registration end section.
     */
    private TextView registrationEndTextView;

    /**
     * TextView displaying the registration end date.
     */
    private TextView registrationEndDateTextView;

    /**
     * TextView label for lottery drawn section.
     */
    private TextView lotteryDrawnTextView;

    /**
     * TextView displaying the lottery drawn date.
     */
    private TextView lotteryDrawnDateTextView;

    /**
     * TextView displaying the event description.
     */
    private TextView eventDescriptionTextView;

    /**
     * Button for entering the lottery (joining the waitlist).
     */
    private Button enterLotteryButton;

    /**
     * Layout container for the waitlisted badge display.
     */
    private LinearLayout waitlistedBadgeLayout;

    /**
     * CardView containing information for waitlisted users.
     */
    private androidx.cardview.widget.CardView waitlistedInfoCard;

    /**
     * Button for leaving the waitlist.
     */
    private MaterialButton leaveWaitlistButton;

    /**
     * Button displayed when no action is available (status-dependent).
     */
    private MaterialButton noActionButton;

    /**
     * TextView displaying the message in the info card.
     */
    private TextView infoCardMessageTextView;

    /**
     * TextView displaying the header in the info card.
     */
    private TextView infoCardHeaderTextView;

    /**
     * CardView containing registration dates information.
     */
    private androidx.cardview.widget.CardView registrationDatesCard;

    /**
     * CardView containing the about/description section.
     */
    private androidx.cardview.widget.CardView aboutCard;

    /**
     * Reference to the root view for UI updates.
     */
    private View viewRef;

    /**
     * Current user status for the event (PENDING, SELECTED, ACCEPTED, WAITLISTED, CANCELLED, or null).
     */
    private String status;
    private boolean locationGranted = false;


    public static EventDetailsFragment newInstance(Event event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of the fragment.
     *
     * <p>Retrieves the event object from arguments if present.</p>
     *
     * @param savedInstanceState the previously saved state of the fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }


        currentUser = new User(requireContext());
        if(event.isGeolocationRequired()){
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Location Required")
                    .setMessage("This event requires location sharing to be enabled to join the waitlist.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        viewRef = view;
        currentUser = new User(requireContext(), new Consumer<User>() {
            @Override
            public void accept(User user) {
                initializeViews(view);
                populateEventData(user);
                setupListeners(user);
            }
        });

        return view;
    }

    /**
     * Initializes all view components by binding them to their IDs.
     *
     * @param view the root view of the fragment
     */
    private void initializeViews(View view) {
        backButton = view.findViewById(R.id.backButton);
        eventImageView = view.findViewById(R.id.eventImageView);
        eventTitleTextView = view.findViewById(R.id.eventTitleTextView);
        eventDateTextView = view.findViewById(R.id.eventDateTextView);
        eventTimeTextView = view.findViewById(R.id.eventTimeTextView);
        eventLocationTextView = view.findViewById(R.id.eventLocationTextView);
        entriesCountTextView = view.findViewById(R.id.entriesCountTextView);
        registrationEndTextView = view.findViewById(R.id.registrationEndTextView);
        registrationEndDateTextView = view.findViewById(R.id.registrationEndDateTextView);
        lotteryDrawnTextView = view.findViewById(R.id.lotteryDrawnTextView);
        lotteryDrawnDateTextView = view.findViewById(R.id.lotteryDrawnDateTextView);
        eventDescriptionTextView = view.findViewById(R.id.eventDescriptionTextView);
        enterLotteryButton = view.findViewById(R.id.enterLotteryButton);
        waitlistedBadgeLayout = view.findViewById(R.id.waitlistedBadgeLayout);
        waitlistedInfoCard = view.findViewById(R.id.waitlistedInfoCard);
        leaveWaitlistButton = view.findViewById(R.id.leaveWaitlistButton);
        noActionButton = view.findViewById(R.id.noActionButton);
        infoCardMessageTextView = view.findViewById(R.id.infoCardMessageTextView);
        infoCardHeaderTextView = view.findViewById(R.id.infoCardHeaderTextView);
        registrationDatesCard = view.findViewById(R.id.registrationDatesCard);
        aboutCard = view.findViewById(R.id.aboutCard);
    }

    /**
     * Populates all event data into the UI components.
     *
     * <p>Displays event title, date (formatted as "EEEE, MMMM dd"), time (formatted as
     * "HH:mm"), location, entries count, registration end date, lottery drawn
     * date (currently hardcoded), and description. Loads the event poster image and
     * updates UI based on user status.</p>
     *
     * @param user the current user viewing the event
     */
    private void populateEventData(User user) {
        if (event == null)
            return;

        status = getUserStatus(user);
        List<String> entrantIds = event.getWaitlistEntrantIds();
        if(entrantIds == null) entrantIds=new ArrayList<>();
        if (user.findRegEvent(event.getId()) && !entrantIds.contains(user.getId())) {
            user.removeRegEvent(event.getId());
        } else if (!user.findRegEvent(event.getId()) && entrantIds.contains(user.getId())) {
            entrantIds.remove(user.getId());
            event.setWaitlistEntrantIds(entrantIds);
            EventRepository eventRepo = new EventRepository();
            eventRepo.updateEvent(event, new OperationCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onError(String errorMessage) {}
            });
        }

        // Set title
        eventTitleTextView.setText(event.getName());

        // Set date
        if (event.getEventDateTime() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
            eventDateTextView.setText(dateFormat.format(event.getEventDateTime()));
        }

        // Set time
        if (event.getEventDateTime() != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startTime = timeFormat.format(event.getEventDateTime());
            eventTimeTextView.setText(startTime);
        }

        // Set location
        eventLocationTextView.setText(event.getLocation());

        // Set entries count
        int currentEntries = event.getCurrentEntrantsCount();
        int maxEntries = event.getMaxEntrants();
        entriesCountTextView.setText(currentEntries + " of " + maxEntries);

        // Set registration end date
        if (event.getRegistrationEndDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
            registrationEndDateTextView.setText(dateFormat.format(event.getRegistrationEndDate()));
        }

        // Set lottery drawn date
        // TODO: Add lottery drawn date to Event model once its ready
        lotteryDrawnDateTextView.setText(event.getRegistrationEndDate().toString());

        // Set description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            eventDescriptionTextView.setText(event.getDescription());
        } else {
            eventDescriptionTextView.setText("No description available.");
        }

        loadEventImage();

        // Update button state based on user's registration status
        updateUIBasedOnStatus(user);
    }

    /**
     * Loads and displays the event poster image.
     *
     * <p>Handles three scenarios:</p>
     * <ol>
     *     <li>No image/failed image: Shows placeholder (image_24px)</li>
     *     <li>Base64 image: Decodes and displays with CENTER_CROP scaling</li>
     *     <li>Decode error: Shows placeholder and logs exception</li>
     * </ol>
     *
     * <p>Recognizes special values: "no_image" and strings starting with "image_failed_"</p>
     */
    private void loadEventImage() {
        String posterImageUrl = event.getPosterImageUrl();

        if (posterImageUrl == null || posterImageUrl.isEmpty() ||
                posterImageUrl.equals("no_image") ||
                posterImageUrl.startsWith("image_failed_")) {
            // No image or failed image - keep placeholder
            eventImageView.setImageResource(R.drawable.image_24px);
            return;
        }

        try {
            // Decoding Base64 string to bitmap
            byte[] decodedBytes = Base64.decode(posterImageUrl, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                eventImageView.setImageBitmap(bitmap);
                eventImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {

                eventImageView.setImageResource(R.drawable.image_24px);
            }
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
            eventImageView.setImageResource(R.drawable.image_24px);
        } catch (Exception e) {

            e.printStackTrace();
            eventImageView.setImageResource(R.drawable.image_24px);
        }
    }

    /**
     * Updates the UI based on the user's registration status.
     *
     * <p>This method handles all possible states:</p>
     * <ul>
     *     <li>Not registered + registration open → "Enter Lottery" (enabled)</li>
     *     <li>Not registered + registration closed → "Registration Closed" (disabled)</li>
     *     <li>Not registered + waitlist full → "Lottery Full" (disabled)</li>
     *     <li>Pending (before lottery drawn) → "Leave Waitlist" (enabled)</li>
     *     <li>Waitlisted (after lottery, not selected) → Red badge + info card + red button</li>
     *     <li>Selected → Hide all buttons</li>
     *     <li>Accepted → Hide all buttons</li>
     *     <li>Cancelled → Display info message with disabled button</li>
     * </ul>
     *
     * <p>For waitlisted state specifically, hides registration dates and about cards while
     * showing a red badge and informational card explaining the waitlist status.</p>
     *
     * @param user the current user viewing the event
     */
    private void updateUIBasedOnStatus(User user) {
        if (event == null || user == null) return;

        status = getUserStatus(user);
        boolean lotteryDrawn = hasLotteryBeenDrawn();

        // Hide all status-specific UI by default
        waitlistedBadgeLayout.setVisibility(View.GONE);
        waitlistedInfoCard.setVisibility(View.GONE);
        leaveWaitlistButton.setVisibility(View.GONE);
        enterLotteryButton.setVisibility(View.VISIBLE);

        // Show cards by default (will be hidden in waitlisted state)
        registrationDatesCard.setVisibility(View.VISIBLE);
        aboutCard.setVisibility(View.VISIBLE);

        buttonState(status);
        if ("PENDING".equals(status)) {
            // ========== USER IS ON WAITLIST ==========

            if (lotteryDrawn) {
                // ========== WAITLISTED STATE (US 01.05.01) ==========
                // Lottery was drawn, user not selected = WAITLISTED
                // Show red badge, info card, and red "Leave Waiting List" button
                waitlistedBadgeLayout.setVisibility(View.VISIBLE);
                waitlistedInfoCard.setVisibility(View.VISIBLE);
                infoCardMessageTextView.setText("You weren't selected in the initial lottery draw, but you're still on the waiting list. Good news! You could still be selected if any chosen participants decline their invitation. If a spot opens up, you will be notified immediately if you get selected");

                // Hide registration dates and about section
                registrationDatesCard.setVisibility(View.GONE);
                aboutCard.setVisibility(View.GONE);

            }
        } else if ("CANCELLED".equals(status)) {
            // ========== CANCELLED ==========
            // User was cancelled - show info card and disabled button
            waitlistedInfoCard.setVisibility(View.VISIBLE);
            infoCardHeaderTextView.setText("Event Status");
            infoCardMessageTextView.setText("You were cancelled for this event and cannot rejoin.");
        }
    }

    /**
     * Sets up click listeners for interactive UI elements.
     *
     * <p>Configures the back button to navigate up, the enter lottery button to toggle
     * between joining and leaving the waitlist based on registration status, and the
     * leave waitlist button to remove the user from the waitlist.</p>
     *
     * @param user the current user viewing the event
     */
    private void setupListeners(User user) {
        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigateUp();
        });
        enterLotteryButton.setOnClickListener(v -> {
            joinWaitlistBack();
        });

        leaveWaitlistButton.setOnClickListener(v -> {
            leaveWaitlist();
        });

    }

    private boolean locationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                locationGranted = isGranted;
                joinWaitlist();
            });

    private void joinWaitlistBack() {
        locationGranted = locationPermission();
        if (event.isGeolocationRequired()) {
            if (!locationPermission()) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }
        }
        joinWaitlist();
    }

    private void joinWaitlist() {
        if (event == null || currentUser == null) {
            Toast.makeText(getContext(), "Error: Unable to join waitlist", Toast.LENGTH_SHORT).show();
            return;
        }
        // Checks if already registered
        if (currentUser.findRegEvent(event.getId())) {
            Toast.makeText(getContext(),
                    "You're already registered for this event",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        buttonState("LOADING");
        String userId = currentUser.getId();

        try {
            if (event.isGeolocationRequired() && locationGranted) {
                LocationController locationController = new LocationController(getContext());
                locationController.getLatLon(new LocationController.LocationCallBack() {
                    @Override
                    public void onLocationReady(double lat, double lon) {
                        if (Double.isNaN(lat) || Double.isNaN(lon)) {
                            enterLotteryButton.setEnabled(true);
                            enterLotteryButton.setText("Enter Lottery");
                            Toast.makeText(getContext(), "Location required to join waitlist", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        event.setEntrantLocations(userId, new GeoPoint(lat, lon));

                        List<String> waitlistIds = event.getWaitlistEntrantIds();
                        if (waitlistIds == null) {
                            waitlistIds = new ArrayList<>();
                        }
                        if (!waitlistIds.contains(userId)) {
                            waitlistIds.add(userId);
                        }
                        event.setWaitlistEntrantIds(waitlistIds);

                        EventRepository eventRepository = new EventRepository();
                        eventRepository.updateEvent(event, new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(),
                                        "Successfully joined waitlist for " + event.getName(),
                                        Toast.LENGTH_SHORT).show();
                                // Only add reg event if it successfully added to firestore
                                currentUser.addRegEvent(event.getId());
                                updateUIBasedOnStatus(currentUser);

                                int currentEntries = event.getCurrentEntrantsCount();
                                int maxEntries = event.getMaxEntrants();
                                entriesCountTextView.setText(currentEntries + " of " + maxEntries);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                showDialogPerStatus("ERROR JOIN");
                                updateUIBasedOnStatus(null);
                            }
                        });
                    }
                });
            }
            else if(event.isGeolocationRequired()) {
                Toast.makeText(getContext(),
                        "Geolocation is required. Please allow location access when prompted to join this event. " + event.getName(),
                        Toast.LENGTH_SHORT).show();
                updateUIBasedOnStatus(null);
                buttonState(null);
            } else {
                // Non-geolocation events
                List<String> waitlistIds = event.getWaitlistEntrantIds();
                if (waitlistIds == null) {
                    waitlistIds = new ArrayList<>();
                }
                if (!waitlistIds.contains(userId)) {
                    waitlistIds.add(userId);
                }
                event.setWaitlistEntrantIds(waitlistIds);

                EventRepository eventRepository = new EventRepository();
                eventRepository.updateEvent(event, new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(),
                                "Successfully joined waitlist for " + event.getName(),
                                Toast.LENGTH_SHORT).show();
                        // Only add reg event if it successfully added to firestore
                        currentUser.addRegEvent(event.getId());
                        updateUIBasedOnStatus(currentUser); // ADD THIS LINE

                        int currentEntries = event.getCurrentEntrantsCount();
                        int maxEntries = event.getMaxEntrants();
                        entriesCountTextView.setText(currentEntries + " of " + maxEntries);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getContext(),
                                "Error joining waitlist: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                        buttonState(null);
                        showDialogPerStatus("ERROR JOIN");
                    }
                });
            }

        } catch (Exception e) {
            // Rollback
            currentUser.removeRegEvent(event.getId());

            showDialogPerStatus("ERROR JOIN");

            status = getUserStatus(currentUser);
            buttonState(status);
        }
    }


    /**
     * Initiates the waitlist leave process by showing a confirmation dialog.
     *
     * <p>
     * Gets the current user status and displays an appropriate confirmation
     * dialog or error message based on that status.
     * </p>
     */
    private void leaveWaitlist() {
        if (event == null || currentUser == null) {
            showDialogPerStatus("ERROR LEAVE");
            return;
        }

        status = getUserStatus(currentUser);
        buttonState(status);
        showDialogPerStatus(status);
    }

    /**
     * Performs the actual waitlist leave operation with optimistic updates and rollback.
     *
     * <p>This method:</p>
     * <ol>
     *     <li>Disables buttons to prevent double-clicks</li>
     *     <li>Removes event from user's registered events</li>
     *     <li>Removes user ID from event's waitlist</li>
     *     <li>Updates event in Firestore</li>
     *     <li>On success: updates UI and shows success message</li>
     *     <li>On error: rolls back changes and displays error message</li>
     * </ol>
     *
     * <p>Shows loading state during operation and toast messages for success/failure.</p>
     */
    private void performLeaveWaitlist() {
        // Disable all buttons to prevent double-clicks
        buttonState("LOADING");

        try {
            String userId = currentUser.getId();
            boolean removed = currentUser.removeRegEvent(event.getId());

            // this should never happen
            if (!removed) {
                Toast.makeText(getContext(), "Event not found in your registered events", Toast.LENGTH_SHORT).show();
                status = getUserStatus(currentUser);
                buttonState(status);
                return;
            }

            List<String> waitlistIds = event.getWaitlistEntrantIds();
            if (waitlistIds != null) {
                waitlistIds.remove(userId);
            }

            if (event.getEntrantLocations() != null) {
                event.getEntrantLocations().remove(userId);
            }

            EventRepository eventRepository = new EventRepository();
            eventRepository.updateEvent(event, new OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Successfully left waitlist for " + event.getName(),
                            Toast.LENGTH_SHORT).show();
                    updateUIBasedOnStatus(currentUser);

                    int currentEntries = event.getCurrentEntrantsCount();
                    int maxEntries = event.getMaxEntrants();
                    entriesCountTextView.setText(currentEntries + " of " + maxEntries);
                }

                @Override
                public void onError(String errorMessage) {
                    // Rollback: Add event back to user's registered events
                    currentUser.addRegEvent(event.getId());

                    showDialogPerStatus("ERROR LEAVE");

                    // Re-enable buttons
                    buttonState(status);
                }
            });
        } catch (Exception e) {
            // Rollback
            currentUser.addRegEvent(event.getId());

            showDialogPerStatus("ERROR LEAVE");
            buttonState(status);
        }
    }


    /**
     * Checks if the lottery has been drawn for this event.
     *
     * <p>Determines if the lottery has been drawn by checking if there are any
     * selected entrants in the event's selected list.</p>
     *
     * @return true if lottery was drawn (selected list is not empty), false otherwise
     */
    private boolean hasLotteryBeenDrawn() {
        if (event == null) return false;

        List<String> selectedIds = event.getSelectedEntrantIds();

        // If there are selected entrants, lottery has been drawn
        return selectedIds != null && !selectedIds.isEmpty();
    }

    /**
     * Gets the user's registration status for this event.
     *
     * <p>Checks the event's various entrant lists in priority order to determine status:</p>
     * <ol>
     *     <li>ACCEPTED - user is in accepted list</li>
     *     <li>SELECTED - user is in selected list</li>
     *     <li>CANCELLED - user is in cancelled list</li>
     *     <li>WAITLISTED - user is in waitlist and lottery has been drawn</li>
     *     <li>PENDING - user is in waitlist but lottery not yet drawn</li>
     *     <li>null - user is not registered</li>
     * </ol>
     *
     * @param user the user to check status for
     * @return status string ("PENDING", "SELECTED", "ACCEPTED", "WAITLISTED", "CANCELLED"), or null if not registered
     */
    private String getUserStatus(User user) {
        if (event == null || user == null) return null;

        String userId = user.getId();

        // Check if user is in any of the event's lists
        List<String> waitlistIds = event.getWaitlistEntrantIds();
        List<String> selectedIds = event.getSelectedEntrantIds();
        List<String> acceptedIds = event.getAcceptedEntrantIds();
        List<String> cancelledIds = event.getCancelledEntrantIds();

        // Check in priority order (most specific to least specific)
        if (acceptedIds != null && acceptedIds.contains(userId)) {
            return "ACCEPTED";
        }

        if (selectedIds != null && selectedIds.contains(userId)) {
            return "SELECTED";
        }

        if (cancelledIds != null && cancelledIds.contains(userId)) {
            return "CANCELLED";
        }

        if (waitlistIds != null && waitlistIds.contains(userId) && hasLotteryBeenDrawn()) {
            return "WAITLISTED";
        }

        if (waitlistIds != null && waitlistIds.contains(userId)) {
            return "PENDING";
        }

        return null; // Not registered
    }

    /**
     * Updates button visibility and text based on the current status.
     *
     * <p>Controls which buttons are visible and what text they display for each status:</p>
     * <ul>
     *     <li>PENDING: Shows "Leave Waitlist" button</li>
     *     <li>SELECTED: Shows disabled "Already Selected" button</li>
     *     <li>ACCEPTED: Shows "Cancel Acceptance" button</li>
     *     <li>CANCELLED: Shows disabled "Event Already Drawn" button</li>
     *     <li>LOADING: Shows disabled "Loading..." button</li>
     *     <li>NULL: Shows "Enter Lottery" button</li>
     *     <li>WAITLISTED: Shows disabled "Event Already Drawn" button</li>
     * </ul>
     *
     * @param status the current user status string
     */
    private void buttonState(String status) {
        if(status == null) status="NULL";
        switch (status) {
            case "PENDING" -> {
                leaveWaitlistButton.setVisibility(View.VISIBLE);
                enterLotteryButton.setVisibility(View.GONE);
                noActionButton.setVisibility(View.GONE);
                leaveWaitlistButton.setText("Leave Waitlist");
            }
            case "SELECTED" -> {
                leaveWaitlistButton.setVisibility(View.GONE);
                enterLotteryButton.setVisibility(View.GONE);
                noActionButton.setVisibility(View.VISIBLE);
                noActionButton.setText("Already Selected");
            }
            case "ACCEPTED" -> {
                leaveWaitlistButton.setVisibility(View.VISIBLE);
                enterLotteryButton.setVisibility(View.GONE);
                noActionButton.setVisibility(View.GONE);
                leaveWaitlistButton.setText("Cancel Acceptance");
            }
            case "CANCELLED" -> {
                leaveWaitlistButton.setVisibility(View.GONE);
                enterLotteryButton.setVisibility(View.GONE);
                noActionButton.setVisibility(View.VISIBLE);
                noActionButton.setText("Event Already Drawn");
            }
            case "LOADING" -> {
                leaveWaitlistButton.setVisibility(View.GONE);
                enterLotteryButton.setVisibility(View.GONE);
                noActionButton.setVisibility(View.VISIBLE);
                noActionButton.setText("Loading...");
            }
            case "NULL" -> {
                leaveWaitlistButton.setVisibility(View.GONE);
                enterLotteryButton.setVisibility(View.VISIBLE);
                noActionButton.setVisibility(View.GONE);
            }
            case "WAITLISTED" -> {
                leaveWaitlistButton.setVisibility(View.GONE);
                enterLotteryButton.setVisibility(View.GONE);
                noActionButton.setVisibility(View.VISIBLE);
                noActionButton.setText("Event Already Drawn");
            }default -> {}
        }
    }

    /**
     * Displays appropriate dialogs or toast messages based on the current status.
     *
     * <p>Shows status-specific dialogs or toasts:</p>
     * <ul>
     *     <li>PENDING: Confirmation dialog for leaving waitlist</li>
     *     <li>SELECTED: Notice that user is already selected</li>
     *     <li>ACCEPTED: Confirmation dialog for canceling acceptance (irreversible warning)</li>
     *     <li>CANCELLED: Notice that user already canceled</li>
     *     <li>NULL: Success toast for joining event</li>
     *     <li>ERROR JOIN: Error toast for join failure</li>
     *     <li>ERROR LEAVE: Error toast for leave failure</li>
     * </ul>
     *
     * <p>Dialogs include positive/negative buttons where appropriate, with the positive
     * button calling performLeaveWaitlist() for destructive actions.</p>
     *
     * @param status the current status or error state string
     */
    private void showDialogPerStatus(String status) {
        if(status == null) status="NULL";
        switch (status) {
            case "PENDING" -> {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Leave Waitlist")
                        .setMessage("Are you sure you want to leave the waitlist for " + event.getName() + "?")
                        .setPositiveButton("Leave", (dialog, which) -> {
                            performLeaveWaitlist();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            case "SELECTED" -> {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Notice")
                        .setMessage("You have already been selected for the event " + event.getName() + ".\nPlease accept or deny the selection.")
                        .setPositiveButton("Leave", null)
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            case "ACCEPTED" -> {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Cancel Selection")
                        .setMessage("Are you sure you want to cancel your acceptance to the event " + event.getName() + "?\nThis action is irreversible!!")
                        .setPositiveButton("Leave", (dialog, which) -> {
                            performLeaveWaitlist();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            case "CANCELLED" -> {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Notice")
                        .setMessage("You have already canceled your selection for the event " + event.getName() + ".")
                        .setPositiveButton("Leave", null)
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            case "NULL" -> {
                Toast.makeText(getContext(), "Successfully joined event " + event.getName() + "!", Toast.LENGTH_SHORT).show();
            } case "ERROR JOIN" -> {
                Toast.makeText(getContext(), "Error joining the event " + event.getName(), Toast.LENGTH_SHORT).show();
            } case "ERROR LEAVE" -> {
                Toast.makeText(getContext(), "Error leaving the event " + event.getName(), Toast.LENGTH_SHORT).show();
            } default -> {}
        }
    }
}