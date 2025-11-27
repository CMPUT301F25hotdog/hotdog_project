package com.hotdog.elotto.controller;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.EntrantInfo;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Controller for organizer event management operations.
 * Handles business logic for viewing entrants, running lottery draws, and
 * sending notifications.
 *
 * <p>
 * This controller coordinates between EventRepository and UserRepository to
 * provide
 * complete entrant information including user details and registration dates.
 *
 * <p>
 * Controller layer in MVC pattern.
 *
 * @author Bhuvnesh Batta
 * @version 1.0
 * @since 2025-11-24
 */
public class OrganizerEventController {
    private static final String TAG = "OrganizerEventController";
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationController notificationController;

    /**
     * Default constructor that initializes repositories.
     */
    public OrganizerEventController() {
        this.eventRepository = new EventRepository();
        this.userRepository = UserRepository.getInstance();
        this.notificationController = new NotificationController();
    }

    /**
     * Constructor for dependency injection (useful for testing).
     *
     * @param eventRepository the event repository instance
     * @param userRepository  the user repository instance
     */
    public OrganizerEventController(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationController = new NotificationController();
    }

    /**
     * Loads entrants from the waiting list with their user information and join
     * dates.
     *
     * @param eventId  the event ID to load waiting list from
     * @param callback the callback to receive list of EntrantInfo or error
     */
    public void loadWaitingListEntrants(String eventId, FirestoreListCallback<EntrantInfo> callback) {
        loadEntrantsByList(eventId, "waitlist", callback);
    }

    /**
     * Loads selected entrants with their user information and join dates.
     *
     * @param eventId  the event ID to load selected entrants from
     * @param callback the callback to receive list of EntrantInfo or error
     */
    public void loadSelectedEntrants(String eventId, FirestoreListCallback<EntrantInfo> callback) {
        loadEntrantsByList(eventId, "selected", callback);
    }

    /**
     * Loads accepted entrants with their user information and join dates.
     *
     * @param eventId  the event ID to load accepted entrants from
     * @param callback the callback to receive list of EntrantInfo or error
     */
    public void loadAcceptedEntrants(String eventId, FirestoreListCallback<EntrantInfo> callback) {
        loadEntrantsByList(eventId, "accepted", callback);
    }

    /**
     * Loads cancelled entrants with their user information and join dates.
     *
     * @param eventId  the event ID to load cancelled entrants from
     * @param callback the callback to receive list of EntrantInfo or error
     */
    public void loadCancelledEntrants(String eventId, FirestoreListCallback<EntrantInfo> callback) {
        loadEntrantsByList(eventId, "cancelled", callback);
    }

