package com.hotdog.elotto.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;

import java.util.ArrayList;
import java.util.List;

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
}

