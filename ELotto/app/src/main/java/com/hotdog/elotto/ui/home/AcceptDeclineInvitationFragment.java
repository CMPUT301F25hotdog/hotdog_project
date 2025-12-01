package com.hotdog.elotto.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;
import com.hotdog.elotto.R;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Fragment for accepting or declining event invitations with RSVP deadline.
 *
 * <p>This fragment displays comprehensive event details for a selected invitation
 * and provides users with options to accept or decline. Features a real-time countdown
 * timer showing the remaining time to respond (24 hours from selection). Automatically
 * declines the invitation and disables buttons when the deadline expires.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Event details display (title, image, date, time, location, description)</li>
 *     <li>Real-time countdown timer (24-hour RSVP deadline)</li>
 *     <li>Accept button with confirmation dialog</li>
 *     <li>Decline button with confirmation dialog</li>
 *     <li>Automatic decline and button disabling on deadline expiration</li>
 *     <li>Base64 event image decoding with fallback placeholder</li>
 *     <li>Status updates in both User and Event documents</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Ethan Carter
 * @version 1.0
 * @since 2025-11-27
 */
public class AcceptDeclineInvitationFragment extends Fragment {

    private static final String TAG = "AcceptDeclineInvitation";

    /**
     * RSVP deadline duration in hours (24 hours from selection time).
     */
    private static final long RSVP_DEADLINE_HOURS = 24;

    /**
     * Button for navigating back to the previous screen.
     */
    private ImageButton backButton;

    /**
     * TextView displaying the event title.
     */
    private TextView eventTitleTextView;

    /**
     * ImageView displaying the event poster image.
     */
    private ImageView eventImageView;

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
     * TextView displaying the countdown timer showing time remaining to RSVP.
     */
    private TextView countdownTextView;

    /**
     * TextView displaying the event description.
     */
    private TextView eventDescriptionTextView;

    /**
     * Button for declining the invitation.
     */
    private Button declineButton;

    /**
     * Button for accepting the invitation.
     */
    private Button acceptButton;

    /**
     * The event object for which the invitation is being handled.
     */
    private Event event;

    /**
     * The current user's ID.
     */
    private String currentUserId;

    /**
     * Repository for event data access operations.
     */
    private EventRepository eventRepository;

    /**
     * Repository for user data access operations.
     */
    private UserRepository userRepository;

    /**
     * Handler for posting countdown timer updates on the main thread.
     */
    private Handler countdownHandler;

