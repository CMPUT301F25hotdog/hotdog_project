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
 * Controller for managing event creation and update operations.
 *
 * <p>This controller handles the business logic for creating and updating events,
 * including image encoding, data validation, and interaction with Firebase Firestore
 * and Storage. Provides methods for encoding poster images to Base64 format and
 * managing event lifecycle operations.</p>
 *
 * <p>Controller layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh Batta
 * @version 1.0
 * @since 2025-11-01
 */
public class EventCreationController {
    /**
     * Firestore database instance for event operations.
     */
    private FirebaseFirestore db;

    /**
     * Reference to the events collection in Firestore.
     */
    private CollectionReference eventsRef;

    /**
     * Firebase Storage instance for image storage operations.
     */
    private FirebaseStorage storage;

    /**
     * Application context for accessing resources and system services.
     */
    private final Context context;

    /**
     * Repository for event data access operations.
     */
    private EventRepository repository;

    /**
     * Constructs a new EventCreationController with default repository.
     *
     * @param context the application context
     */
    public EventCreationController(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        this.repository = new EventRepository();
    }

    /**
     * Constructs a new EventCreationController with dependency injection.
     *
     * <p>This constructor is useful for testing with mock repositories.</p>
     *
     * @param context the application context
     * @param repository the EventRepository instance to use
     */
    public EventCreationController(Context context, EventRepository repository) {
        this.context = context;
        this.repository = repository;
    }

    /**
     * Encodes an image URI into a Base64 string for storage in Firestore.
     *
     * <p>Converts the image to JPEG format with 80% quality compression before
     * encoding to reduce storage size. Returns fallback strings if encoding fails:</p>
     * <ul>
     *     <li>"no_image" - if URI is null</li>
     *     <li>"image_failed_null" - if input stream is null</li>
     *     <li>"image_failed_exception" - if encoding throws an exception</li>
     * </ul>
     *
     * @param bannerUri the URI pointing to the banner image selected by the user
     * @return Base64-encoded image string or fallback error identifier
     * @see <a href="https://stackoverflow.com/questions/49265931/how-to-add-an-image-to-a-record-in-a-firestore-database">Stack Overflow Reference</a>
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

    /**
     * Creates a new event and saves it to Firestore.
     *
     * <p>This method constructs an Event object with the provided parameters, saves it
     * to Firestore using the EventRepository, adds the event to the organizer's event
     * list, and navigates to the QR code generation screen upon successful creation.</p>
     *
     * <p>On success, displays a success toast, updates the organizer's event list,
     * launches the QRCodeView activity, and finishes the current activity with RESULT_OK.</p>
     *
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
     * @param tagList the list of tags for categorizing the event
     */
    public void SaveEvent(String name, String description, Date dateTime, Date openPeriod,
                          Date closePeriod, int entrantLimit, int waitListSize,
                          String location, double price, boolean requireGeo, String bannerUrl,ArrayList<String> tagList) {
        Event event = new Event(name, description, location, dateTime, openPeriod, closePeriod, entrantLimit, "todo");
        event.setCreatedAt(new Date());
        event.setUpdatedAt(new Date());
        event.setGeolocationRequired(requireGeo);
        event.setPosterImageUrl(bannerUrl);
        event.setPrice(price);
        event.setTagList(tagList);
        Organizer org = new Organizer(context);
        event.setOrganizerId(org.getId());
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
     * Updates an existing event in Firestore with new information.
     *
     * <p>This method constructs an Event object with the updated parameters and saves
     * it to Firestore using the EventRepository. The organizer ID is preserved from
     * the current organizer context.</p>
     *
     * <p>On success, displays a success toast and finishes the current activity. On
     * failure, displays an error toast with the error message.</p>
     *
     * @param eventId the unique identifier of the event to update
     * @param name the updated name of the event
     * @param description the updated description of the event
     * @param dateTime the updated scheduled event date and time
     * @param openPeriod the updated registration open date
     * @param closePeriod the updated registration close date
     * @param entrantLimit the updated maximum number of attendees allowed
     * @param waitListSize the updated number of users that can be on the waitlist
     * @param location the updated physical or virtual location of the event
     * @param price the updated cost to participate in the event
     * @param requireGeo whether the event enforces geolocation verification
     * @param bannerUrl the updated Base64-encoded image string or fallback identifier
     * @param tagList the updated list of tags for the event
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