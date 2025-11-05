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
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.ui.home.QRCodeView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * Controller class responsible for managing event creation logic, including
 * data processing, image encoding, and interaction with Firestore and Storage.
 * <p>
 * The {@code EventCreationController} handles:
 * <ul>
 *     <li>Validating and encoding an event's banner image into Base64 format.</li>
 *     <li>Saving new event records to Firestore using {@link EventRepository}.</li>
 *     <li>Navigating to {@link QRCodeView} upon successful event creation.</li>
 * </ul>
 *
 * This controller bridges the app’s UI (such as {@link com.hotdog.elotto.ui.home.EventCreationView})
 * and the backend data management layers.
 */
public class EventCreationController {

    /** Firestore database instance for interacting with event data. */
    private FirebaseFirestore db;

    /** Reference to the Firestore 'events' collection. */
    private CollectionReference eventsRef;

    /** Firebase Storage instance (currently unused but reserved for image storage). */
    private FirebaseStorage storage;

    /** Android context used for accessing content resolvers and starting new activities. */
    private final Context context;

    /**
     * Constructs a new {@code EventCreationController}.
     *
     * @param context the current context, used for resource access and UI updates.
     */
    public EventCreationController(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
    }

    /**
     * Creates a new event record, including encoding its banner image and saving metadata.
     * <p>
     * If a banner image URI is provided, it is converted to a Base64-encoded string and
     * passed along with other event data to {@link #SaveEvent(String,String, String, Date, Date, Date, int, int, String, double, boolean, String)}.
     * </p>
     * In the event of an error (e.g., null input stream or failed image decoding),
     * the controller stores a fallback banner string indicating the failure type.
     *
     * @param name          the name of the event.
     * @param description   a short description of the event.
     * @param dateTime      the scheduled date and time of the event.
     * @param openPeriod    the event’s opening registration period.
     * @param closePeriod   the event’s closing registration period.
     * @param entrantLimit  the maximum number of entrants allowed.
     * @param waitListSize  the maximum size of the event waitlist.
     * @param location      the location of the event.
     * @param price         the ticket or entry price for the event.
     * @param requireGeo    whether geolocation is required for participation.
     * @param bannerUri     a URI pointing to the banner image selected by the user.
     *
     * @see <a href="https://stackoverflow.com/questions/49265931/how-to-add-an-image-to-a-record-in-a-firestore-database">
     *      Stack Overflow reference: How to add an image to a record in Firestore</a>
     */
    public void CreateEvent(String currentUser, String name, String description, Date dateTime, Date openPeriod,
                            Date closePeriod, int entrantLimit, int waitListSize,
                            String location, double price, boolean requireGeo, Uri bannerUri) {
        if (bannerUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(bannerUri);
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    SaveEvent(currentUser, name, description, dateTime, openPeriod, closePeriod,
                            entrantLimit, waitListSize, location, price, requireGeo, base64String);
                } else {
                    SaveEvent(currentUser,name, description, dateTime, openPeriod, closePeriod,
                            entrantLimit, waitListSize, location, price, requireGeo,
                            "image_failed_nullinput_" + System.currentTimeMillis());
                }
            } catch (Exception e) {
                e.printStackTrace();
                SaveEvent(currentUser,name, description, dateTime, openPeriod, closePeriod,
                        entrantLimit, waitListSize, location, price, requireGeo,
                        "image_failed_exception_" + System.currentTimeMillis());
            }
        } else {
            SaveEvent(currentUser,name, description, dateTime, openPeriod, closePeriod,
                    entrantLimit, waitListSize, location, price, requireGeo, "no_image");
        }
    }

    /**
     * Saves an event object to Firestore and opens the QR code display screen upon success.
     * <p>
     * The event metadata is stored using an instance of {@link EventRepository}.
     * If the save operation succeeds, the user is redirected to {@link QRCodeView},
     * where a QR code for the newly created event is generated and displayed.
     * </p>
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
     */
    public void SaveEvent(String currentUser,String name, String description, Date dateTime, Date openPeriod,
                          Date closePeriod, int entrantLimit, int waitListSize,
                          String location, double price, boolean requireGeo, String bannerUrl) {
        Event event = new Event(name, description, location, dateTime, openPeriod, closePeriod, entrantLimit, "todo");
        event.setCreatedAt(new Date());
        event.setUpdatedAt(new Date());
        event.setGeolocationRequired(requireGeo);
        event.setPosterImageUrl(bannerUrl);
        event.setPrice(price);

        EventRepository repository = new EventRepository();
        repository.createEvent(event, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Event created successfully!", Toast.LENGTH_SHORT).show();

                OrganizerController organizerController = new OrganizerController();
                organizerController.updateOrganizerEvents(currentUser,event.getId(),new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Yep", Toast.LENGTH_LONG).show()
                        );
                    }

                    @Override
                    public void onError(String errorMessage) {
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Nope", Toast.LENGTH_LONG).show()
                        );
                        Log.e("EventCreation", "Failed to update organizer: " + errorMessage);
                    }
                });
                Toast.makeText(context, "idk man", Toast.LENGTH_LONG).show();
                Intent qrIntent = new Intent(context, QRCodeView.class);
                qrIntent.putExtra("EVENT_NAME", name);
                qrIntent.putExtra("EVENT_ID", event.getId());
                context.startActivity(qrIntent);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "Failed to create event", Toast.LENGTH_LONG).show();
            }
        });
    }
}