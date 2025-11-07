package com.hotdog.elotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EventDetailsFragment displays detailed information about a selected event.
 *
 *
 * Outstanding Issues:
 * - Image loading not yet implemented
 * - Need to add event to Event's waitlist in Firebase (currently only updates User)
 * - Lottery drawn date needs to be added to Event model
 */
public class EventDetailsFragment extends Fragment {


    private Event event;
    private User currentUser;

    // UI Components
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

        // Load current user
        currentUser = new User(requireContext(), true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        initializeViews(view);
        populateEventData();
        setupListeners();

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
    }

    private void populateEventData() {
        if (event == null) return;

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
        // TODO: Add lottery drawn date to Event model
        lotteryDrawnDateTextView.setText("Monday, June 8");

        // Set description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            eventDescriptionTextView.setText(event.getDescription());
        } else {
            eventDescriptionTextView.setText("No description available.");
        }

        // TODO: Load event image from Url
        // For now, placeholder will show

        // Update button state based on user's registration status
        updateButtonState();
    }

    private void updateButtonState() {
        if (event == null || currentUser == null) return;

        // Check if user is already registered for this event
        List<String> registeredEvents = currentUser.getRegEvents();
        boolean isRegistered = registeredEvents.contains(event.getId());

        if (isRegistered) {
            // User already registered - show leave waitlist button
            enterLotteryButton.setText("Leave Waitlist");
            enterLotteryButton.setEnabled(true);
            enterLotteryButton.setBackgroundResource(R.drawable.button_leave_background);
        } else if (!event.isRegistrationOpen()) {
            // Registration closed
            enterLotteryButton.setText("Registration Closed");
            enterLotteryButton.setEnabled(false);
            enterLotteryButton.setBackgroundResource(R.drawable.button_disabled_background);
        } else if (event.isFull()) {
            // Waitlist is full
            enterLotteryButton.setText("Lottery Full");
            enterLotteryButton.setEnabled(false);
            enterLotteryButton.setBackgroundResource(R.drawable.button_disabled_background);
        } else {
            // Available to join
            enterLotteryButton.setText("Enter Lottery");
            enterLotteryButton.setEnabled(true);
            enterLotteryButton.setBackgroundResource(R.drawable.button_primary_background);
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        enterLotteryButton.setOnClickListener(v ->{
            List<String> registeredEvents = currentUser.getRegEvents();
            boolean isRegistered = registeredEvents.contains(event.getId());

            if (isRegistered){
                leaveWaitlist();
            } else{
                joinWaitlist();
            }
        });

    }

    private void joinWaitlist() {
        if (event == null || currentUser == null) {
            Toast.makeText(getContext(), "Error: Unable to join waitlist", Toast.LENGTH_SHORT).show();
            return;
        }


        // Check if already registered
        if (currentUser.getRegEvents().contains(event.getId())) {
            Toast.makeText(getContext(),
                    "You're already registered for this event",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button while processing
        enterLotteryButton.setEnabled(false);
        enterLotteryButton.setText("Joining...");

        try {
            // Add event to user's registered events
            // This automatically saves to Firebase via User's controller
            currentUser.addRegEvent(event.getId());

            String userId = currentUser.getId();

            List<String> waitlistIds = event.getWaitlistEntrantIds();
            if (waitlistIds == null){
                waitlistIds = new ArrayList<>();
                event.setWaitlistEntrantIds(waitlistIds);
            }
            if (!waitlistIds.contains(userId)){
                waitlistIds.add(userId);
            }

            EventRepository eventRepository = new EventRepository();
            eventRepository.updateEvent(event, new OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(),
                            "Successfully joined waitlist for " + event.getName(),
                            Toast.LENGTH_SHORT).show();
                    updateButtonState();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(getContext(),
                            "Error joining waitlist: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // show button again
                    enterLotteryButton.setEnabled(true);
                    enterLotteryButton.setText("Enter Lottery");
                }
            });

            } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Error joining waitlist: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();


            enterLotteryButton.setEnabled(true);
            enterLotteryButton.setText("Enter Lottery");
        }
    }

    private void leaveWaitlist() {
        if (event == null || currentUser == null) {
            Toast.makeText(getContext(), "Error: Unable to leave waitlist", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirm with user first
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Leave Waitlist")
                .setMessage("Are you sure you want to leave the waitlist for " + event.getName() + "?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    performLeaveWaitlist();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLeaveWaitlist() {
        enterLotteryButton.setEnabled(false);
        enterLotteryButton.setText("Leaving...");

        try {
            String userId = currentUser.getId();
            List<String> waitlistIds = event.getWaitlistEntrantIds();
            if (waitlistIds != null) {
                waitlistIds.remove(userId);
            }

            // Update the event in Firebase
            EventRepository eventRepository = new EventRepository();
            eventRepository.updateEvent(event, new OperationCallback() {
                @Override
                public void onSuccess() {
                    // Step 2: Remove event from user's registered events
                    // Note: User class needs a removeRegEvent method
                    // For now, we'll need to reload the user

                    Toast.makeText(getContext(),
                            "Successfully left waitlist for " + event.getName(),
                            Toast.LENGTH_SHORT).show();

                    // Reload user data to reflect changes
                    currentUser.atomicReload();

                    // Update button state
                    updateButtonState();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(getContext(),
                            "Error leaving waitlist: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    enterLotteryButton.setEnabled(true);
                    enterLotteryButton.setText("Leave Waitlist");
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Error leaving waitlist: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();

            // Re-enable button
            enterLotteryButton.setEnabled(true);
            enterLotteryButton.setText("Leave Waitlist");
        }
    }
}