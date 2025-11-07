package com.hotdog.elotto.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.OrganizerController;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class Organizer {

    /**
     * A nested class that defines a FirestoreCallback to handle the returned value of the user calls
     * using AtomicReference and CountDownLatch to maintain thread safety in the case of atomic operations.
     */
    // Creates a callback that sets a reference to the returned user and edits the info of this outer class user object
    private class AtomicOrgCallback implements FirestoreCallback<Organizer> {

        private final AtomicReference<Organizer> orgRef = new AtomicReference<>();
        private final CountDownLatch gate = new CountDownLatch(1);

        /**
         * Creates a new AtomicOrgCallback instance with the calling organizer object set.
         * @param SuperOrg The Organizer object that the callbacks are meant to check.
         */
        public AtomicOrgCallback(Organizer SuperOrg) {
            orgRef.set(SuperOrg);}

        public void onSuccess(Organizer org) {
            // Set the info to the returned user value
            this.orgRef.get().setOrg(org);
            this.gate.countDown();
        }

        public void onError(String errorMessage) {
            Log.d("ORGANIZER REPO", errorMessage);
            this.orgRef.get().updateOrganizer();
            this.gate.countDown();
        }

        /**
         * Used to block this thread until the callbacks have finished (so either a success or fail)
         */
        public void await() {
            try {
                this.gate.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Exclude
    private User user;
    @Exclude
    private static Organizer SuperOrg;
    @Exclude
    private OrganizerController controller;
    @DocumentId
    private final String deviceId;

    private List<String> myEvents = new ArrayList<>();


    @SuppressLint("HardwareIds")
    public Organizer(Context context) {
        // Get deviceId
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(SuperOrg!=null) {
            this.setOrg(SuperOrg);
            this.controller = SuperOrg.controller;
            return;
        }
        SuperOrg=this;
        this.user=new User(context, true);
        this.controller=new OrganizerController(this);

        AtomicOrgCallback atomicCallback = new AtomicOrgCallback(this);
        ExecutorService bgExec = Executors.newSingleThreadExecutor();
        bgExec.execute(() -> OrganizerRepository.getInstance().getOrganizerById(this.deviceId, atomicCallback, bgExec));
        atomicCallback.await();
    }
    public Organizer() {
        this.deviceId = null;
    }

    public void setOrg(Organizer org) {
        this.user=org.user;
        this.myEvents=org.myEvents;
    }

    /**
     * Add an event to the my events list.
     * @param eventId Event to be added.
     * @return True if the event was added, false if the event already exists in the list.
     */
    public boolean addEvent(String eventId) {
        if(Collections.binarySearch(this.myEvents, eventId)>=0) return false;
        this.myEvents.add(eventId);
        this.myEvents.sort(Comparator.naturalOrder());
        this.updateOrganizer();
        return true;
    }

    /**
     * Remove an event from the events this user has created.
     * @implNote This will also delete the event from the firestore database.
     * @param eventId Event ID to remove.
     * @return True if the event was found, false if the event wasn't found or the deletion failed.
     */
    public boolean removeEvent(String eventId) {
        int index = Collections.binarySearch(this.myEvents, eventId);
        if(index <0) return false;
        final boolean[] success = new boolean[1];
        EventRepository repo = new EventRepository();
        ExecutorService bgExec = Executors.newSingleThreadExecutor();
        CountDownLatch gate = new CountDownLatch(1);
        repo.deleteEvent(eventId, new OperationCallback() {
            @Override
            public void onSuccess() {
                myEvents.remove(index);
                success[0] =true;
                gate.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("DELETE EVENT", errorMessage);
                success[0]=false;
                gate.countDown();
            }
        }, bgExec);
        try {
            gate.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return success[0];
    }

    /**
     * THIS IS MEANT FOR FIRESTORE TO HAVE AN ACCESSOR
     * NOT FOR PEOPLE TO USE
     * @param events Events to set.
     */
    public void setMyEvents(List<String> events) {
        this.myEvents=events;
    }

    /**
     * Gets the entire list of events from this organizer.
     * @return List of events.
     */
    public List<String> getMyEvents() {
        return this.myEvents;
    }

    /**
     * Get the device id used as the document id for this organizer.
     * @return Device ID.
     */
    public String getId() {
        return this.deviceId;
    }

    /**
     * Updates firebase to have all this organizers information.
     * @implNote You should never need to call this yourself, since you should never have to directly modify information of the organizer without using the methods; which do it automatically.
     */
    public void updateOrganizer() {
        this.controller.updateOrganizer();
    }
}
