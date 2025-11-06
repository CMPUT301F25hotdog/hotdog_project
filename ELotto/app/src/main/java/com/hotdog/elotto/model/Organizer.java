package com.hotdog.elotto.model;

import java.util.ArrayList;

/**
 * Represents an organizer within the eLotto application.
 * <p>
 * An {@code Organizer} is typically a user who can create and manage events.
 * Each organizer has a unique ID (usually matching their user ID) and a list
 * of event IDs that they have created.
 * </p>
 */
public class Organizer {
    /** The unique ID of the organizer, usually the same as the associated user ID. */
    private String orgID;

    /** A list of event IDs that this organizer has created. */
    private ArrayList<String> createdEvents = new ArrayList<>();

    /**
     * Default constructor that initializes an empty list of created events.
     */
    public Organizer() {
        this.createdEvents = new ArrayList<>();
    }

    /**
     * Constructs an {@code Organizer} with a specified organizer ID.
     *
     * @param orgID the unique ID of the organizer
     */
    public Organizer(String orgID) {
        this.orgID = orgID;
    }

    /**
     * Returns the unique ID of the organizer.
     *
     * @return the organizer's ID
     */
    public String getOrgID() {
        return orgID;
    }

    /**
     * Sets the unique ID for this organizer.
     *
     * @param orgID the organizer ID to set
     */
    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    /**
     * Returns a list of event IDs that this organizer has created.
     *
     * @return a list of created event IDs
     */
    public ArrayList<String> getCreatedEvents() {
        return createdEvents;
    }

    /**
     * Adds a new event ID to the list of created events.
     *
     * @param eventID the ID of the event to add
     */
    public void addCreatedEvent(String eventID) {
        createdEvents.add(eventID);
    }

    /**
     * Removes an event ID from the list of created events.
     *
     * @param eventID the ID of the event to remove
     */
    public void removeCreatedEvent(String eventID) {
        createdEvents.remove(eventID);
    }
}