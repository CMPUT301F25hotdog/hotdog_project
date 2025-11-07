package com.hotdog.elotto.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Organizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Repository class responsible for managing Organizer data access with Firebase Firestore.
 *
 * <p>This class handles all CRUD (Create, Read, Update, Delete) operations for Organizer data storage
 * in the Firebase Firestore database.
 *
 * <p>All database operations are asynchronous and use callback interfaces to return results
 * to the calling code.
 *
 * <p>the data access layer in the Model.
 *
 * <p><b>Design Pattern:</b> Repository pattern which centralizes data access logic and provides
 * a clean API for data operations.
 *
 *
 * @author Layne Pitman
 * @version 1.0
 * @since 2025-10-29
 */
public class OrganizerRepository {
    // initialize our collection name "Organizers" and our Firestore db.
    private static final String COLLECTION_NAME = "organizers";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final OrganizerRepository instance = new OrganizerRepository();

    /**
     * Returns the singleton instance of the OrganizerRepository.
     * @return Single instance of OrganizerRepo to do all firebase interactions.
     */
    public static OrganizerRepository getInstance() {return instance;}

    /**
     * Retrieves all Organizers from the Firestore database and turns them into Organizer objects.
     * Utilizes the callback interfaces.
     *
     * <p>Organizers are returned in the order they are stored in Firestore
     * The Controller will deal with sorting.<p/>
     *
     * @param callback The callback to receive the list of Organizers or error message
     */
    public void getAllOrganizers(FirestoreListCallback<Organizer> callback) {
        instance.db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Organizer> Organizers = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Organizer Organizer = document.toObject(Organizer.class);
                        Organizers.add(Organizer);
                    }

