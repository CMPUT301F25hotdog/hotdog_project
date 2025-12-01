package com.hotdog.elotto.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.UserController;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.helpers.UserStatus;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
* A class containing all common information for a User.
* All information required for Entrant is non-unique to Entrant, meaning we share it with Organizer and Administrator.
 * @author Layne Pitman
 * @version 1.0.0
*/
public class User {

    /**
     * The controller for this user. Excluded from firebase as it needs to be reinstantiated each time this user is made.
     */
    @Exclude
    private UserController controller;

    /**
     * AtomicUserCallback instance that will be the point of contact between the main thread and the synch thread.
     */
    @Exclude
    private AtomicUserCallback atomicCallback;

    /**
     * A class that represents one registered event for a user. Each object holds a different event and information for one registration of one user.
     */
    public static class RegisteredEvent implements Comparable<RegisteredEvent> {
        private Timestamp registeredDate;
        private Timestamp selectedDate;
        private Status status;
        private String eventId;

        public RegisteredEvent(){
            this.registeredDate = Timestamp.now();
            this.status = Status.Pending;
            this.eventId = "";
        }

        public RegisteredEvent(String eventId) {
            this.registeredDate = Timestamp.now();
            this.status = Status.Pending;
            this.eventId = eventId;
        }

        @Override
        public int compareTo(RegisteredEvent o) {
            return eventId.compareTo(o.eventId);
        }

        public void setRegisteredDate(Timestamp registeredDate) {
            this.registeredDate = registeredDate;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }

        public String getEventId() {
            return eventId;
        }

        public Timestamp getRegisteredDate() {
            return registeredDate;
        }

        public Timestamp getSelectedDate(){
            return selectedDate;
        }
        public void setSelectedDate(Timestamp selectedDate){
            this.selectedDate = selectedDate;
        }
    }

    /**
     * A class to store a firebase compatible shared string that is mutable and can be updated by multiple references.
     */
    public class SharedString {
        String value;

        /**
         * Creates the initial shared string.
         * @param value String to be shared.
         */
        public SharedString(String value) {
            this.value = value;
        }

        /**
         * Sets the shared string to a new value.
         * @param value The string to be set.
         */
        public void set(String value) {
            this.value=value;
        }

        /**
         * Gets the current value of the shared string.
         * @return Current state of shared string.
         */
        public String get() {
            return this.value;
        }
    }

    /**
     * A nested class that defines a FirestoreCallback to handle the returned value of the user calls
     * using AtomicReference and CountDownLatch to maintain thread safety in the case of atomic operations.
     */
    // Creates a callback that sets a reference to the returned user and edits the info of this outer class user object
    private class AtomicUserCallback implements FirestoreCallback<User> {

        private final AtomicReference<User> userRef = new AtomicReference<>();
        private final AtomicReference<Consumer<User>> consumer=new AtomicReference<>(null);
        private final  AtomicReference<Runnable> runnable=new AtomicReference<>(null);

        /**
         * Creates a new AtomicUserCallback instance with the calling user object set, and a task that takes in the returned user for callback.
         * @param superUser The User object that the callbacks are meant to check.
         * @param task The action to be completed on BOTH a successful and unsuccessful User read. This <b>can</b> block it's thread since it will be running in a separate thread.
         */
        public AtomicUserCallback(User superUser, Consumer<User> task) {
            userRef.set(superUser);
            this.consumer.set(task);
        }

        /**
         * Creates a new AtomicUserCallback instance with the calling user object set, and a task with no params for callback.
         * @param superUser The User object that the callbacks are meant to check.
         * @param task The action to be completed on BOTH a successful and unsuccessful User read. This <b>can</b> block it's thread since it will be running in a separate thread.
         */
        public AtomicUserCallback(User superUser, Runnable task) {
            userRef.set(superUser);
            this.runnable.set(task);
        }

        /**
         * Creates a new AtomicUserCallback instance with the calling user object set.
         * @param superUser The User object that the callbacks are meant to check for existing User.
         */
        public AtomicUserCallback(User superUser) {
            userRef.set(superUser);
        }

        public void onSuccess(User user) {
            // Set the info to the returned user value
            this.userRef.get().setUser(user);
            this.userRef.get().status=UserStatus.Existent;
            if(this.consumer.get() != null) this.consumer.get().accept(this.userRef.get());
            if(this.runnable.get() != null) this.runnable.get().run();
        }


        public void onError(String errorMessage) {
            Log.d("USER_REPO", errorMessage);
            // Signals whether the firestore db has an instance of this user.
            this.userRef.get().status=errorMessage.toLowerCase().contains("not found") ? UserStatus.Nonexistent : UserStatus.Error;
            // Upon getting nothing back or an error, we just assume that the current user instance is new.
            if(this.consumer.get() != null) this.consumer.get().accept(this.userRef.get());
            if(this.runnable.get() != null) this.runnable.get().run();
        }
    }

