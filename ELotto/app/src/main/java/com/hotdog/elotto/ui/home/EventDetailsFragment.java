package com.hotdog.elotto.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * EventDetailsFragment displays detailed information about a selected event.
 * Also allows the user to join and leave a waiting list for an event
 *
 *
 * Outstanding Issues:
 * - Lottery drawn date needs to be added to Event model
 */
public class EventDetailsFragment extends Fragment {

    private Event event;
    private User currentUser;

    private ImageButton backButton;
    private ImageView eventImageView;
    private TextView eventTitleTextView;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;
    private TextView eventLocationTextView;
    private TextView entriesCountTextView;
    private TextView registrationEndTextView;
    private TextView registrationEndDateTextView;
    private TextView lotteryDrawnTextView;
    private TextView lotteryDrawnDateTextView;
    private TextView eventDescriptionTextView;
    private Button enterLotteryButton;
    private LinearLayout waitlistedBadgeLayout;
    private androidx.cardview.widget.CardView waitlistedInfoCard;
    private MaterialButton leaveWaitlistButton;
    private MaterialButton noActionButton;
    private TextView infoCardMessageTextView;  // ADD THIS LINE

    private TextView infoCardHeaderTextView;  // ADD THIS (optional, for header)

    private androidx.cardview.widget.CardView registrationDatesCard;  // ADD THIS

    private androidx.cardview.widget.CardView aboutCard;  // ADD THIS

    // For initializing views
    private View viewRef;

    // For state consistency
    private String status;


    public static EventDetailsFragment newInstance(Event event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
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
        infoCardHeaderTextView = view.findViewById(R.id.infoCardHeaderTextView);  // ADD THIS (optional)
        registrationDatesCard = view.findViewById(R.id.registrationDatesCard);  // ADD THIS
        aboutCard = view.findViewById(R.id.aboutCard);  // ADD THIS
    }

    private void populateEventData(User user) {
        if (event == null)
            return;

        status = getUserStatus(user);

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
            eventTimeTextView.setText(startTime + " - 17:30");
        }

        // Set location
        eventLocationTextView.setText(event.getLocation());

        // Set entries count
        int currentEntries = event.getCurrentWaitlistCount();
        int maxEntries = event.getMaxEntrants();
        entriesCountTextView.setText(currentEntries + " of " + maxEntries);

        // Set registration end date
        if (event.getRegistrationEndDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
            registrationEndDateTextView.setText(dateFormat.format(event.getRegistrationEndDate()));
        }

        // Set lottery drawn date
        // TODO: Add lottery drawn date to Event model once its ready
        lotteryDrawnDateTextView.setText("Monday, June 8");

