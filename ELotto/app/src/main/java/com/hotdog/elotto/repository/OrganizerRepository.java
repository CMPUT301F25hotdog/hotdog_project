package com.hotdog.elotto.repository;

import android.util.Log;

    import com.google.firebase.firestore.FieldValue;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.hotdog.elotto.callback.FirestoreCallback;
    import com.hotdog.elotto.callback.FirestoreListCallback;
    import com.hotdog.elotto.callback.OperationCallback;
    import com.hotdog.elotto.model.Event;
    import com.hotdog.elotto.model.Organizer;
    import com.hotdog.elotto.model.User;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.Executor;
    import java.util.concurrent.Executors;

/**
 * Repository class responsible for handling Firestore database operations related to Organizer
 *
 */
public class OrganizerRepository {
    private static final String COLLECTION_NAME = "organizers";
    private static OrganizerRepository instance;
    private final FirebaseFirestore db;
    private final Executor bgThread = Executors.newSingleThreadExecutor();

    /**
     * Constructs an OrganizerRepository instance and initializes Firestore.
     */
    public OrganizerRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new organizer document in Firestore.
     *
     * @param organizer the  Organizer object to be stored in the database
     * @param callback  the callback to handle success or failure events
     */
    public void createOrganizer(Organizer organizer, OperationCallback callback) {
        String orgID = organizer.getOrgID();
        db.collection(COLLECTION_NAME)
                .document(orgID)
                .set(organizer)
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("UserRepository", "Organizer created with ID: " + orgID);
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("UserRepository", "Error creating Organizer", e);
                    callback.onError("Failed to create Organizer: " + e.getMessage());
                });
    }

    /**
     * Retrieves an organizer document from Firestore using that organizers ID.
     *
     * @param orgID    the ID of the organizer
     * @param callback the FirestoreCallback that returns the Organizer object
     */
    public void getOrganizerById(String orgID, FirestoreCallback<Organizer> callback) {
        db.collection(COLLECTION_NAME)
                .document(orgID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Organizer organizer = doc.toObject(Organizer.class);
                        callback.onSuccess(organizer);
                    } else {
                        callback.onError("Organizer not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Error fetching organizer: " + e.getMessage()));
    }

    /**
     * Checks if the organizer already exists for the given orgID, if so then updates that organizers list
     * and checks if the event is already in the organizers list. If no organizer exists for that ID then creates
     * a new organizer document
     *
     * @param orgID     the unique ID of the organizer to update or create
     * @param eventID   the ID of the event created by the organizer
     * @param callback  the callback to handle success or failure events
     */
    public void updateOrganizer(String orgID, String eventID, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(orgID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Organizer organizer = doc.toObject(Organizer.class);
                        if (organizer != null) {
                            if (!organizer.getCreatedEvents().contains(eventID)) {
                                organizer.addCreatedEvent(eventID);
                            }
                            db.collection(COLLECTION_NAME)
                                    .document(orgID)
                                    .set(organizer)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("OrganizerRepository", "Organizer updated with new event: " + eventID);
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("OrganizerRepository", "Failed to update organizer", e);
                                        callback.onError("Failed to update organizer: " + e.getMessage());
                                    });
                        } else {
                            callback.onError("Organizer data is null");
                        }
                    } else {
                        Organizer newOrganizer = new Organizer(orgID);
                        newOrganizer.addCreatedEvent(eventID);
                        db.collection(COLLECTION_NAME)
                                .document(orgID)
                                .set(newOrganizer)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("OrganizerRepository", "New organizer created with event: " + eventID);
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("OrganizerRepository", "Failed to create new organizer", e);
                                    callback.onError("Failed to create new organizer: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error fetching organizer", e);
                    callback.onError("Error fetching organizer: " + e.getMessage());
                });

    }
    public void deleteOrganizer(String orgID, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(orgID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrganizerRepository", "Organizer deleted successfully: " + orgID);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error deleting Organizer: " + orgID, e);
                    callback.onError("Failed to delete Organizer: " + e.getMessage());
                });
    }
}