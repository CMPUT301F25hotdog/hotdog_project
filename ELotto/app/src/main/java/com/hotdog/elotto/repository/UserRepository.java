package com.hotdog.elotto.repository;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for managing User data access with Firebase Firestore.
 *
 * <p>This class handles all CRUD (Create, Read, Update, Delete) operations for User data storage
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
@RequiresApi(api = Build.VERSION_CODES.O)
public class UserRepository {
    // initialize our collection name "Users" and our Firestore db.
    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final UserRepository instance = new UserRepository();

    public static UserRepository getInstance() {return instance;}

    /**
     * Retrieves all Users from the Firestore database and turns them into User objects.
     * Utilizes the callback interfaces.
     *
     * <p>Users are returned in the order they are stored in Firestore
     * The Controller will deal with sorting.
     *
     * @param callback the callback to receive the list of Users or error message
     */
    public void getAllUsers(FirestoreListCallback<User> callback) {
        instance.db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> Users = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User User = document.toObject(User.class);
                        Users.add(User);
                    }

                    Log.d("UserRepository", "Successfully fetched " + Users.size() + " Users");
                    callback.onSuccess(Users);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching Users", e);
                    callback.onError("Failed to fetch Users: " + e.getMessage());
                });
    }

    /**
     * Retrieves a single User by its unique ID from Firestore.
     *
     * @param userId the unique identifier of the User to retrieve
     * @param callback the callback to receive the User or error message
     */
    public void getUserById(String userId, FirestoreCallback<User> callback) {
        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Log.d("UserRepository", "Successfully fetched User: " + userId);
                        callback.onSuccess(user);
                    } else {
                        Log.w("UserRepository", "User not found: " + userId);
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching User: " + userId, e);
                    callback.onError("Failed to fetch User: " + e.getMessage());
                });
    }

    /**
     * Retrieves all Users created by a specific organizer.
     *
     * @param organizerId the unique identifier of the organizer
     * @param callback the callback to receive the list of Users or error message
     */
    public void getUsersByOrganizer(String organizerId, FirestoreListCallback<User> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> Users = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User User = document.toObject(User.class);
                        Users.add(User);
                    }

                    Log.d("UserRepository", "Successfully fetched " + Users.size() + " Users for organizer: " + organizerId);
                    callback.onSuccess(Users);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching Users for organizer: " + organizerId, e);
                    callback.onError("Failed to fetch organizer Users: " + e.getMessage());
                });
    }

    /**
     * Retrieves all Users with a specific status (e.g., "OPEN", "CLOSED", "FULL").
     *
     * @param status the status to filter by
     * @param callback the callback to receive the list of Users or error message
     */
    public void getUsersByStatus(String status, FirestoreListCallback<User> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> Users = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User User = document.toObject(User.class);
                        Users.add(User);
                    }

                    Log.d("UserRepository", "Successfully fetched " + Users.size() + " Users with status: " + status);
                    callback.onSuccess(Users);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching Users by status: " + status, e);
                    callback.onError("Failed to fetch Users by status: " + e.getMessage());
                });
    }

    /**
     * Creates a new User in the Firestore database.
     * Firestore will automatically generate a unique document ID for the User.
     *
     * @param User the User object to create in the database
     * @param callback the callback to receive success confirmation or error message
     */
    public void createUser(User User, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(User)
                .addOnSuccessListener(documentReference -> {
                    String uid = User.getId();
                    // Update the document with its own ID
                    documentReference.update("id", uid)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("UserRepository", "User created successfully with ID: " + uid);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UserRepository", "Error updating User ID", e);
                                callback.onError("User created but failed to update ID: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error creating User", e);
                    callback.onError("Failed to create User: " + e.getMessage());
                });
    }

    /**
     * Updates an existing User in the Firestore database.
     * The User must have a valid ID set.
     *
     * @param User the User object with updated data
     * @param callback the callback to receive success confirmation or error message
     */
    public void updateUser(User User, OperationCallback callback) {
        if (User.getId() == null || User.getId().isEmpty()) {
            callback.onError("Cannot update User: ID is null or empty");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(User.getId())
                .set(User)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserRepository", "User updated successfully: " + User.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error updating User: " + User.getId(), e);
                    callback.onError("Failed to update User: " + e.getMessage());
                });
    }

    /**
     * Deletes an User from the Firestore database.
     *
     * @param UserId the unique identifier of the User to delete
     * @param callback the callback to receive success confirmation or error message
     */
    public void deleteUser(String UserId, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(UserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserRepository", "User deleted successfully: " + UserId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error deleting User: " + UserId, e);
                    callback.onError("Failed to delete User: " + e.getMessage());
                });
    }
}

