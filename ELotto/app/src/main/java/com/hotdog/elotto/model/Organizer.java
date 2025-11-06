package com.hotdog.elotto.model;

import java.util.ArrayList;

/**
 * Represents an Organizer, containing their ID which should be the same as user ID and a list of
 * created events in the form of their id's
 */
public class Organizer {
    private String orgID;
    private ArrayList<String> createdEvents = new ArrayList<>();

    /**
     * Constructor that initializes the organizer with an empty list of created events.
     * Used to allow FireStore to do doc.toObject(Organizer.class)
     */
    public Organizer() {
        this.createdEvents = new ArrayList<>();
    }

    /**
     * Constructs an Organizer with a specified organizer ID.
     *
     * @param orgID ID of the organizer
     */
    public Organizer(String orgID) {
        this.orgID = orgID;
    }

    /**
     * Returns the ID of the organizer
     *
     * @return the organizer's ID
     */
    public String getOrgID() {
        return orgID;
    }

    /**
     * Sets the ID for this organizer.
     *
     * @param orgID the organizer ID to set
     *
     * Shouldn't really have to use it*
     */
    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    /**
     * Returns a list of event IDs that this organizer has created.
     *
     * @return the list of created event IDs
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