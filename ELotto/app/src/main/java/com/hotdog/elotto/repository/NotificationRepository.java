package com.hotdog.elotto.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class responsible for managing Notification data access with
 * Firebase Firestore.
 * Stores notifications in a "notifications" collection where each document ID
 * is the User ID.
 * The document contains an array of Notification objects.
 *
 * @author Layne Pitman
 * @version 1.0
 * @since 2025-11-26
 */
public class NotificationRepository {
    private static final String COLLECTION_NAME = "notifications";
    private static final String FIELD_NOTIFICATIONS = "notifications";
    private static final String TAG = "NotificationRepository";
    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId   The ID of the user.
     * @param callback Callback to receive the list of notifications.
     */
    public void getNotifications(String userId, FirestoreListCallback<Notification> callback) {
        db.collection(COLLECTION_NAME).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> rawList = (List<Map<String, Object>>) documentSnapshot
                                .get(FIELD_NOTIFICATIONS);
                        List<Notification> notifications = new ArrayList<>();

                        if (rawList != null) {
                            for (Map<String, Object> map : rawList) {
                                try {
                                    // Manually map fields in the sub collection
                                    Notification n = new Notification();
                                    n.setUuid((String) map.get("uuid"));
                                    n.setTitle((String) map.get("title"));
                                    n.setMessage((String) map.get("message"));
                                    n.setEventId((String) map.get("eventId"));
                                    n.setRead(Boolean.TRUE.equals(map.get("read")));
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing notification", e);
                                }
                            }
                        }

                        NotificationContainer container = documentSnapshot.toObject(NotificationContainer.class);
                        if (container != null && container.getNotifications() != null) {
                            List<Notification> list = container.getNotifications();
                            Collections.sort(list, (n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
                            callback.onSuccess(list);
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }

                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching notifications", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Adds a notification to the user's list.
     * Creates the document if it doesn't exist.
     *
     * @param userId       The ID of the user.
     * @param notification The notification to add.
     * @param callback     Callback for success/error.
     */
    public void addNotification(String userId, Notification notification, OperationCallback callback) {
        Map<String, Object> data = new HashMap<>();

        data.put(FIELD_NOTIFICATIONS, FieldValue.arrayUnion(notification));

        db.collection(COLLECTION_NAME).document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification added for user: " + userId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding notification", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Marks a notification as read.
     * Since we can't update a specific array element easily, we read, modify, and
     * write back.
     *
     * @param userId         The ID of the user.
     * @param notificationId The UUID of the notification.
     * @param callback       Callback for success/error.
     */
    public void markAsRead(String userId, String notificationId, OperationCallback callback) {
        db.collection(COLLECTION_NAME).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NotificationContainer container = documentSnapshot.toObject(NotificationContainer.class);
                        if (container != null && container.getNotifications() != null) {
                            List<Notification> list = container.getNotifications();
                            boolean found = false;
                            for (Notification n : list) {
                                if (n.getUuid().equals(notificationId)) {
                                    n.setRead(true);
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                // Write back the modified list
                                db.collection(COLLECTION_NAME).document(userId)
                                        .set(container)
                                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            } else {
                                callback.onError("Notification not found");
                            }
                        } else {
                            callback.onError("No notifications found");
                        }
                    } else {
                        callback.onError("User notification document not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Deletes a notification.
     *
     * @param userId         The ID of the user.
     * @param notificationId The UUID of the notification.
     * @param callback       Callback for success/error.
     */
    public void deleteNotification(String userId, String notificationId, OperationCallback callback) {
        db.collection(COLLECTION_NAME).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NotificationContainer container = documentSnapshot.toObject(NotificationContainer.class);
                        if (container != null && container.getNotifications() != null) {
                            List<Notification> list = container.getNotifications();
                            boolean removed = list.removeIf(n -> n.getUuid().equals(notificationId));

                            if (removed) {
                                // Write back the modified list
                                db.collection(COLLECTION_NAME).document(userId)
                                        .set(container)
                                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            } else {
                                callback.onError("Notification not found");
                            }
                        } else {
                            callback.onError("No notifications found");
                        }
                    } else {
                        callback.onError("User notification document not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Inner class to map the Firestore document structure.
     */
    public static class NotificationContainer {
        private List<Notification> notifications;

        public NotificationContainer() {
            // Default constructor
        }

        public NotificationContainer(List<Notification> notifications) {
            this.notifications = notifications;
        }

        public List<Notification> getNotifications() {
            return notifications;
        }

        public void setNotifications(List<Notification> notifications) {
            this.notifications = notifications;
        }
    }
}
