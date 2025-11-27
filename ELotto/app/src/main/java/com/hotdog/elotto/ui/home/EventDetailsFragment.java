package com.hotdog.elotto.ui.home;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.GeoPoint;
import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.LocationController;
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
    private Button mapButton;
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
        mapButton = view.findViewById(R.id.MapButton);
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
        updateButtonState();
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
    private void updateButtonState() {
        if (event == null || currentUser == null) return;

        // Check if user is already registered for this event
        List<String> registeredEvents = currentUser.getRegEventIds();
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
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigateUp();
        });
        enterLotteryButton.setOnClickListener(v ->{
            List<String> registeredEvents = currentUser.getRegEventIds();
            boolean isRegistered = registeredEvents.contains(event.getId());

            if (isRegistered){
                leaveWaitlist();
            } else{
                joinWaitlist();
            }
        });
        mapButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);

            Bundle args = new Bundle();
            args.putString("eventId", event.getId());

            navController.navigate(R.id.eventMapFragment, args);
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


        enterLotteryButton.setEnabled(false);
        enterLotteryButton.setText("Joining...");

        try {
            // Add event to user's registered events
            currentUser.addRegEvent(event.getId());

            String userId = currentUser.getId();

            List<String> waitlistIds = event.getWaitlistEntrantIds();
            if (waitlistIds == null) {
                waitlistIds = new ArrayList<>();
                event.setWaitlistEntrantIds(waitlistIds);
            }
            if (!waitlistIds.contains(userId)) {
                waitlistIds.add(userId);
            }
            LocationController locationController = new LocationController(getContext());
            locationController.getLatLon(new LocationController.LocationCallBack() {
                @Override
                public void onLocationReady(double lat, double lon) {
                    if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                        event.setEntrantLocations(userId, new GeoPoint(lat, lon));
                    }
                    else{
                        event.setEntrantLocations(userId, new GeoPoint(-1,-1));
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


                            enterLotteryButton.setEnabled(true);
                            enterLotteryButton.setText("Enter Lottery");
                        }
                    });

                }
            });
        }catch (Exception e) {
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
            boolean removed = currentUser.removeRegEvent(event.getId());

            // this should never happen
            if (!removed) {
                Toast.makeText(getContext(), "Event not found in your registered events", Toast.LENGTH_SHORT).show();
                enterLotteryButton.setEnabled(true);
                enterLotteryButton.setText("Leave Waitlist");
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
                    Toast.makeText(getContext(), "Successfully left waitlist for " + event.getName(), Toast.LENGTH_SHORT).show();

                    updateButtonState();
                }

                @Override
                public void onError(String errorMessage) {
                    // Rollback: Add event back to user's registered events
                    currentUser.addRegEvent(event.getId());

                    Toast.makeText(getContext(),
                            "Error leaving waitlist: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    enterLotteryButton.setEnabled(true);
                    enterLotteryButton.setText("Leave Waitlist");
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error leaving waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            enterLotteryButton.setEnabled(true);
            enterLotteryButton.setText("Leave Waitlist");
        }
    }}