                    Log.d("OrganizerRepository", "Successfully fetched " + Organizers.size() + " Organizers");
                    callback.onSuccess(Organizers);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error fetching Organizers", e);
                    callback.onError("Failed to fetch Organizers: " + e.getMessage());
                });
    }

    /**
     * Retrieves all Organizers from the Firestore database and turns them into Organizer objects.
     * Utilizes the callback interfaces.
     *
     * <p>Organizers are returned in the order they are stored in Firestore
     * The Controller will deal with sorting.<p/>
     * @param callback The callback to receive the list of Organizers or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void getAllOrganizers(FirestoreListCallback<Organizer> callback, Executor bgThread) {
        instance.db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(bgThread, queryDocumentSnapshots -> {
                    List<Organizer> Organizers = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Organizer Organizer = document.toObject(Organizer.class);
                        Organizers.add(Organizer);
                    }

                    Log.d("OrganizerRepository", "Successfully fetched " + Organizers.size() + " Organizers");
                    callback.onSuccess(Organizers);
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("OrganizerRepository", "Error fetching Organizers", e);
                    callback.onError("Failed to fetch Organizers: " + e.getMessage());
                });
    }

    /**
     * Retrieves a single Organizer by its unique ID from Firestore.
     *
     * @param OrganizerId the unique identifier of the Organizer to retrieve
     * @param callback the callback to receive the Organizer or error message.
     */
    public void getOrganizerById(String OrganizerId, FirestoreCallback<Organizer> callback) {
        db.collection(COLLECTION_NAME)
                .document(OrganizerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Organizer Organizer = documentSnapshot.toObject(Organizer.class);
                        Log.d("OrganizerRepository", "Successfully fetched Organizer: " + OrganizerId);
                        callback.onSuccess(Organizer);
                    } else {
                        Log.w("OrganizerRepository", "Organizer not found: " + OrganizerId);
                        callback.onError("Organizer not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error fetching Organizer: " + OrganizerId, e);
                    callback.onError("Failed to fetch Organizer: " + e.getMessage());
                });
    }

    /**
     * Retrieves a single Organizer by its unique ID from Firestore.
     *
     * @param OrganizerId the unique identifier of the Organizer to retrieve.
     * @param callback the callback to receive the Organizer or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void getOrganizerById(String OrganizerId, FirestoreCallback<Organizer> callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .document(OrganizerId)
                .get()
                .addOnSuccessListener(bgThread, documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Organizer Organizer = documentSnapshot.toObject(Organizer.class);
                        Log.d("OrganizerRepository", "Successfully fetched Organizer: " + OrganizerId);
                        callback.onSuccess(Organizer);
                    } else {
                        Log.w("OrganizerRepository", "Organizer not found: " + OrganizerId);
                        callback.onError("Organizer not found");
                    }
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("OrganizerRepository", "Error fetching Organizer: " + OrganizerId, e);
                    callback.onError("Failed to fetch Organizer: " + e.getMessage());
                });
    }

    /**
     * Creates a new Organizer in the Firestore database.
     * Firestore will automatically generate a unique document ID for the Organizer.
     *
     * @param Organizer the Organizer object to create in the database.
     * @param callback the callback to receive success confirmation or error message.
     */
    public void createOrganizer(Organizer Organizer, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(Organizer)
                .addOnSuccessListener(documentReference -> {
                    Log.d("OrganizerRepository", "Successfully created Organizer: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error creating Organizer", e);
                    callback.onError("Failed to create Organizer: " + e.getMessage());
                });
    }

    /**
     * Creates a new Organizer in the Firestore database.
     * Firestore will automatically generate a unique document ID for the Organizer.
     *
     * @param Organizer the Organizer object to create in the database.
     * @param callback the callback to receive success confirmation or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void createOrganizer(Organizer Organizer, OperationCallback callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .add(Organizer)
                .addOnSuccessListener(bgThread, documentReference -> {
                    String uid = Organizer.getId();
                    // Update the document with its own ID
                    documentReference.update("id", uid)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("OrganizerRepository", "Organizer created successfully with ID: " + uid);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("OrganizerRepository", "Error updating Organizer ID", e);
                                callback.onError("Organizer created but failed to update ID: " + e.getMessage());
                            });
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("OrganizerRepository", "Error creating Organizer", e);
                    callback.onError("Failed to create Organizer: " + e.getMessage());
                });
    }

    /**
     * Updates an existing organizer in the Firestore database.
     * The organizer must have a valid ID set.
     *
     * @param organizer the organizer object with updated data
     * @param callback the callback to receive success confirmation or error message
     */
    public void updateOrganizer(Organizer organizer, OperationCallback callback) {
        if (organizer.getId() == null || organizer.getId().isEmpty()) {
            callback.onError("Cannot update organizer: ID is null or empty");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(organizer.getId())
                .set(organizer)
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrganizerRepository", "organizer updated successfully: " + organizer.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error updating organizer: " + organizer.getId(), e);
                    callback.onError("Failed to update organizer: " + e.getMessage());
                });
    }

    /**
     * Updates an existing Organizer in the Firestore database.
     * The Organizer must have a valid ID set.
     *
     * @param Organizer the Organizer object with updated data.
     * @param callback the callback to receive success confirmation or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void updateOrganizer(Organizer Organizer, OperationCallback callback, Executor bgThread) {
        if (Organizer.getId() == null || Organizer.getId().isEmpty()) {
            callback.onError("Cannot update Organizer: ID is null or empty");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(Organizer.getId())
                .set(Organizer)
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("OrganizerRepository", "Organizer updated successfully: " + Organizer.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("OrganizerRepository", "Error updating Organizer: " + Organizer.getId(), e);
                    callback.onError("Failed to update Organizer: " + e.getMessage());
                });
    }

    /**
     * Deletes an Organizer from the Firestore database.
     *
     * @param OrganizerId the unique identifier of the Organizer to delete.
     * @param callback the callback to receive success confirmation or error message.
     */
    public void deleteOrganizer(String OrganizerId, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(OrganizerId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrganizerRepository", "Organizer deleted successfully: " + OrganizerId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerRepository", "Error deleting Organizer: " + OrganizerId, e);
                    callback.onError("Failed to delete Organizer: " + e.getMessage());
                });
    }

    /**
     * Deletes an Organizer from the Firestore database.
     *
     * @param OrganizerId the unique identifier of the Organizer to delete.
     * @param callback the callback to receive success confirmation or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void deleteOrganizer(String OrganizerId, OperationCallback callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .document(OrganizerId)
                .delete()
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("OrganizerRepository", "Organizer deleted successfully: " + OrganizerId);
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("OrganizerRepository", "Error deleting Organizer: " + OrganizerId, e);
                    callback.onError("Failed to delete Organizer: " + e.getMessage());
                });
    }
}