        // Set description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            eventDescriptionTextView.setText(event.getDescription());
        } else {
            eventDescriptionTextView.setText("No description available.");
        }

        loadEventImage();

        // Update button state based on user's registration status
        updateUIBasedOnStatus(user);  // ADD THIS LINE
    }

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
     * Updates the UI based on the user's registration status
     * This method combines logic from updateButtonState() and handles all states
     *
     * States handled:
     * 1. Not registered + registration open → "Enter Lottery" (enabled)
     * 2. Not registered + registration closed → "Registration Closed" (disabled)
     * 3. Not registered + waitlist full → "Lottery Full" (disabled)
     * 4. Pending (before lottery drawn) → "Leave Waitlist" (blue/gray, enabled)
     * 5. Waitlisted (after lottery, not selected) → Red badge + info + red button (US 01.05.01)
     * 6. Selected → Hide all buttons (friend's screen handles this)
     * 7. Accepted → Hide all buttons
     * 8. Cancelled → Allow rejoin if possible
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
        registrationDatesCard.setVisibility(View.VISIBLE);  // ADD THIS
        aboutCard.setVisibility(View.VISIBLE);  // ADD THIS

        buttonState(status);
        if ("PENDING".equals(status)) {
            // ========== USER IS ON WAITLIST ==========

            if (lotteryDrawn) {
                // ========== WAITLISTED STATE (US 01.05.01) ==========
                // Lottery was drawn, user not selected = WAITLISTED
                // Show red badge, info card, and red "Leave Waiting List" button
                waitlistedBadgeLayout.setVisibility(View.VISIBLE);
                waitlistedInfoCard.setVisibility(View.VISIBLE);
                infoCardMessageTextView.setText("You weren't selected in the initial lottery draw, but you're still on the waiting list. Good news! You could still be selected if any chosen participants decline their invitation. If a spot opens up, you will be notified immediately if you get selected");  // ADD THIS

                // Hide registration dates and about section
                registrationDatesCard.setVisibility(View.GONE);  // ADD THIS
                aboutCard.setVisibility(View.GONE);  // ADD THIS

            }
        } else if ("CANCELLED".equals(status)) {
            // ========== CANCELLED ==========
            // User was cancelled - show info card and disabled button
            waitlistedInfoCard.setVisibility(View.VISIBLE);
            infoCardHeaderTextView.setText("Event Status");  // ADD THIS (if you added the header ID)
            infoCardMessageTextView.setText("You were cancelled for this event and cannot rejoin.");  // ADD THIS
        }
    }

    private void setupListeners(User user) {
        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigateUp();
        });
        enterLotteryButton.setOnClickListener(v -> {
            List<String> registeredEvents = user.getRegEventIds();
            boolean isRegistered = registeredEvents.contains(event.getId());

            if (isRegistered) {
                leaveWaitlist();
            } else {
                joinWaitlist();
            }
        });

        leaveWaitlistButton.setOnClickListener(v -> {
            leaveWaitlist();
        });

    }

    private void joinWaitlist() {
        if (event == null || currentUser == null) {
            Toast.makeText(getContext(), "Error: Unable to join waitlist", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks if already registered
        if (currentUser.getRegEvents().contains(event.getId())) {
            Toast.makeText(getContext(),
                    "You're already registered for this event",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        buttonState("LOADING");

        try {
            // Add event to user's registered events

            String userId = currentUser.getId();

            List<String> waitlistIds = event.getWaitlistEntrantIds();
            if (waitlistIds == null) {
                waitlistIds = new ArrayList<>();
            }
            Log.e("WEINER", Arrays.toString(waitlistIds.toArray()));
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
                    updateUIBasedOnStatus(currentUser);  // ADD THIS LINE


                    int currentEntries = event.getCurrentWaitlistCount();
                    int maxEntries = event.getMaxEntrants();
                    entriesCountTextView.setText(currentEntries + " of " + maxEntries);
                }

                @Override
                public void onError(String errorMessage) {
                    // Rollback: Remove event from user's registered events
                    currentUser.removeRegEvent(event.getId());

                    showDialogPerStatus("ERROR JOIN");

                    status = getUserStatus(currentUser);
                    buttonState(status);
                }
            });

        } catch (Exception e) {
            // Rollback
            currentUser.removeRegEvent(event.getId());

            showDialogPerStatus("ERROR JOIN");

            status = getUserStatus(currentUser);
            buttonState(status);
        }
    }

    private void leaveWaitlist() {
        if (event == null || currentUser == null) {
            showDialogPerStatus("ERROR LEAVE");
            return;
        }

        status = getUserStatus(currentUser);
        buttonState(status);
        showDialogPerStatus(status);
    }

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

            EventRepository eventRepository = new EventRepository();
            eventRepository.updateEvent(event, new OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Successfully left waitlist for " + event.getName(),
                            Toast.LENGTH_SHORT).show();
                    updateUIBasedOnStatus(currentUser);

                    int currentEntries = event.getCurrentWaitlistCount();
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
     * Checks if the lottery has been drawn for this event
     * @return true if lottery was drawn, false otherwise
     */
    private boolean hasLotteryBeenDrawn() {
        if (event == null) return false;

        List<String> selectedIds = event.getSelectedEntrantIds();

        // If there are selected entrants, lottery has been drawn
        return selectedIds != null && !selectedIds.isEmpty();
    }

    /**
     * Gets the user's registration status for this event
     * @return "PENDING", "SELECTED", "ACCEPTED", "CANCELLED", or null if not registered
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