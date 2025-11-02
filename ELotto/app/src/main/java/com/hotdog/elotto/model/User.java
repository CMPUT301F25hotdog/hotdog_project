package com.hotdog.elotto.model;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.controller.UserController;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
* A class containing all common information for a User.
* All information required for Entrant is non-unique to Entrant, meaning we share it with Organizer and Administrator.
*/
@RequiresApi(api = Build.VERSION_CODES.O)
public class User {

    /**
     * The controller for this user. Excluded from firebase as it needs to be reinstantiated each time this user is made.
     */
    @Exclude
    private UserController controller;

    /**
     * A class that represents one registered event for a user. Each object holds a different event and information for one registration of one user.
     */
    private class RegisteredEvent {
        LocalDateTime registeredDate;
        Status status;
        String eventId;

        public RegisteredEvent(String eventId) {
            this.registeredDate = LocalDateTime.now();
            this.status = Status.Pending;
            this.eventId = eventId;
        }
    }



    // Personal Info
    private String name;
    private String email;
    private String phone;

    // Identifying information
    @DocumentId
    private final String deviceId;

    // Events
    private List<RegisteredEvent> regEvents; // Events registered in

    // Repo control
    private boolean exists;

    // Permission control
    private UserType type;

    /**
    * Class constructor that gets parent context to grab device ID and either pull or null User info.
    *
    * @param context The context of the caller to resolve device ID.
    * @throws NoSuchFieldException When the android device ID cannot be found.
    */
    public User(Context context) throws NoSuchFieldException {
        // Get deviceId
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(deviceId == null) throw new NoSuchFieldException("No android ID found.");

        // Create new controller
        this.controller = new UserController(this);

        // Creates a callback that sets a reference to the returned user and edits the info of this outer class user object.
        class UserCallback implements FirestoreCallback<User> {

            User superUser;
            public UserCallback(User superUser) {this.superUser=superUser;}
            public void onSuccess(User user) {
                // Set the info to the returned user value
                this.superUser.setUser(user);
                this.superUser.exists=true;
            }

            public void onError(String errorMessage) {
                Log.d("USER_REPO", errorMessage);
                this.superUser.exists=false;
            }
        }

        UserRepository.getInstance().getUserById(this.deviceId, new UserCallback(this));
    }

    /**
     * Set this user object info to the given user object info.
     * @param user The user object to grab info from.
     * @return True if given user exists, false otherwise.
     */
    private boolean setUser(User user) {
        if (!this.exists) return false;

        this.name=user.name;
        this.email=user.email;
        this.phone=user.phone;
        this.regEvents=user.regEvents;
        return true;
    }

    /**
     * Updates the name for this User, overwriting the current name if there is one.
     * @param name Name to be set.
     */
    public void updateName(String name) {
        this.name = name;
        this.updateUser();
    }

    /**
     * Returns the name of this User.
     * @return Name of the User.
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the email for this User, overwriting the current email if there is one.
     * @param email Email to be set.
     */
    public void updateEmail(String email) {
        this.email = email;
        this.updateUser();
    }

    /**
     * Returns the email of this User.
     * @return Email of the User.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the phone number for this User, overwriting the current phone number if there is one.
     * @param phone Phone number to be set.
     */
    public void updatePhone(String phone) {
        this.phone = phone;
        this.updateUser();
    }

    /**
     * Return the phone number of this User.
     * @return Phone number of the User.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Updates the current User type for this user. WARNING: WILL OVERWRITE ANY USER TYPE AND THEREFORE PERMISSIONS ALREADY GRANTED.
     * @param type The User type to be set. Uses the UserType Enum.
     */
    public void updateType(UserType type) {
        this.type = type;
        this.updateUser();
    }

    /**
     * Returns the type of the User.
     * @return Type of the User.
     */
    public UserType getType() {
        return type;
    }

    /**
     * <p>Takes an eventID and adds that event as a registered event for the User. Will set the status to pending and the time of registration to the local time that the function was called.</p>
     * <p>NOTE: This does not do any checking with regards to the event in question. Calling this will ALWAYS add the event to the users registered events.</p>
     *
     * @param eventId ID of the event being registered.
     */
    public void addRegEvent(String eventId) {
        this.regEvents.add(new RegisteredEvent(eventId));
        this.sort();
    }

    /**
     * Returns all eventIDs of the events the User is registered for.
     * @return List of eventIDs.
     */
    public List<String> getRegEvents() {
        List<String> ret = new ArrayList<String>();
        for(RegisteredEvent event : this.regEvents) {
            ret.add(event.eventId);
        }
        return ret;
    }

    /**
     * Sets the status of an event this user is registered in, denoted by the event ID provided.
     * @param eventId ID of the event to have it's status changed.
     * @param status Status of the event to be set.
     * @throws NoSuchFieldException Thrown if the User does not have this event registered.
     */
    public void setRegEventStatus(String eventId, Status status) throws NoSuchFieldException {
        int index = Collections.binarySearch(this.getRegEvents(), eventId, (String id1, String id2) -> id1.compareTo(id2));
        if(index<0) throw new NoSuchFieldException("No such event ID " + eventId + " in this Users registered events.");
        this.regEvents.get(index).status=status;
    }

    /**
    * Gets the unique Device ID for user identification.
    */
    public String getId() {
        return deviceId;
    }

    /**
     * Checks whether the User exists already in the repo or not.
     * @return True if the user is in the db False otherwise.
     */
    public boolean exists() {
        return this.exists;
    }

    /**
     * Sets the user's basic information when the user doesn't already exist.
     * NOTE: DO NOT use this to update user values. Use the corresponding User.update___ method instead.
     * @param name Users name
     * @param email Users email
     */
    public void setUser(String name, String email) {
        this.name=name;
        this.email=email;
        this.phone="";
        this.type=UserType.Entrant;
        this.updateUser();
    }

    /**
     * Sets the user's basic information when the user doesn't already exist.
     * NOTE: DO NOT use this to update user values. Use the corresponding User.update___ method instead.
     * @param name Users name
     * @param email Users email
     * @param phone Users phone number
     */
    public void setUser(String name, String email, String phone) {
        this.name=name;
        this.email=email;
        this.phone=phone;
        this.type=UserType.Entrant;
        this.updateUser();
    }

    /**
     * Sorts this Users registered events by EventID. Called every time a new registered event is added.
     */
    private void sort() {
        // Sort purely based on the Event ID
        this.regEvents.sort(new Comparator<RegisteredEvent>() {
            @Override
            public int compare(RegisteredEvent o1, RegisteredEvent o2) {
                return o1.eventId.compareTo(o2.eventId);
            }
        });
    }

    /**
     * Updates the user information in the firebase. To be used after every user information change.
     */
    private void updateUser() {
        controller.updateUser();
    }
}
