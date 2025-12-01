package com.hotdog.elotto.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.OrganizerController;
import com.hotdog.elotto.helpers.UserStatus;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.security.auth.callback.Callback;

public class Organizer {

    /**
     * A nested class that defines a FirestoreCallback to handle the returned value of the user calls
     * using AtomicReference and CountDownLatch to maintain thread safety in the case of atomic operations.
     */
    // Creates a callback that sets a reference to the returned user and edits the info of this outer class user object
    private class AtomicOrgCallback implements FirestoreCallback<Organizer> {

        private final AtomicReference<Organizer> orgRef = new AtomicReference<>();
        private Consumer<Organizer> consumer=null;
        private Runnable runnable=null;

        /**
         * Creates a new AtomicOrgCallback instance with the calling organizer object set, and a task that takes in the returned organizer for callback.
         * @param superOrg The User object that the callbacks are meant to check.
         * @param task The action to be completed on BOTH a successful and unsuccessful User read. This <b>can</b> block it's thread since it will be running in a separate thread.
         */
        public AtomicOrgCallback(Organizer superOrg, Consumer<Organizer> task) {orgRef.set(superOrg); this.consumer=task;}

        /**
         * Creates a new AtomicOrgCallback instance with the calling organizer object set, and a task with no params for callback.
         * @param superOrg
         * @param task
         */
        public AtomicOrgCallback(Organizer superOrg, Runnable task) {orgRef.set(superOrg); this.runnable=task;}

        /**
         * Creates a new AtomicOrgCallback instance with the calling organizer object set.
         * @param superOrg The Organizer object that the callbacks are meant to check for existing Organizer.
         */
        public AtomicOrgCallback(Organizer superOrg) {orgRef.set(superOrg);}

        public void onSuccess(Organizer org) {
            // Set the info to the returned user value
            this.orgRef.get().setOrg(org);
            this.orgRef.get().status= UserStatus.Existent;
            if(this.consumer != null) this.consumer.accept(org);
            if(this.runnable != null) this.runnable.run();
        }


        public void onError(String errorMessage) {
            Log.d("USER_REPO", errorMessage);
            // Signals whether the firestore db has an instance of this user.
            this.orgRef.get().status=errorMessage.toLowerCase().contains("not found") ? UserStatus.Nonexistent : UserStatus.Error;
            // Upon getting nothing back or an error, we just assume that the current user instance is new.
            if(this.consumer != null) this.consumer.accept(orgRef.get());
            if(this.runnable != null) this.runnable.run();
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
    @Exclude
    private UserStatus status;

    private List<String> myEvents = new ArrayList<>();


    @SuppressLint("HardwareIds")
    public Organizer(Context context) {
        // Get deviceId
        if(SuperOrg!=null) {
            this.deviceId= SuperOrg.deviceId;
            this.setOrg(SuperOrg);
            this.controller = SuperOrg.controller;
            return;
        }
        SuperOrg=this;
        this.user=new User(context);
        this.controller=new OrganizerController(this);
        this.deviceId=this.user.getDeviceId();

        AtomicOrgCallback atomicCallback = new AtomicOrgCallback(this);
        OrganizerRepository.getInstance().getOrganizerById(this.deviceId, atomicCallback);
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
     * Gets the list of event objects for this organizer
     * @return  List of events for this organizer, or empty list if they can't be found or there is an error
     */
    public List<Event> getEventList(boolean atomic) {
        ExecutorService bgExec = Executors.newSingleThreadExecutor();
        EventRepository repo = new EventRepository();
        final CountDownLatch gate = new CountDownLatch(1);
        final AtomicReference<List<Event>> EventsRef = new AtomicReference<>();
        repo.getEventsByOrganizer(this.getId(), new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> results) {
                EventsRef.set(results);
                gate.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("EVENT BY ORG", errorMessage);
                EventsRef.set(new ArrayList<>());
                gate.countDown();
            }
        });

        if(atomic) {
            try {
                gate.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Log.d("ORGANIZER EVENTS", ""+EventsRef.get());

        return EventsRef.get();
    }

    /**
     * Gets the list of event objects for this organizer
     * @param callback Extra actions to perform on result (whether success or fail)
     */
    public void getEventList(FirestoreCallback<List<Event>> callback) {
        EventRepository repo = new EventRepository();
        repo.getEventsByOrganizer(this.getId(), new FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> results) {
                callback.onSuccess(results);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d("EVENT BY ORG", errorMessage);
                callback.onError(errorMessage);
            }
        });
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

    /**
     * Checks whether the Organizer exists already in the repo or not, or if there was an error in retrieving it.
     * @return UserStatus based on the return of the organizer repo fetch.
     */
    public UserStatus exists() {
        return this.status;
    }

    /**
     * Merges two organizers, losing and nullifying the oldOrg.
     * @param oldOrg The organizer instance to be merged into this one.
     */
    public void Merge(Organizer oldOrg) {
        this.myEvents.addAll(0, oldOrg.myEvents);
        this.status=oldOrg.status;
        oldOrg.user = null;
        oldOrg.myEvents=null;
        oldOrg.status=null;
        oldOrg.controller=null;
        this.updateOrganizer();
    }
}