    // Super User
    @Exclude
    private static User SuperUser;

    // Personal Info
    private SharedString name=new SharedString("");
    private SharedString email=new SharedString("");
    private SharedString phone=new SharedString("");

    // Identifying information
    @DocumentId
    private final String deviceId;

    // Events
    private List<RegisteredEvent> regEvents=new ArrayList<>(); // Events registered in

    // Repo control
    @Exclude
    private UserStatus status;

    /**
     * Nested class container for UserType to allow for multiple references to be value changed by one.
     */
    private class TypeContainer {
        private UserType type;

        /**
         * Creates a new type container with the type specified.
         * @param type Type to contain.
         */
        public TypeContainer (UserType type) {
            this.type=type;
        }

        /**
         * Get the type contained.
         * @return The type contained.
         */
        public UserType get() {
            return this.type;
        }

        /**
         * Set the type being contained.
         * @param type Type to be contained.
         */
        public void set(UserType type) {
            this.type=type;
        }
    }

    // Permission control
    private TypeContainer type=new TypeContainer(null);

    /**
     * No arg constructor to be used by firestore to create a blank version of the User.
     * NEVER USE THIS AS A PERSON
     */
    public User() {deviceId="";}

    /**
    * Class Constructor that will <b>ALWAYS</b> get the user of this phone.
     * If atomic is true, then the main thread will not run until either a result or error has returned.
     * <p><b>WARNING:</b> This <b>cannot</b> be used to get any other user than the current phone user, and updating the info of a user object created with this constructor will update the current phone user no matter what!</p>
    *
    * @param context The context of the caller to resolve device ID.
     * @param atomic A parameterized consumer task to be performed on <b>any</b> return of the user. This <b>can</b> block it's thread since it will be running in a thread separate from the main thread.
    */
    @SuppressLint("HardwareIds")
    public User(Context context, Consumer<User> atomic) {
        // Get deviceId
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(SuperUser!=null) {
            this.setUser(SuperUser);
            this.controller=SuperUser.controller;
            Log.d("SUPER USER", "SU Name: " + SuperUser.getName() + "SU Email: " + SuperUser.getEmail() + "SU Phone: " + SuperUser.getPhone());
            atomic.accept(SuperUser);
            return;
        }

        SuperUser=this;

        // Create new controller
        this.controller = new UserController(this);

        final AtomicUserCallback atomicCallback = new AtomicUserCallback(this, atomic);
        UserRepository.getInstance().getUserById(this.deviceId, atomicCallback);
    }

    /**
     * Class Constructor that will <b>ALWAYS</b> get the user of this phone.
     * If atomic is true, then the main thread will not run until either a result or error has returned.
     * <p><b>WARNING:</b> This <b>cannot</b> be used to get any other user than the current phone user, and updating the info of a user object created with this constructor will update the current phone user no matter what!</p>
     *
     * @param context The context of the caller to resolve device ID.
     * @param atomic A runnable task to be performed on <b>any</b> return of the user. This <b>can</b> block it's thread since it will be running in a thread separate from the main thread.
     */
    @SuppressLint("HardwareIds")
    public User(Context context, Runnable atomic) {
        // Get deviceId
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(SuperUser!=null) {
            this.setUser(SuperUser);
            this.controller=SuperUser.controller;
            Log.d("SUPER USER", "SU Name: " + SuperUser.getName() + "SU Email: " + SuperUser.getEmail() + "SU Phone: " + SuperUser.getPhone());
            atomic.run();
            return;
        }

        SuperUser=this;

        // Create new controller
        this.controller = new UserController(this);

        final AtomicUserCallback atomicCallback = new AtomicUserCallback(this, atomic);
        UserRepository.getInstance().getUserById(this.deviceId, atomicCallback);
    }

    /**
     * Class Constructor that will <b>ALWAYS</b> get the user of this phone.
     * <p><b>WARNING:</b> This <b>cannot</b> be used to get any other user than the current phone user, and updating the info of a user object created with this constructor will update the current user no matter what!</p>
     *
     * @param context The context of the caller to resolve device ID.
     */
    @SuppressLint("HardwareIds")
    public User(Context context) {
        // Get deviceId
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(SuperUser!=null) {
            this.setUser(SuperUser);
            this.controller=SuperUser.controller;
            Log.d("SUPER USER", "SU Name: " + SuperUser.getName() + "SU Email: " + SuperUser.getEmail() + "SU Phone: " + SuperUser.getPhone());
            return;
        }

        SuperUser=this;

        // Create new controller
        this.controller = new UserController(this);

        final AtomicUserCallback atomicCallback = new AtomicUserCallback(this);
        // No need to be on another thread since no actions are needed to be performed in the callback
        UserRepository.getInstance().getUserById(this.deviceId, atomicCallback);
    }

