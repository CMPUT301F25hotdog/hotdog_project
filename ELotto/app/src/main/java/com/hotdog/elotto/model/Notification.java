package com.hotdog.elotto.model;

import com.google.firebase.Timestamp;
import java.util.UUID;

/**
 * Represents a notification sent to a user.
 * Stored within a list in the user's notification document.
 *
 * @author Layne Pitman
 * @version 1.0
 * @since 2025-11-26
 */
public class Notification {
    private String uuid;
    private Timestamp timestamp;
    private String title;
    private String message;
    private boolean isRead;
    private String eventId;
    private String eventTitle;
    private String eventImageUrl;

    /**
     * Default constructor required for Firestore serialization.
     */
    public Notification() {
    }

    /**
     * Creates a new Notification.
     *
     * @param title   The title of the notification.
     * @param message The body message of the notification.
     * @param eventId The ID of the event associated with this notification
     *                (optional).
     */
    public Notification(String title, String message, String eventId) {
        this.uuid = UUID.randomUUID().toString();
        this.timestamp = Timestamp.now();
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.eventId = eventId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventImageUrl() {
        return eventImageUrl;
    }

    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }
}
