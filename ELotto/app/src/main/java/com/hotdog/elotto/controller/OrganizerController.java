package com.hotdog.elotto.controller;

import android.content.Context;
import android.widget.Toast;

import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Controller class responsible for managing organizer-related operations.
 * This class handles interactions between the OrganizerRepository and some outside call
 * handling creation, retrieval, and updating of organizer data.
 */
public class OrganizerController {
    private final OrganizerRepository repository;
    /**
     * Constructs an organizer controller object
     */
    public OrganizerController() {
        this.repository = new OrganizerRepository();
    }
    /**
     * Constructs an organizer controller object to use only in the testing
     */
    public OrganizerController(OrganizerRepository repository){this.repository = repository;}
    /**
     * Creates a new organizer entry in the Firestore database.
     *
     * @param organizer organizer object
     * @param callback  the callback to handle success or failure
     */
    public void createOrganizer(Organizer organizer, OperationCallback callback) {
        repository.createOrganizer(organizer, callback);
    }

    /**
     * Retrieves an organizer from the Firestore database using its ID.
     *
     * @param orgID    the unique ID of the organizer
     * @param callback the FirestoreCallback that returns the Organizer data
     */
    public void getOrganizer(String orgID, FirestoreCallback<Organizer> callback) {
        repository.getOrganizerById(orgID, callback);
    }

    /**
     * Updates an organizer’s event list by adding or modifying an event reference.
     *
     * @param orgID     the unique ID of the organizer to update
     * @param eventID   the unique ID of the event to add or update
     * @param callback  the callback to handle the operation result
     */
    public void updateOrganizerEvents(String orgID, String eventID, OperationCallback callback) {
        repository.updateOrganizer(orgID, eventID, callback);
    }
    public void deleteOrganizer(String orgID, OperationCallback callback) {
        repository.deleteOrganizer(orgID, callback);
    }
}