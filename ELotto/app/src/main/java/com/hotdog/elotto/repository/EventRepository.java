package com.hotdog.elotto.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class responsible for managing Event data access with Firebase Firestore.
 *
 * <p>This class handles all CRUD (Create, Read, Update, Delete) operations for Event objects
 * in the Firebase Firestore database.
 *
 * <p>All database operations are asynchronous and use callback interfaces to return results
 * to the calling code.
 *
 * <p>the data access layer in the Model.
 *
 * <p><b>Design Pattern:</b> Repository pattern which centralizes data access logic and provides
 * a clean API for data operations.
 *
 *
 * @author Ethan Carter
 * @version 1.0
 * @since 2025-10-28
 */
public class EventRepository {
    // initialize our collection name "events" and our Firestore db.
    private static final String COLLECTION_NAME = "events";
    private final FirebaseFirestore db;

    /**
     * Constructs a new EventRepository instance.
     * Initializes the Firebase Firestore instance for database operations.
     */
    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves all events from the Firestore database and turns them into Event objects.
     * Utilizes the callback interfaces.
     *
     * <p>Events are returned in the order they are stored in Firestore
     * The Controller will deal with sorting.
     *
     * @param callback the callback to receive the list of events or error message
     */
    public void getAllEvents(FirestoreListCallback<Event> callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        events.add(event);
                    }

