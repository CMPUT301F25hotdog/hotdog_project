package com.hotdog.elotto.controller;

import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.model.Notification;
import com.hotdog.elotto.repository.NotificationRepository;

/**
 * Controller class responsible for managing notification logic.
 * Bridges the UI and the Repository.
 *
 * @author Layne Pitman
 * @version 1.0
 * @since 2025-11-26
 */
public class NotificationController {
    private final NotificationRepository repository;

    public NotificationController() {
        this.repository = new NotificationRepository();
    }

    /**
     * Loads all notifications for a specific user.
     *
     * @param userId   The ID of the user.
     * @param callback Callback to receive the list of notifications.
     */
    public void loadNotifications(String userId, FirestoreListCallback<Notification> callback) {
        repository.getNotifications(userId, callback);
    }

    /**
     * Sends a notification to a user.
     *
     * @param userId        The ID of the user to notify.
     * @param title         The title of the notification.
     * @param message       The message content.
     * @param eventId       The ID of the related event (optional).
     * @param eventTitle    The title of the related event (optional).
     * @param eventImageUrl The image URL of the related event (optional).
     * @param callback      Callback for success/error.
     */
    public void sendNotification(String userId, String title, String message, String eventId, String eventTitle,
            String eventImageUrl, OperationCallback callback) {
        Notification notification = new Notification(title, message, eventId);
        notification.setEventTitle(eventTitle);
        notification.setEventImageUrl(eventImageUrl);
        repository.addNotification(userId, notification, callback);
    }

    /**
     * Marks a notification as read.
     *
     * @param userId         The ID of the user.
     * @param notificationId The UUID of the notification.
     * @param callback       Callback for success/error.
     */
    public void markAsRead(String userId, String notificationId, OperationCallback callback) {
        repository.markAsRead(userId, notificationId, callback);
    }

    /**
     * Deletes a notification.
     *
     * @param userId         The ID of the user.
     * @param notificationId The UUID of the notification.
     * @param callback       Callback for success/error.
     */
    public void deleteNotification(String userId, String notificationId, OperationCallback callback) {
        repository.deleteNotification(userId, notificationId, callback);
    }
}