    /**
     * Helper method to load entrants from a specific list.
     *
     * @param eventId  the event ID
     * @param listType the type of list ("waitlist", "selected", "accepted",
     *                 "cancelled")
     * @param callback the callback to receive results
     */
    private void loadEntrantsByList(String eventId, String listType, FirestoreListCallback<EntrantInfo> callback) {
        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                // Get the appropriate list of user IDs based on listType
                List<String> userIds = getUserIdsFromEvent(event, listType);

                if (userIds == null || userIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Fetch all users for these IDs
                userRepository.getUsersByIds(userIds, new FirestoreListCallback<User>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        // Combine users with their join dates
                        List<EntrantInfo> entrantInfoList = new ArrayList<>();

                        for (User user : users) {
                            Date joinedDate = getJoinedDateForUser(user, eventId);
                            entrantInfoList.add(new EntrantInfo(user, joinedDate));
                        }

                        Log.d(TAG, "Successfully loaded " + entrantInfoList.size() + " entrants from " + listType);
                        callback.onSuccess(entrantInfoList);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error loading users: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading event: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Helper method to get the appropriate user ID list from an event based on list
     * type.
     *
     * @param event    the event object
     * @param listType the type of list to retrieve
     * @return list of user IDs
     */
    private List<String> getUserIdsFromEvent(Event event, String listType) {
        switch (listType) {
            case "waitlist":
                return event.getWaitlistEntrantIds();
            case "selected":
                return event.getSelectedEntrantIds();
            case "accepted":
                return event.getAcceptedEntrantIds();
            case "cancelled":
                return event.getCancelledEntrantIds();
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Extracts the joined date for a specific event from a user's registered
     * events.
     *
     * @param user    the user object
     * @param eventId the event ID to find
     * @return the date when user registered, or current date if not found
     */
    private Date getJoinedDateForUser(User user, String eventId) {
        if (user.getRegEvents() != null) {
            for (User.RegisteredEvent regEvent : user.getRegEvents()) {
                if (regEvent.getEventId().equals(eventId)) {
                    Timestamp timestamp = regEvent.getRegisteredDate();
                    return timestamp != null ? timestamp.toDate() : new Date();
                }
            }
        }
        return new Date(); // Default to current date if not found
    }

    /**
     * Runs the lottery draw to randomly select entrants from the waiting list.
     *
     * @param eventId        the event ID to run lottery for
     * @param numberToSelect the number of entrants to select
     * @param callback       the callback to receive success or error
     */
    public void runLotteryDraw(String eventId, int numberToSelect, OperationCallback callback) {
        // Validation
        if (numberToSelect <= 0) {
            callback.onError("Number to select must be greater than 0");
            return;
        }

        // Get the event
        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                List<String> waitlistIds = event.getWaitlistEntrantIds();

                // Validate waitlist
                if (waitlistIds == null || waitlistIds.isEmpty()) {
                    callback.onError("Waiting list is empty");
                    return;
                }

                if (numberToSelect > waitlistIds.size()) {
                    callback.onError("Cannot select " + numberToSelect + " entrants. Only " + waitlistIds.size()
                            + " in waiting list");
                    return;
                }

                // Perform lottery draw - shuffle and select
                List<String> shuffled = new ArrayList<>(waitlistIds);
                Collections.shuffle(shuffled);
                List<String> winners = shuffled.subList(0, numberToSelect);
                List<String> losers = shuffled.subList(numberToSelect, shuffled.size());

                // Move winners to selected list
                eventRepository.moveEntrantsToSelected(eventId, winners, new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Lottery draw completed. Selected " + winners.size() + " entrants");

                        // Update user statuses and send notifications
                        updateUserStatusesAndNotify(event, winners, losers, callback);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error moving entrants to selected: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading event for lottery: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Updates user statuses after lottery draw and sends notifications.
     *
     * @param event    the event object
     * @param winners  list of winning user IDs
     * @param losers   list of losing user IDs
     * @param callback the callback to receive final success or error
     */
    private void updateUserStatusesAndNotify(Event event, List<String> winners, List<String> losers,
            OperationCallback callback) {
        // Send win notifications
        for (String userId : winners) {
            notificationController.sendNotification(
                    userId,
                    "Lottery Win",
                    "Congratulations! You have been selected in the lottery draw for " + event.getName() + ".",
                    event.getId(),
                    event.getName(),
                    event.getPosterImageUrl(),
                    new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Win notification sent to: " + userId);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Failed to send win notification: " + errorMessage);
                        }
                    });
        }

        // Send lose notifications
        for (String userId : losers) {
            notificationController.sendNotification(
                    userId,
                    "Lottery Result",
                    "Unfortunately, you were not selected in the lottery draw for " + event.getName()
                            + ". You remain on the waiting list.",
                    event.getId(),
                    event.getName(),
                    event.getPosterImageUrl(),
                    new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Lose notification sent to: " + userId);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Failed to send lose notification: " + errorMessage);
                        }
                    });
        }

        // Return success after initiating all notifications
        callback.onSuccess();

    }

    /**
     * Sends a custom notification to a list of entrants.
     *
     * @param eventId  the event ID
     * @param userIds  the list of user IDs to notify
     * @param message  the notification message
     * @param callback the callback to receive success or error
     */
    public void sendNotificationToEntrants(String eventId, List<String> userIds, String message,
            OperationCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onError("No entrants to notify");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            callback.onError("Notification message cannot be empty");
            return;
        }

        // Fetch event details first
        eventRepository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                int totalNotifications = userIds.size();
                int[] successCount = { 0 };
                int[] errorCount = { 0 };

                for (String userId : userIds) {
                    notificationController.sendNotification(userId, "Event Update", message, eventId, event.getName(),
                            event.getPosterImageUrl(), new OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    successCount[0]++;
                                    Log.d(TAG, "Notification sent to: " + userId);

                                    // Check if all notifications have been processed
                                    if (successCount[0] + errorCount[0] == totalNotifications) {
                                        if (errorCount[0] == 0) {
                                            callback.onSuccess();
                                        } else {
                                            callback.onError("Some notifications failed. " + successCount[0] + " sent, "
                                                    + errorCount[0]
                                                    + " failed");
                                        }
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    errorCount[0]++;
                                    Log.e(TAG, "Failed to send notification to " + userId + ": " + errorMessage);

                                    // Check if all notifications have been processed
                                    if (successCount[0] + errorCount[0] == totalNotifications) {
                                        if (successCount[0] > 0) {
                                            callback.onError("Some notifications failed. " + successCount[0] + " sent, "
                                                    + errorCount[0]
                                                    + " failed");
                                        } else {
                                            callback.onError("All notifications failed");
                                        }
                                    }
                                }
                            });
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Failed to fetch event details: " + errorMessage);
            }
        });
    }
}