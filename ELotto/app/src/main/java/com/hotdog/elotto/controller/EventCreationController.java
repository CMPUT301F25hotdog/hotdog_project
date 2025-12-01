package com.hotdog.elotto.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.ui.home.QRCodeView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Controller class responsible for managing location logic, including
 * finding and converting current location of the user.
 */
public class    EventCreationController {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private FirebaseStorage storage;
    private final Context context;
    private EventRepository repository;
    private boolean testMode = false;
    /**
     * Constructs a new EventCreationController.
     *
     * @param context the current context
     */
    public EventCreationController(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        this.repository = new EventRepository();
    }
    public EventCreationController(Context context, EventRepository repository) {
        this.context = context;
        this.repository = repository;
    }
    /**
     * Encodes an image Uri into a string to be stored
     * @param bannerUri     a URI pointing to the banner image selected by the user.
     *
     * https://stackoverflow.com/questions/49265931/how-to-add-an-image-to-a-record-in-a-firestore-database
     * used to figure out how to convert images into strings to store
     */
    public String EncodeImage(Uri bannerUri) {
        String base64String = "no_image";
        if (bannerUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(bannerUri);
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                } else {
                    base64String = "image_failed_null";
                }
            } catch (Exception e) {
                e.printStackTrace();
                base64String = "image_failed_exception";
            }
        }
        return base64String;
    }
    public void setTestMode(boolean testMode){
        this.testMode = testMode;
    }
    /**
     * Creates a new event and saves it to FireStore using the EventRepository class, then opens up the QRCode
     * Screen, also runs updateOrganizer which creates or updates an organizer
     *
     * @param name          the name of the event.
     * @param description   a brief description of the event.
     * @param dateTime      the scheduled event date and time.
     * @param openPeriod    the registration open date.
     * @param closePeriod   the registration close date.
     * @param entrantLimit  the maximum number of attendees allowed.
     * @param waitListSize  the number of users that can be on the waitlist.
     * @param location      the physical or virtual location of the event.
     * @param price         the cost to participate in the event.
     * @param requireGeo    whether the event enforces geolocation verification.
     * @param bannerUrl     the Base64-encoded image string or fallback identifier.
     * @param tagList       a string list of tags
     * @param organizerName the organizers name
     */
    public void SaveEvent(String name, String description, Date dateTime, Date openPeriod,
                          Date closePeriod, int entrantLimit, int waitListSize,
                          String location, double price, boolean requireGeo, String bannerUrl,ArrayList<String> tagList,String organizerName) {
        if(testMode){
            Intent qrIntent = new Intent(context, QRCodeView.class);
            qrIntent.putExtra("EVENT_NAME", name);
            qrIntent.putExtra("EVENT_ID", "1728");
            context.startActivity(qrIntent);
            if (context instanceof Activity) {
                ((Activity) context).setResult(Activity.RESULT_OK);
                ((Activity) context).finish();
            }
        }
        Event event = new Event(name, description, location, dateTime, openPeriod, closePeriod, entrantLimit, "todo");
        event.setCreatedAt(new Date());
        event.setUpdatedAt(new Date());
        event.setGeolocationRequired(requireGeo);
        event.setPosterImageUrl(bannerUrl);
        event.setPrice(price);
        event.setTagList(tagList);
        Organizer org = new Organizer(context);
        event.setOrganizerId(org.getId());
        event.setOrganizerName(organizerName);
        repository.createEvent(event, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Event created successfully!", Toast.LENGTH_SHORT).show();

                Organizer organizer = new Organizer(context);
                organizer.addEvent(event.getId());
                Intent qrIntent = new Intent(context, QRCodeView.class);
                qrIntent.putExtra("EVENT_NAME", name);
                qrIntent.putExtra("EVENT_ID", event.getId());
                context.startActivity(qrIntent);
                if (context instanceof Activity) {
                    ((Activity) context).setResult(Activity.RESULT_OK);
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "Failed to create event", Toast.LENGTH_LONG).show();
            }
        });
    }
    /**
     * Updates an existing event in the database
     *
     * @param eventId the ID of the event to update
     * @param name the name of the event
     * @param description a brief description of the event
     * @param dateTime the scheduled event date and time
     * @param openPeriod the registration open date
     * @param closePeriod the registration close date
     * @param entrantLimit the maximum number of attendees allowed
     * @param waitListSize the number of users that can be on the waitlist
     * @param location the physical or virtual location of the event
     * @param price the cost to participate in the event
     * @param requireGeo whether the event enforces geolocation verification
     * @param bannerUrl the Base64-encoded image string or fallback identifier
     * @param tagList the list of tags for the event
     */
    public void UpdateEvent(String eventId, String name, String description, Date dateTime,
                            Date openPeriod, Date closePeriod, int entrantLimit, int waitListSize,
                            String location, double price, boolean requireGeo, String bannerUrl,
                            ArrayList<String> tagList) {

        // Create event object with updated data
        Event event = new Event(name, description, location, dateTime, openPeriod, closePeriod, entrantLimit, "todo");
        event.setUpdatedAt(new Date());
        event.setGeolocationRequired(requireGeo);
        event.setPosterImageUrl(bannerUrl);
        event.setPrice(price);
        event.setTagList(tagList);

        // Keep the organizer ID (don't change it)
        Organizer org = new Organizer(context);
        event.setOrganizerId(org.getId());

        // Update the event in the database
        repository.updateEvent(eventId, event, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "Failed to update event: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

}