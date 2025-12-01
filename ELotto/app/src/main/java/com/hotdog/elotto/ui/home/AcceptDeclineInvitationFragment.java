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
 * Fragment for accepting or declining event invitations.
 *
 * @author Ethan Carter
 * @version 1.0
 * @since 2025-11-27
 */
public class AcceptDeclineInvitationFragment extends Fragment {

    private static final String TAG = "AcceptDeclineInvitation";
    private static final long RSVP_DEADLINE_HOURS = 24;
    private ImageButton backButton;
    private TextView eventTitleTextView;
    private ImageView eventImageView;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;
    private TextView eventLocationTextView;
    private TextView countdownTextView;
    private TextView eventDescriptionTextView;
    private Button declineButton;
    private Button acceptButton;
    private Event event;
    private String currentUserId;
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accept_decline_invitation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeRepositories();
        loadCurrentUser();
        loadEventData();
        setupListeners();
    }

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

    private void initializeRepositories() {
        eventRepository = new EventRepository();
        userRepository = UserRepository.getInstance();
    }

    private void loadCurrentUser() {
        new User(requireContext(), new Consumer<User>() {
            @Override
            public void accept(User user) {
                currentUserId = user.getId();
            }
        });
    }

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

    private void handleExpiredInvitation() {
        countdownTextView.setText("Invitation expired");

        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);


        autoDeclineUser();
    }

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

    private void setupListeners() {
        backButton.setOnClickListener(v -> navigateBack());

        acceptButton.setOnClickListener(v -> showAcceptConfirmation());

        declineButton.setOnClickListener(v -> showDeclineConfirmation());
    }

    private void showAcceptConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Accept Invitation")
                .setMessage("Are you sure you want to accept this invitation? You will be registered for the event.")
                .setPositiveButton("Yes, Accept", (dialog, which) -> acceptInvitation())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeclineConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline this invitation? This action cannot be undone.")
                .setPositiveButton("Yes, Decline", (dialog, which) -> declineInvitation())
                .setNegativeButton("Cancel", null)
                .show();
    }

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

    private void moveUserToCancelledList() {
        List<String> userIds = new ArrayList<>();
        userIds.add(currentUserId);

        eventRepository.moveEntrantsToCancelled(event.getId(), userIds, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                navigateBack();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error moving to cancelled list: " + errorMessage);
                Toast.makeText(getContext(), "Error declining invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop countdown timer
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }
}