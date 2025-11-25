package com.hotdog.elotto.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.User;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

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
public class UserRepository {
    // initialize our collection name "Users" and our Firestore db.
    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final UserRepository instance = new UserRepository();

    /**
     * Returns the singleton instance of the UserRepository.
     * @return Single instance of UserRepo to do all firebase interactions.
     */
    public static UserRepository getInstance() {return instance;}

    /**
     * Retrieves all Users from the Firestore database and turns them into User objects.
     * Utilizes the callback interfaces.
     *
     * <p>Users are returned in the order they are stored in Firestore
     * The Controller will deal with sorting.<p/>
     *
     * @param callback The callback to receive the list of Users or error message
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
     * Retrieves all Users from the Firestore database and turns them into User objects.
     * Utilizes the callback interfaces.
     *
     * <p>Users are returned in the order they are stored in Firestore
     * The Controller will deal with sorting.<p/>
     * @param callback The callback to receive the list of Users or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void getAllUsers(FirestoreListCallback<User> callback, Executor bgThread) {
        instance.db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(bgThread, queryDocumentSnapshots -> {
                    List<User> Users = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User User = document.toObject(User.class);
                        Users.add(User);
                    }

                    Log.d("UserRepository", "Successfully fetched " + Users.size() + " Users");
                    callback.onSuccess(Users);
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("UserRepository", "Error fetching Users", e);
                    callback.onError("Failed to fetch Users: " + e.getMessage());
                });
    }

    /**
     * Retrieves a single User by its unique ID from Firestore.
     *
     * @param userId the unique identifier of the User to retrieve
     * @param callback the callback to receive the User or error message.
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
     * Retrieves a single User by its unique ID from Firestore.
     *
     * @param userId the unique identifier of the User to retrieve.
     * @param callback the callback to receive the User or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void getUserById(String userId, FirestoreCallback<User> callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(bgThread, documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Log.d("UserRepository", "Successfully fetched User: " + userId);
                        callback.onSuccess(user);
                    } else {
                        Log.w("UserRepository", "User not found: " + userId);
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("UserRepository", "Error fetching User: " + userId, e);
                    callback.onError("Failed to fetch User: " + e.getMessage());
                });
    }

    /**
     * Retrieves multiple users by their IDs from Firestore.
     * Useful for fetching entrant information when you have a list of user IDs.
     *
     * @param userIds the list of user IDs to retrieve
     * @param callback the callback to receive the list of Users or error message
     */
    public void getUsersByIds(List<String> userIds, FirestoreListCallback<User> callback) {
        if (userIds == null || userIds.isEmpty()) {
            Log.w("UserRepository", "No user IDs provided");
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Use Tasks.whenAllComplete to fetch multiple users in parallel
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String userId : userIds) {
            tasks.add(db.collection(COLLECTION_NAME).document(userId).get());
        }

        Tasks.whenAllComplete(tasks).addOnSuccessListener(doneTasks -> {
            List<User> users = new ArrayList<>();
            for (Task<DocumentSnapshot> task : tasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                }
            }
            Log.d("UserRepository", "Successfully fetched " + users.size() + " users");
            callback.onSuccess(users);
        }).addOnFailureListener(e -> {
            Log.e("UserRepository", "Error fetching users by IDs", e);
            callback.onError("Failed to fetch users: " + e.getMessage());
        });
    }

    /**
     * Creates a new User in the Firestore database.
     * Firestore will automatically generate a unique document ID for the User.
     *
     * @param User the User object to create in the database.
     * @param callback the callback to receive success confirmation or error message.
     */
    public void createUser(User User, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(User)
                .addOnSuccessListener(documentReference -> {
                    Log.d("UserRepository", "Successfully created user: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error creating User", e);
                    callback.onError("Failed to create User: " + e.getMessage());
                });
    }

    /**
     * Creates a new User in the Firestore database.
     * Firestore will automatically generate a unique document ID for the User.
     *
     * @param User the User object to create in the database.
     * @param callback the callback to receive success confirmation or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void createUser(User User, OperationCallback callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .add(User)
                .addOnSuccessListener(bgThread, documentReference -> {
                    Log.d("UserRepository", "Successfully created user: " + documentReference.getId());
                    callback.onSuccess();
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
     * Updates an existing User in the Firestore database.
     * The User must have a valid ID set.
     *
     * @param User the User object with updated data.
     * @param callback the callback to receive success confirmation or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void updateUser(User User, OperationCallback callback, Executor bgThread) {
        if (User.getId() == null || User.getId().isEmpty()) {
            callback.onError("Cannot update User: ID is null or empty");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(User.getId())
                .set(User)
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("UserRepository", "User updated successfully: " + User.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("UserRepository", "Error updating User: " + User.getId(), e);
                    callback.onError("Failed to update User: " + e.getMessage());
                });
    }

    /**
     * Deletes an User from the Firestore database.
     *
     * @param UserId the unique identifier of the User to delete.
     * @param callback the callback to receive success confirmation or error message.
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

    /**
     * Deletes an User from the Firestore database.
     *
     * @param UserId the unique identifier of the User to delete.
     * @param callback the callback to receive success confirmation or error message.
     * @param bgThread The Executor instance which will be used to run the callbacks in that executors thread.
     */
    public void deleteUser(String UserId, OperationCallback callback, Executor bgThread) {
        db.collection(COLLECTION_NAME)
                .document(UserId)
                .delete()
                .addOnSuccessListener(bgThread, aVoid -> {
                    Log.d("UserRepository", "User deleted successfully: " + UserId);
                    callback.onSuccess();
                })
                .addOnFailureListener(bgThread, e -> {
                    Log.e("UserRepository", "Error deleting User: " + UserId, e);
                    callback.onError("Failed to delete User: " + e.getMessage());
                });
    }
}

