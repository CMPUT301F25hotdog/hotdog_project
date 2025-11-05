package com.hotdog.elotto.model;

import java.util.ArrayList;

public class Organizer {
    private String orgID; // Usually same as user ID
    private ArrayList<String> createdEvents = new ArrayList<>();
    public Organizer() {
        this.createdEvents = new ArrayList<>();
    }
    public Organizer(String orgID) {
        this.orgID = orgID;
    }

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    public ArrayList<String> getCreatedEvents() {
        return createdEvents;
    }

    public void addCreatedEvent(String eventID) {
        createdEvents.add(eventID);
    }

    public void removeCreatedEvent(String eventID) {
        createdEvents.remove(eventID);
    }
}