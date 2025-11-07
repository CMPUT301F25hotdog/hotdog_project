package com.hotdog.elotto.controller;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.repository.OrganizerRepository;

/**
 * A controller class to update the UserRepository model for a single user.
 * @author Layne Pitman
 * @version 1.0.0
 */
public class OrganizerController {
    /**
     * Reference to the user of this controller that will be used in the model updating.
     */
    Organizer org;

    /**
     * Instantiate the controller with a single user.
     * @param org The organizer to be referenced by this controller.
     */
    public OrganizerController(Organizer org) {
        this.org=org;
    }

    /**
     * Updates the information of the current user in firebase.
     */
    public void updateOrganizer() {
        OrganizerRepository.getInstance().updateOrganizer(this.org,
                new OperationCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError(String errorMessage) {}
                });
    }


}