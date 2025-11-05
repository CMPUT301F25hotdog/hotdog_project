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

public class OrganizerController {
    private final OrganizerRepository repository;

    // Constructor using orgID (usually the user ID)
    public OrganizerController(){
        this.repository = new OrganizerRepository();
    }
    public void createOrganizer(Organizer organizer, OperationCallback callback) {
        repository.createOrganizer(organizer, callback);
    }
    public void getOrganizer(String orgID, FirestoreCallback<Organizer> callback){
        repository.getOrganizerById(orgID,callback);
    }
    public void updateOrganizerEvents(String orgID, String eventID, OperationCallback callback){
        repository.updateOrganizer(orgID, eventID, callback);
    }

}