    /**
     * Runnable for the countdown timer that updates every second.
     */
    private Runnable countdownRunnable;

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater the LayoutInflater object that can be used to inflate views
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState the previously saved state of the fragment
     * @return the root View of the fragment's layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accept_decline_invitation, container, false);
    }

    /**
     * Called immediately after onCreateView has returned.
     *
     * <p>Initializes all views, repositories, loads the current user, loads event
     * data from arguments, and sets up button click listeners.</p>
     *
     * @param view the View returned by onCreateView
     * @param savedInstanceState the previously saved state of the fragment
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeRepositories();
        loadCurrentUser();
        loadEventData();
        setupListeners();
    }

    /**
     * Initializes all view components and the countdown handler.
     *
     * <p>Binds UI elements by their IDs and creates a Handler for the main looper
     * to handle countdown timer updates.</p>
     *
     * @param view the root view of the fragment
     */
    private void initializeViews(View view) {
        backButton = view.findViewById(R.id.backButton);
        eventTitleTextView = view.findViewById(R.id.eventTitleTextView);
        eventImageView = view.findViewById(R.id.eventImageView);
        eventDateTextView = view.findViewById(R.id.eventDateTextView);
        eventTimeTextView = view.findViewById(R.id.eventTimeTextView);
        eventLocationTextView = view.findViewById(R.id.eventLocationTextView);
        countdownTextView = view.findViewById(R.id.countdownTextView);
        eventDescriptionTextView = view.findViewById(R.id.eventDescriptionTextView);
        declineButton = view.findViewById(R.id.declineButton);
        acceptButton = view.findViewById(R.id.acceptButton);

        countdownHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Initializes repository instances for data access.
     *
     * <p>Creates a new EventRepository instance and obtains the singleton
     * UserRepository instance.</p>
     */
    private void initializeRepositories() {
        eventRepository = new EventRepository();
        userRepository = UserRepository.getInstance();
    }

    /**
     * Loads the current user and stores their ID.
     *
     * <p>Creates a User instance with a callback to extract and store the
     * current user's device ID for subsequent operations.</p>
     */
    private void loadCurrentUser() {
        new User(requireContext(), new Consumer<User>() {
            @Override
            public void accept(User user) {
                currentUserId = user.getId();
            }
        });
    }

    /**
     * Loads event data from fragment arguments.
     *
     * <p>Retrieves the Event object passed through navigation arguments, displays
     * the event details, and checks the invitation status to start the countdown
     * timer. If no event is found, shows an error and navigates back.</p>
     */
    private void loadEventData() {
        // Get event from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");

            if (event != null) {
                displayEventDetails();
                checkInvitationStatus();
            } else {
                Toast.makeText(getContext(), "Error loading event", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        }
    }

    /**
     * Displays all event details in the UI.
     *
     * <p>Populates TextViews with event information including title, date (formatted
     * as "EEEE, MMMM d"), time (formatted as "HH:mm"), location, and description.
     * Calls loadEventImage to handle the poster image display.</p>
     */
    private void displayEventDetails() {
        // Set event title
        eventTitleTextView.setText(event.getName());

        // Set event image
        loadEventImage();

        // Set event date
        if (event.getEventDateTime() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            eventDateTextView.setText(dateFormat.format(event.getEventDateTime()));
        }

        // Set event time
        if (event.getEventDateTime() != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startTime = timeFormat.format(event.getEventDateTime());
            eventTimeTextView.setText(startTime + " - 17:30");
        }

        // Set event location
        eventLocationTextView.setText(event.getLocation());

        // Set event description
        eventDescriptionTextView.setText(event.getDescription());
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
            // No image or failed image so keep placeholder
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
     * Checks the invitation status and starts the countdown timer.
     *
     * <p>Loads the user from Firestore, finds the registered event using binary
     * search, retrieves the selectedDate timestamp, and starts the countdown timer
     * based on that timestamp. If selectedDate is null, uses the current time as
     * fallback. Navigates back if the event is not found in registrations.</p>
     */
    private void checkInvitationStatus() {
        // Load user to get selectedDate
        userRepository.getUserById(currentUserId, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Find the registered event
                int index = Collections.binarySearch(
                        user.getRegEvents(),
                        new User.RegisteredEvent(event.getId())
                );

                if (index >= 0) {
                    User.RegisteredEvent regEvent = user.getRegEvents().get(index);
                    Timestamp selectedDate = regEvent.getSelectedDate();

                    if (selectedDate != null) {
                        startCountdownTimer(selectedDate);
                    } else {

                        startCountdownTimer(Timestamp.now());
                    }
                } else {
                    Toast.makeText(getContext(), "Event not found in your registrations", Toast.LENGTH_SHORT).show();
                    navigateBack();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading user: " + errorMessage);
                Toast.makeText(getContext(), "Error loading invitation details", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    /**
     * Starts the countdown timer that updates every second.
     *
     * <p>Creates and posts a Runnable that calls updateCountdown every 1000ms
     * to display the remaining time to respond to the invitation.</p>
     *
     * @param selectedDate the timestamp when the user was selected for the event
     */
    private void startCountdownTimer(Timestamp selectedDate) {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown(selectedDate);
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    /**
     * Updates the countdown display with remaining time.
     *
     * <p>Calculates the deadline as 24 hours from selectedDate, computes the
     * remaining time, and displays it in "Xh Ym left to RSVP" format. If the
     * deadline has passed, calls handleExpiredInvitation.</p>
     *
     * @param selectedDate the timestamp when the user was selected for the event
     */
    private void updateCountdown(Timestamp selectedDate) {
        // Calculate deadline (24 hours from selectedDate)
        long deadlineMillis = selectedDate.toDate().getTime() +
                TimeUnit.HOURS.toMillis(RSVP_DEADLINE_HOURS);
        long currentMillis = System.currentTimeMillis();
        long remainingMillis = deadlineMillis - currentMillis;

        if (remainingMillis <= 0) {

            handleExpiredInvitation();
        } else {

            long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;

            String countdownText = hours + "h " + minutes + "m left to RSVP";
            countdownTextView.setText(countdownText);
        }
    }

    /**
     * Handles expired invitations by disabling buttons and auto-declining.
     *
     * <p>Updates the countdown text to "Invitation expired", disables both accept
     * and decline buttons, and calls autoDeclineUser to automatically move the user
     * to declined status.</p>
     */
    private void handleExpiredInvitation() {
        countdownTextView.setText("Invitation expired");

        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);


        autoDeclineUser();
    }

    /**
     * Automatically declines the user when the RSVP deadline expires.
     *
     * <p>Loads the user, updates their status to Declined for the event, saves the
     * change to Firestore, and then moves the user to the cancelled list in the
     * event document.</p>
     */
    private void autoDeclineUser() {
        // Move user to Declined status and cancelled list
        userRepository.getUserById(currentUserId, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                try {
                    // Update user status to Declined
                    user.setRegEventStatus(event.getId(), Status.Declined);


                    userRepository.updateUser(user, new OperationCallback() {
                        @Override
                        public void onSuccess() {

                            moveUserToCancelledList();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Error updating user status: " + errorMessage);
                        }
                    });
                } catch (NoSuchFieldException e) {
                    Log.e(TAG, "Event not found in user's registered events", e);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading user for auto-decline: " + errorMessage);
            }
        });
    }

    /**
     * Sets up click listeners for all interactive buttons.
     *
     * <p>Configures the back button to navigate back, accept button to show
     * confirmation dialog, and decline button to show confirmation dialog.</p>
     */
    private void setupListeners() {
        backButton.setOnClickListener(v -> navigateBack());

        acceptButton.setOnClickListener(v -> showAcceptConfirmation());

        declineButton.setOnClickListener(v -> showDeclineConfirmation());
    }

    /**
     * Shows a confirmation dialog for accepting the invitation.
     *
     * <p>Displays an AlertDialog asking the user to confirm acceptance. On positive
     * response, calls acceptInvitation to process the acceptance.</p>
     */
    private void showAcceptConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Accept Invitation")
                .setMessage("Are you sure you want to accept this invitation? You will be registered for the event.")
                .setPositiveButton("Yes, Accept", (dialog, which) -> acceptInvitation())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows a confirmation dialog for declining the invitation.
     *
     * <p>Displays an AlertDialog asking the user to confirm decline with a warning
     * that the action cannot be undone. On positive response, calls declineInvitation
     * to process the decline.</p>
     */
    private void showDeclineConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline this invitation? This action cannot be undone.")
                .setPositiveButton("Yes, Decline", (dialog, which) -> declineInvitation())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Processes the invitation acceptance.
     *
     * <p>Loads the user, updates their status to Accepted for the event, saves the
     * change to Firestore, and calls moveUserToAcceptedList to update the event's
     * accepted list. Shows error toasts if any operation fails.</p>
     */
    private void acceptInvitation() {
        // Update user status to Accepted
        userRepository.getUserById(currentUserId, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                try {
                    user.setRegEventStatus(event.getId(), Status.Accepted);

                    userRepository.updateUser(user, new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            // Move to accepted list in event
                            moveUserToAcceptedList();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Error updating user: " + errorMessage);
                            Toast.makeText(getContext(), "Error accepting invitation", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (NoSuchFieldException e) {
                    Log.e(TAG, "Event not found", e);
                    Toast.makeText(getContext(), "Error accepting invitation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading user: " + errorMessage);
                Toast.makeText(getContext(), "Error accepting invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Moves the user to the accepted list in the event document.
     *
     * <p>Calls the EventRepository to move the user from the selected list to the
     * accepted list. Shows a success toast and navigates back on completion, or
     * shows an error toast on failure.</p>
     */
    private void moveUserToAcceptedList() {
        List<String> userIds = new ArrayList<>();
        userIds.add(currentUserId);

        eventRepository.moveEntrantsToAccepted(event.getId(), userIds, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Invitation accepted!", Toast.LENGTH_SHORT).show();
                navigateBack();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error moving to accepted list: " + errorMessage);
                Toast.makeText(getContext(), "Error accepting invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Processes the invitation decline.
     *
     * <p>Loads the user, updates their status to Declined for the event, saves the
     * change to Firestore, and calls moveUserToCancelledList to update the event's
     * cancelled list. Shows error toasts if any operation fails.</p>
     */
    private void declineInvitation() {
        // Update user status to Declined
        userRepository.getUserById(currentUserId, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                try {
                    user.setRegEventStatus(event.getId(), Status.Declined);

                    userRepository.updateUser(user, new OperationCallback() {
                        @Override
                        public void onSuccess() {

                            moveUserToCancelledList();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Error updating user: " + errorMessage);
                            Toast.makeText(getContext(), "Error declining invitation", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (NoSuchFieldException e) {
                    Log.e(TAG, "Event not found", e);
                    Toast.makeText(getContext(), "Error declining invitation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading user: " + errorMessage);
                Toast.makeText(getContext(), "Error declining invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Moves the user to the cancelled list in the event document.
     *
     * <p>Calls the EventRepository to move the user from the selected list to the
     * cancelled list. Shows a success toast and navigates back on completion, or
     * shows an error toast on failure.</p>
     */
    private void moveUserToCancelledList() {
        List<String> userIds = new ArrayList<>();
        userIds.add(currentUserId);

        eventRepository.moveEntrantsToCancelled(event.getId(), userIds, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                navigateBack();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error moving to cancelled list: " + errorMessage);
                Toast.makeText(getContext(), "Error declining invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigates back to the previous screen using the Navigation Controller.
     */
    private void navigateBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     *
     * <p>Removes the countdown runnable from the handler to prevent memory leaks and
     * stop the timer from continuing to update after the view is destroyed.</p>
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop countdown timer
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }
}