    /**
     * Set this user object info to the given user object info. Effectively makes a copy of the given user that references all the same things.
     * <p><b>NOTE:</b> This is intended for internal use, so only use it if you are confident it is what you need.</p>
     * @param user The user object to grab info from.
     */
    private void setUser(User user) {
        this.status=user.status;
        this.name=user.name;
        this.email=user.email;
        this.phone=user.phone;
        this.regEvents=user.regEvents;
        this.type=user.type;
    }

    /**
     * Updates the name for this User, overwriting the current name if there is one.
     * @param name Name to be set.
     */
    public void updateName(String name) {
        this.name.set(name);
        this.updateUser();
    }

    /**
     * Returns the name of this User.
     * @return Name of the User.
     */
    public String getName() {
        return name.get();
    }

    /**
     * Sets this user objects name.
     * <p><b>WARNING:</b> THIS DOES NOT UPDATE FIREBASE AND IS NOT MEANT FOR DEV USE.</p>
     * @param name Name to be set.
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Updates the email for this User, overwriting the current email if there is one.
     * @param email Email to be set.
     */
    public void updateEmail(String email) {
        this.email.set(email);
        this.updateUser();
    }

    /**
     * Returns the email of this User.
     * @return Email of the User.
     */
    public String getEmail() {
        return email.get();
    }

    /**
     * Sets this user objects email.
     * <p><b>WARNING:</b> THIS DOES NOT UPDATE FIREBASE AND IS NOT MEANT FOR DEV USE.</p>
     * @param email Email to be set.
     */
    public void setEmail(String email) {
        this.email.set(email);
    }

    /**
     * Updates the phone number for this User, overwriting the current phone number if there is one.
     * @param phone Phone number to be set.
     */
    public void updatePhone(String phone) {
        this.phone.set(phone);
        this.updateUser();
    }

    /**
     * Return the phone number of this User.
     * @return Phone number of the User.
     */
    public String getPhone() {
        return phone.get();
    }

    /**
     * Sets this user objects phone.
     * <p><b>WARNING:</b> THIS DOES NOT UPDATE FIREBASE AND IS NOT MEANT FOR DEV USE.</p>
     * @param phone Phone to be set.
     */
    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    /**
     * Updates the current User type for this user. WARNING: WILL OVERWRITE ANY USER TYPE AND THEREFORE PERMISSIONS ALREADY GRANTED.
     * @param type The User type to be set. Uses the UserType Enum.
     */
    public void updateType(UserType type) {
        this.type.set(type);
        this.updateUser();
    }

    /**
     * Returns the type of the User.
     * @return Type of the User.
     */
    public UserType getType() {
        return type.get();
    }

    /**
     * Sets this user objects type.
     * <p><b>WARNING:</b> THIS DOES NOT UPDATE FIREBASE AND IS NOT MEANT FOR DEV USE.</p>
     * @param type Type to be set.
     */
    public void setType(UserType type) {
        this.type.set(type);
    }

    /**
     * <p>Takes an eventID and adds that event as a registered event for the User. Will set the status to pending and the time of registration to the local time that the function was called.</p>
     * <p>NOTE: This does not do any checking with regards to the event in question. Calling this will ALWAYS add the event to the users registered events.</p>
     *
     * @param eventId ID of the event being registered.
     * @return True if the event was added, false if the event is already registered.
     */
    public boolean addRegEvent(String eventId) {
        if(this.findRegEvent(eventId)) return false;
        this.regEvents.add(new RegisteredEvent(eventId));
        this.sort();
        this.controller.updateUser();
        return true;
    }

    /**
     * Returns the list of Registered event objects, which contain, eventID, registered date timestamp, and status.
     * Mainly for firestore implementation.
     * @return List of RegisteredEvents.
     */
    public List<RegisteredEvent> getRegEvents() {
        return this.regEvents;
    }

    /**
     * Returns all eventIDs of the events the User is registered for.
     * @return List of eventIDs.
     */
    @Exclude
    public List<String> getRegEventIds() {
        List<String> ret = new ArrayList<String>();
        for(RegisteredEvent event : this.regEvents) {
            ret.add(event.eventId);
        }
        return ret;
    }

    public RegisteredEvent getSingleRegEvent(String eventId) {
        int index = Collections.binarySearch(this.regEvents, new RegisteredEvent(eventId));
        if(index<0) {
            return null;
        }

        return this.regEvents.get(index);
    }

