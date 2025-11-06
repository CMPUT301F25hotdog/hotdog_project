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
 * This class acts as an intermediary between the UI layer and the {@link OrganizerRepository},
 * handling creation, retrieval, and updating of organizer data.
 */
public class OrganizerController {
    private final OrganizerRepository repository;

    /**
     * Constructs an {@code OrganizerController} and initializes the associated repository.
     */
    public OrganizerController() {
        this.repository = new OrganizerRepository();
    }

    /**
     * Creates a new organizer entry in the Firestore database.
     *
     * @param organizer the {@link Organizer} object containing organizer details
     * @param callback  the {@link OperationCallback} to handle success or failure
     */
    public void createOrganizer(Organizer organizer, OperationCallback callback) {
        repository.createOrganizer(organizer, callback);
    }

    /**
     * Retrieves an organizer from the Firestore database using its ID.
     *
     * @param orgID    the unique ID of the organizer
     * @param callback the {@link FirestoreCallback} that returns the {@link Organizer} data
     */
    public void getOrganizer(String orgID, FirestoreCallback<Organizer> callback) {
        repository.getOrganizerById(orgID, callback);
    }

    /**
     * Updates an organizer’s event list by adding or modifying an event reference.
     *
     * @param orgID     the unique ID of the organizer to update
     * @param eventID   the ID of the event to add or update
     * @param callback  the {@link OperationCallback} to handle the operation result
     */
    public void updateOrganizerEvents(String orgID, String eventID, OperationCallback callback) {
        repository.updateOrganizer(orgID, eventID, callback);
    }
}