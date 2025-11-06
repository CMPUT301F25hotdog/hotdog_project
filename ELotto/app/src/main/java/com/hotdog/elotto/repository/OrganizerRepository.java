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
 * Repository class responsible for handling Firestore database operations related to {@link Organizer}.
 * <p>
 * The {@code OrganizerRepository} provides methods to create, retrieve, and update organizer documents
 * stored in the Firestore database. It acts as the data access layer, isolating Firebase operations
 * from the rest of the application.
 * </p>
 */
public class OrganizerRepository {
    /** The Firestore collection name where organizer documents are stored. */
    private static final String COLLECTION_NAME = "organizers";

    /** Singleton instance of the repository (if used elsewhere). */
    private static OrganizerRepository instance;

    /** The Firestore database reference. */
    private final FirebaseFirestore db;

    /** A background thread executor for Firestore operations. */
    private final Executor bgThread = Executors.newSingleThreadExecutor();

    /**
     * Constructs an {@code OrganizerRepository} instance and initializes Firestore.
     */
    public OrganizerRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new organizer document in Firestore.
     *
     * @param organizer the {@link Organizer} object to be stored in the database
     * @param callback  the {@link OperationCallback} to handle success or failure events
     */
    public void createOrganizer(Organizer organizer, OperationCallback callback) {
        String orgID = organizer.getOrgID();
        db.collection(COLLECTION_NAME)
                .document(orgID)
                .set(organizer)
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("UserRepository", "Organizer created successfully with ID: " + orgID);
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("UserRepository", "Error creating Organizer", e);
                    callback.onError("Failed to create Organizer: " + e.getMessage());
                });
    }

    /**
     * Retrieves an organizer document from Firestore using its unique ID.
     *
     * @param orgID    the unique ID of the organizer
     * @param callback the {@link FirestoreCallback} that returns the {@link Organizer} object
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
     * Updates an existing organizer’s list of created events, or creates a new organizer if none exists.
     * <p>
     * This method first attempts to fetch the organizer document from Firestore.
     * If it exists, the method adds the specified event ID to their list of created events (if not already present),
     * then updates the document. If it does not exist, a new organizer document is created with the given event ID.
     * </p>
     *
     * @param orgID     the unique ID of the organizer to update or create
     * @param eventID   the ID of the event to associate with the organizer
     * @param callback  the {@link OperationCallback} to handle success or failure events
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
}