    /**
     * Remove an event from this users registered events based on the event ID.
     * @param eventId ID of the event you wish to remove.
     * @return True if the event is found and subsequently removed, false if the event could not be found.
     */
    public boolean removeRegEvent(String eventId) {
        int index = Collections.binarySearch(this.regEvents, new RegisteredEvent(eventId));
        if(index<0) {
            return false;
        }
        this.regEvents.remove(index);
        this.controller.updateUser();
        return true;
    }

    /**
     * Check if an event is in regEvents.
     * @param eventId ID of the event you wish to check.
     * @return True if the event is in the list, false otherwise.
     */
    public boolean findRegEvent(String eventId) {
        return Collections.binarySearch(this.regEvents, new RegisteredEvent(eventId)) >= 0;
    }

    /**
     * Sets the status of an event this user is registered in, denoted by the event ID provided.
     * @param eventId ID of the event to have it's status changed.
     * @param status Status of the event to be set.
     * @throws NoSuchFieldException Thrown if the User does not have this event registered.
     */
    public void setRegEventStatus(String eventId, Status status) throws NoSuchFieldException {
        int index = Collections.binarySearch(this.regEvents, new RegisteredEvent(eventId));
        if(index<0) throw new NoSuchFieldException("No such event ID " + eventId + " in this Users registered events.");
        this.regEvents.get(index).status=status;
        if(status == Status.Selected) this.regEvents.get(index).selectedDate=Timestamp.now();
        this.updateUser();
    }

    /**
     * Sets this user objects events.
     * <p><b>WARNING:</b> THIS DOES NOT UPDATE FIREBASE AND IS NOT MEANT FOR DEV USE.</p>
     * @param events Events to be set.
     */
    public void setRegEvents(List<RegisteredEvent> events) {
        this.regEvents=events;
    }

    /**
    * Gets the unique Device ID for user identification.
    */
    public String getId() {
        return deviceId;
    }

    /**
     * Checks whether the User exists already in the repo or not, or if there was an error in retrieving it.
     * @return UserStatus based on the return of the user repo fetch.
     */
    public UserStatus exists() {
        return this.status;
    }

    /**
     * Sorts this Users registered events by EventID. Called every time a new registered event is added.
     */
    private void sort() {
        // Sort purely based on the Event ID
        this.regEvents.sort(Comparator.comparing(o -> o.eventId));
    }

    /**
     * Updates the user information in the firebase. To be used after every user information change.
     */
    private void updateUser() {
        if(controller == null) controller = new UserController(this);
        controller.updateUser();
    }

    /**
     * Reloads this User data in case it has been changed within the lifetime of this User object.
     * NOTE: This should never be able to overwrite any unsaved changes since all changes are immediately pushed to firebase when using the given methods.
     * WARNING: THIS WILL VERY LIKELY RETURN BEFORE A RESULT IS RECEIVED: USE atomicReload() IF YOU NEED THE RESULT BEFORE CONTINUING.
     */
    public void reload() {
        AtomicUserCallback atomicCallback = new AtomicUserCallback(this);
        UserRepository.getInstance().getUserById(this.deviceId, atomicCallback);
    }

    /**
     * Reloads this User data in case it has been changed within the lifetime of this User object, but await this fetch to finish or fail before continuing on the main thread.
     * NOTE: This should never be able to overwrite any unsaved changes since all changes are immediately pushed to firebase when using the given methods.
     * WARNING: THIS WILL NOT BLOCK THE MAIN THREAD. ANY MANDATORY ACTIONS NEEDED AFTER A VALUE IS RETURNED SHOULD BE PLACED IN task.
     * @param task Action(s) to be performed upon BOTH a successful and unsuccessful reload.
     */
    public void atomicReload(Runnable task) {
        final AtomicUserCallback atomicCallback = new AtomicUserCallback(this, task);
        ExecutorService bgExec = Executors.newSingleThreadExecutor();
        bgExec.execute(() -> {
            UserRepository.getInstance().getUserById(this.deviceId, atomicCallback, bgExec);
        });
    }
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * This method will merge the data of two different User instances.
     * @param oldUser The old user whose data shall be merged into the new or overwritten.
     * <b>WARNING:</b> This will update <b>only</b> this user. oldUser will be <b>nullified</b>.
     */
    public void Merge(User oldUser) {
        this.regEvents.addAll(0, oldUser.getRegEvents());
        this.sort();
        // If the user was a higher position then replace the current one
        if (oldUser.type.type.ordinal() > this.type.type.ordinal()) this.type = oldUser.type;
        this.status = oldUser.status;
        oldUser.setRegEvents(null);
        oldUser.setEmail(null);
        oldUser.setName(null);
        oldUser.setPhone(null);
        oldUser.setType(null);
        this.updateUser();
    }
}

