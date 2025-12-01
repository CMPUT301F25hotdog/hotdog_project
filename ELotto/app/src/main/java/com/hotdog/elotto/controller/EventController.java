package com.hotdog.elotto.controller;

import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for managing event-related operations and business logic.
 *
 * <p>This controller acts as an intermediary between the UI layer and the EventRepository,
 * providing methods for loading, creating, updating, and deleting events. It also handles
 * business logic such as filtering events, validating event data, and managing waitlist
 * operations with appropriate validation checks.</p>
 *
 * <p>Controller layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Ethan Carter
 * @version 1.0
 * @since 2025-11-01
 */
public class EventController {
    /**
     * The repository used for event data access operations.
     */
    private final EventRepository repository;

    /**
     * Default constructor that initializes the controller with a new EventRepository.
     */
    public EventController(){
        this.repository = new EventRepository();
    }

    /**
     * Constructor for dependency injection, useful for testing.
     *
     * @param repository the EventRepository instance to use for data operations
     */
    public EventController(EventRepository repository){
        this.repository = repository;
    }

    /**
     * Loads all events from the repository.
     *
     * @param callback the callback to receive the list of events or error message
     */
    public void loadAllEvents(FirestoreListCallback<Event> callback){
        repository.getAllEvents(callback);
    }

    /**
     * Loads a specific event by its ID.
     *
     * @param eventId the unique identifier of the event to load
     * @param callback the callback to receive the event or error message
     */
    public void loadEventById(String eventId, FirestoreCallback<Event> callback) {
        repository.getEventById(eventId, callback);
    }

    /**
     * Loads all events created by a specific organizer.
     *
     * @param organizerId the unique identifier of the organizer
     * @param callback the callback to receive the list of events or error message
     */
    public void loadEventsByOrganizer(String organizerId, FirestoreListCallback<Event> callback) {
        repository.getEventsByOrganizer(organizerId, callback);
    }

    /**
     * Loads only events that are currently open for registration.
     *
     * <p>Filters all events and returns only those where registration is currently open.</p>
     *
     * @param callback the callback to receive the list of open events or error message
     */
    public void loadOpenEvents(FirestoreListCallback<Event> callback) {
        repository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                List<Event> openEvents = new ArrayList<>();

                for (Event event : events) {
                    if (event.isRegistrationOpen()) {
                        openEvents.add(event);
                    }
                }

                callback.onSuccess(openEvents);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Loads events filtered by a specific status.
     *
     * @param status the status to filter by
     * @param callback the callback to receive the filtered list of events or error message
     */
    public void loadEventsByStatus(String status, FirestoreListCallback<Event> callback) {
        repository.getEventsByStatus(status, callback);
    }

    /**
     * Searches for events where the query matches the event name or description.
     *
     * <p>Performs a case-insensitive search across event names and descriptions.</p>
     *
     * @param query the search query string
     * @param callback the callback to receive the list of matching events or error message
     */
    public void searchEvents(String query, FirestoreListCallback<Event> callback) {
        repository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                List<Event> searchedEvents = new ArrayList<>();
                String lowerCaseQuery = query.toLowerCase();

                for (Event event : events) {
                    String name = "";
                    if (event.getName() != null) {
                        name = event.getName().toLowerCase();
                    }

                    String description = "";
                    if (event.getDescription() != null) {
                        description = event.getDescription().toLowerCase();
                    }

                    if (name.contains(lowerCaseQuery) || description.contains(lowerCaseQuery)) {
                        searchedEvents.add(event);
                    }
                }

                callback.onSuccess(searchedEvents);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Filters events by location.
     *
     * <p>Performs a case-insensitive search to find events whose location contains
     * the specified location string.</p>
     *
     * @param location the location string to filter by
     * @param callback the callback to receive the filtered list of events or error message
     */
    public void filterEventsByLocation(String location, FirestoreListCallback<Event> callback) {
        repository.getAllEvents(new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                List<Event> filteredEvents = new ArrayList<>();
                String lowerLocation = location.toLowerCase();

                for (Event event : events) {
                    String eventLocation = "";
                    if (event.getLocation() != null) {
                        eventLocation = event.getLocation().toLowerCase();
                    }

                    if (eventLocation.contains(lowerLocation)) {
                        filteredEvents.add(event);
                    }
                }

                callback.onSuccess(filteredEvents);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Adds an entrant to an event's waiting list with validation checks.
     *
     * <p>This method validates that registration is open and the waiting list is not
     * full before adding the entrant. Returns appropriate error messages if validation
     * fails.</p>
     *
     * @param eventId the ID of the event to join
     * @param entrantId the ID of the entrant joining the waiting list
     * @param callback the callback to receive success confirmation or error message
     */
    public void joinWaitingList(String eventId, String entrantId, OperationCallback callback) {
        repository.getEventById(eventId, new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (!event.isRegistrationOpen()) {
                    callback.onError("Registration is not currently open for this event");
                    return;
                }

                if (event.isFull()) {
                    callback.onError("The waiting list for this event is full");
                    return;
                }

                repository.addEntrantToWaitlist(eventId, entrantId, callback);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Removes an entrant from an event's waiting list.
     *
     * @param eventId the ID of the event to leave
     * @param entrantId the ID of the entrant leaving the waiting list
     * @param callback the callback to receive success confirmation or error message
     */
    public void leaveWaitingList(String eventId, String entrantId, OperationCallback callback) {
        repository.removeEntrantFromWaitlist(eventId, entrantId, callback);
    }

    /**
     * Creates a new event after validating all required fields.
     *
     * <p>Validates that all required event data is present and valid before creating
     * the event. Returns specific error messages for validation failures:</p>
     * <ul>
     *     <li>Event name must not be null or empty</li>
     *     <li>Event location must not be null or empty</li>
     *     <li>Event date and time must be set</li>
     *     <li>Registration period dates must be set</li>
     *     <li>Maximum entrants must be greater than 0</li>
     * </ul>
     *
     * @param event the event object to create
     * @param callback the callback to receive success confirmation or error message
     */
    public void createEvent(Event event, OperationCallback callback) {
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            callback.onError("Event name is required");
            return;
        }

        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            callback.onError("Event location is required");
            return;
        }

        if (event.getEventDateTime() == null) {
            callback.onError("Event date and time is required");
            return;
        }

        if (event.getRegistrationStartDate() == null || event.getRegistrationEndDate() == null) {
            callback.onError("Registration period is required");
            return;
        }

        if (event.getMaxEntrants() <= 0) {
            callback.onError("Maximum entrants must be greater than 0");
            return;
        }

        repository.createEvent(event, callback);
    }

    /**
     * Updates an existing event in the repository.
     *
     * <p>Validates that the event has a valid ID before attempting to update.</p>
     *
     * @param event the event object with updated data (must have valid ID)
     * @param callback the callback to receive success confirmation or error message
     */
    public void updateEvent(Event event, OperationCallback callback) {
        if (event.getId() == null || event.getId().isEmpty()) {
            callback.onError("Cannot update event: ID is required");
            return;
        }

        repository.updateEvent(event, callback);
    }

    /**
     * Deletes an event from the repository.
     *
     * @param eventId the unique identifier of the event to delete
     * @param callback the callback to receive success confirmation or error message
     */
    public void deleteEvent(String eventId, OperationCallback callback) {
        repository.deleteEvent(eventId, callback);
    }
}