                    Log.d("EventRepository", "Successfully fetched " + events.size() + " events");
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error fetching events", e);
                    callback.onError("Failed to fetch events: " + e.getMessage());
                });
    }

    /**
     * Retrieves a single event by its unique ID from Firestore.
     *
     * @param eventId the unique identifier of the event to retrieve
     * @param callback the callback to receive the event or error message
     */
    public void getEventById(String eventId, FirestoreCallback<Event> callback) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        event.setId(documentSnapshot.getId());
                        Log.d("EventRepository", "Successfully fetched event: " + eventId);
                        callback.onSuccess(event);
                    } else {
                        Log.w("EventRepository", "Event not found: " + eventId);
                        callback.onError("Event not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error fetching event: " + eventId, e);
                    callback.onError("Failed to fetch event: " + e.getMessage());
                });
    }

    /**
     * Retrieves all events with the ids given.
     *
     * @param eventIds the unique identifier of the event to retrieve
     * @param callback the callback to receive the event or error message
     */
    public void getEventsById(List<String> eventIds, FirestoreCallback<List<Event>> callback) {
        if(eventIds.isEmpty()) {
            Log.e("EventRepository", "No Event IDs provided.");
            callback.onError("No Event IDs provided.");
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : eventIds) {
            tasks.add(db.collection(COLLECTION_NAME).document(id).get());
        }

        Tasks.whenAllComplete(tasks).addOnSuccessListener( doneTasks -> {
                List<Event> events = new ArrayList<>();
                for(Task<DocumentSnapshot> task : tasks) {
                    if(task.isSuccessful()) {
                        DocumentSnapshot snap = task.getResult();
                        events.add(snap.toObject(Event.class));
                    }
                }
                callback.onSuccess(events);
            }
        ).addOnFailureListener(e -> {
            Log.e("EventRepository", "Error fetching events: " + Arrays.toString(eventIds.toArray()), e);
            callback.onError("Failed to fetch event: " + e.getMessage());
        });
    }

    /**
     * Retrieves all events created by a specific organizer.
     *
     * @param organizerId the unique identifier of the organizer
     * @param callback the callback to receive the list of events or error message
     */
    public void getEventsByOrganizer(String organizerId, FirestoreListCallback<Event> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        events.add(event);
                    }

                    Log.d("EventRepository", "Successfully fetched " + events.size() + " events for organizer: " + organizerId);
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error fetching events for organizer: " + organizerId, e);
                    callback.onError("Failed to fetch organizer events: " + e.getMessage());
                });
    }

    /**
     * Retrieves all events with a specific status (e.g., "OPEN", "CLOSED", "FULL").
     *
     * @param status the status to filter by
     * @param callback the callback to receive the list of events or error message
     */
    public void getEventsByStatus(String status, FirestoreListCallback<Event> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        events.add(event);
                    }

                    Log.d("EventRepository", "Successfully fetched " + events.size() + " events with status: " + status);
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error fetching events by status: " + status, e);
                    callback.onError("Failed to fetch events by status: " + e.getMessage());
                });
    }

    /**
     * Creates a new event in the Firestore database.
     * Firestore will automatically generate a unique document ID for the event.
     *
     * @param event the Event object to create in the database
     * @param callback the callback to receive success confirmation or error message
     */
    public void createEvent(Event event, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    event.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error creating event", e);
                    callback.onError("Failed to create event: " + e.getMessage());
                });
    }

    /**
     * Updates an existing event in the Firestore database.
     * The event must have a valid ID set.
     *
     * @param event the Event object with updated data
     * @param callback the callback to receive success confirmation or error message
     */
    public void updateEvent(Event event, OperationCallback callback) {
        if (event.getId() == null || event.getId().isEmpty()) {
            callback.onError("Cannot update event: ID is null or empty");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventRepository", "Event updated successfully: " + event.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error updating event: " + event.getId(), e);
                    callback.onError("Failed to update event: " + e.getMessage());
                });
    }
    /**
     * Updates an existing event in the Firestore database using eventId and Event object.
     */
    public void updateEvent(String eventId, Event event, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventRepository", "Event updated successfully: " + eventId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error updating event: " + eventId, e);
                    callback.onError("Failed to update event: " + e.getMessage());
                });
    }

    /**
     * Deletes an event from the Firestore database.
     *
     * @param eventId the unique identifier of the event to delete
     * @param callback the callback to receive success confirmation or error message
     */
    public void deleteEvent(String eventId, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventRepository", "Event deleted successfully: " + eventId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error deleting event: " + eventId, e);
                    callback.onError("Failed to delete event: " + e.getMessage());
                });
    }

    /**
     * Deletes an event from the Firestore database atomically, blocking the main thread until finished execution.
     *
     * @param eventId the unique identifier of the event to delete
     * @param callback the callback to receive success confirmation or error message
     */
    public void deleteEvent(String eventId, OperationCallback callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .delete()
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("EventRepository", "Event deleted successfully: " + eventId);
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("EventRepository", "Error deleting event: " + eventId, e);
                    callback.onError("Failed to delete event: " + e.getMessage());
                });
    }

    /**
     * Adds an entrant to an event's waiting list.
     *
     * @param eventId the unique identifier of the event
     * @param entrantId the unique identifier of the entrant to add
     * @param callback the callback to receive success confirmation or error message
     */
    public void addEntrantToWaitlist(String eventId, String entrantId, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);

                    // Try to add to waitlist if it exists, if not then create the waitlist.
                    if (event != null) {
                        event.setId(documentSnapshot.getId());
                        List<String> waitlist = event.getWaitlistEntrantIds();
                        if (waitlist == null) {
                            waitlist = new ArrayList<>();
                        }
                        // check that the entrant isn't already in the waitlist to prevent duplicate joining
                        if (!waitlist.contains(entrantId)) {
                            waitlist.add(entrantId);
                            event.setWaitlistEntrantIds(waitlist); // set the waitlist with the updated list of ids

                            updateEvent(event, callback);
                        } else {
                            callback.onError("Entrant already on waiting list");
                        }
                    } else {
                        callback.onError("Event not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error adding entrant to waitlist", e);
                    callback.onError("Failed to add entrant to waitlist: " + e.getMessage());
                });
    }

    /**
     * Removes an entrant from an event's waiting list.
     *
     * @param eventId the unique identifier of the event
     * @param entrantId the unique identifier of the entrant to remove
     * @param callback the callback to receive success confirmation or error message
     */
    public void removeEntrantFromWaitlist(String eventId, String entrantId, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);

                    if (event != null) {
                        event.setId(documentSnapshot.getId());
                        List<String> waitlist = event.getWaitlistEntrantIds();
                        if (waitlist != null && waitlist.contains(entrantId)) {
                            waitlist.remove(entrantId);
                            event.setWaitlistEntrantIds(waitlist);

                            updateEvent(event, callback);
                        } else {
                            callback.onError("Entrant not found on waiting list");
                        }
                    } else {
                        callback.onError("Event not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error removing entrant from waitlist", e);
                    callback.onError("Failed to remove entrant from waitlist: " + e.getMessage());
                });
    }
    /**
     * Moves entrants from waiting list to selected list (lottery draw).
     * Removes the specified user IDs from waitlistEntrantIds and adds them to selectedEntrantIds.
     *
     * @param eventId the unique identifier of the event
     * @param selectedUserIds the list of user IDs that won the lottery
     * @param callback the callback to receive success confirmation or error message
     */
    public void moveEntrantsToSelected(String eventId, List<String> selectedUserIds, OperationCallback callback) {
        if (selectedUserIds == null || selectedUserIds.isEmpty()) {
            callback.onError("No entrants selected");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Event not found");
                        return;
                    }

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) {
                        callback.onError("Failed to parse event data");
                        return;
                    }

                    event.setId(documentSnapshot.getId());

                    // Get or initialize lists
                    List<String> waitlist = event.getWaitlistEntrantIds();
                    List<String> selected = event.getSelectedEntrantIds();

                    if (waitlist == null) waitlist = new ArrayList<>();
                    if (selected == null) selected = new ArrayList<>();

                    // Remove from waitlist and add to selected
                    for (String userId : selectedUserIds) {
                        waitlist.remove(userId);
                        if (!selected.contains(userId)) {
                            selected.add(userId);
                        }
                    }

                    event.setWaitlistEntrantIds(waitlist);
                    event.setSelectedEntrantIds(selected);

                    // Update the event
                    updateEvent(event, callback);
                    Log.d("EventRepository", "Moved " + selectedUserIds.size() + " entrants to selected");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error moving entrants to selected", e);
                    callback.onError("Failed to move entrants: " + e.getMessage());
                });
    }


    /**
     * Moves entrants from selected list to accepted list.
     * This happens when entrants accept their invitation.
     *
     * @param eventId the unique identifier of the event
     * @param userIds the list of user IDs that accepted
     * @param callback the callback to receive success confirmation or error message
     */
    public void moveEntrantsToAccepted(String eventId, List<String> userIds, OperationCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onError("No entrants to move");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Event not found");
                        return;
                    }

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) {
                        callback.onError("Failed to parse event data");
                        return;
                    }

                    event.setId(documentSnapshot.getId());

                    // Get or initialize lists
                    List<String> selected = event.getSelectedEntrantIds();
                    List<String> accepted = event.getAcceptedEntrantIds();

                    if (selected == null) selected = new ArrayList<>();
                    if (accepted == null) accepted = new ArrayList<>();

                    // Remove from selected and add to accepted
                    for (String userId : userIds) {
                        selected.remove(userId);
                        if (!accepted.contains(userId)) {
                            accepted.add(userId);
                        }
                    }

                    event.setSelectedEntrantIds(selected);
                    event.setAcceptedEntrantIds(accepted);

                    // Update the event
                    updateEvent(event, callback);
                    Log.d("EventRepository", "Moved " + userIds.size() + " entrants to accepted");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error moving entrants to accepted", e);
                    callback.onError("Failed to move entrants: " + e.getMessage());
                });
    }


    /**
     * Moves entrants to cancelled list.
     * Can remove from either waitlist or selected list.
     *
     * @param eventId the unique identifier of the event
     * @param userIds the list of user IDs to cancel
     * @param callback the callback to receive success confirmation or error message
     */
    public void moveEntrantsToCancelled(String eventId, List<String> userIds, OperationCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onError("No entrants to cancel");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Event not found");
                        return;
                    }

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) {
                        callback.onError("Failed to parse event data");
                        return;
                    }

                    event.setId(documentSnapshot.getId());

                    // Get or initialize lists
                    List<String> waitlist = event.getWaitlistEntrantIds();
                    List<String> selected = event.getSelectedEntrantIds();
                    List<String> cancelled = event.getCancelledEntrantIds();

                    if (waitlist == null) waitlist = new ArrayList<>();
                    if (selected == null) selected = new ArrayList<>();
                    if (cancelled == null) cancelled = new ArrayList<>();

                    // Remove from waitlist/selected and add to cancelled
                    for (String userId : userIds) {
                        waitlist.remove(userId);
                        selected.remove(userId);
                        if (!cancelled.contains(userId)) {
                            cancelled.add(userId);
                        }
                    }

                    event.setWaitlistEntrantIds(waitlist);
                    event.setSelectedEntrantIds(selected);
                    event.setCancelledEntrantIds(cancelled);

                    // Update the event
                    updateEvent(event, callback);
                    Log.d("EventRepository", "Moved " + userIds.size() + " entrants to cancelled");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error moving entrants to cancelled", e);
                    callback.onError("Failed to move entrants: " + e.getMessage());
                });
    }



    /**
     * Creates a notification document in Firestore for a user.
     *
     * @param userId the user who will receive the notification
     * @param eventId the event this notification is about
     * @param message the notification message
     * @param callback the callback to receive success confirmation or error message
     */
    public void createNotification(String userId, String eventId, String message, OperationCallback callback) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("eventId", eventId);
        notification.put("message", message);
        notification.put("timestamp", new Date());
        notification.put("read", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d("EventRepository", "Notification created for user: " + userId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error creating notification", e);
                    callback.onError("Failed to create notification: " + e.getMessage());
                });
    }



}

