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
 * Controller class for managing Event related logic
 * This is the middle man between the View (UI) and the Model (Repository)
 * Handles business logic such as filtering events, validating data, and processing user actions
 * this is the Controller layer
 *
 * Outstanding Issues: None
 *
 */
public class EventController {
    private final EventRepository repository;

    // Constructors
    public EventController(){
        this.repository = new EventRepository();
    }

    public EventController(EventRepository repository){
        this.repository = repository;
    }

    // gets all events from repository
    public void loadAllEvents(FirestoreListCallback<Event> callback){
        repository.getAllEvents(callback);
    }

    // gets the specific event from repository
    public void loadEventById(String eventId, FirestoreCallback<Event> callback) {
        repository.getEventById(eventId, callback);
    }

    // gets the events created by a specific organizer from repository
    public void loadEventsByOrganizer(String organizerId, FirestoreListCallback<Event> callback) {
        repository.getEventsByOrganizer(organizerId, callback);
    }

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

    public void loadEventsByStatus(String status, FirestoreListCallback<Event> callback) {
        repository.getEventsByStatus(status, callback);
    }

    // search based on if the query matches the name and/or description of the event
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

    public void leaveWaitingList(String eventId, String entrantId, OperationCallback callback) {
        repository.removeEntrantFromWaitlist(eventId, entrantId, callback);
    }

    // Need to check if all the event data is present and valid
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

    // pass in the event to get the event ID to update
    public void updateEvent(Event event, OperationCallback callback) {
        if (event.getId() == null || event.getId().isEmpty()) {
            callback.onError("Cannot update event: ID is required");
            return;
        }

        repository.updateEvent(event, callback);
    }

    public void deleteEvent(String eventId, OperationCallback callback) {
        repository.deleteEvent(eventId, callback);
    }
}
