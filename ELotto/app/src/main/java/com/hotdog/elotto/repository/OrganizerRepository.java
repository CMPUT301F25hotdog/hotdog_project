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

    public class OrganizerRepository {
        private static final String COLLECTION_NAME = "organizers";
        private static OrganizerRepository instance;
        private final FirebaseFirestore db;
        private final Executor bgThread = Executors.newSingleThreadExecutor();

        public OrganizerRepository() {
            db = FirebaseFirestore.getInstance();
        }

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