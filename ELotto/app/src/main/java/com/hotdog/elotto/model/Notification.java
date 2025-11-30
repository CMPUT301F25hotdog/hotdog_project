package com.hotdog.elotto.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Notification model for admin notification logs
 */
public class Notification {
    private String uuid; // Unique ID for the notification within the array
    private String eventId;
    private String message;
    private String title;
    private boolean read;
    private Timestamp timestamp;
    private String userId; // Helper field to store which user this belongs to

    // Empty constructor for Firestore
    public Notification() {
    }

    public Notification(String uuid, String eventId, String message, String title, boolean read, Timestamp timestamp,
            String userId) {
        this.uuid = uuid;
        this.eventId = eventId;
        this.message = message;
        this.title = title;
        this.read = read;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // Alias for getUuid to maintain compatibility with existing code that uses
    // getId()
    public String getId() {
        return uuid;
    }

    public void setId(String id) {
        this.uuid = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get formatted timestamp string
     */
    public String getFormattedTimestamp() {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault());
            return sdf.format(date);
        }
        return "Unknown time";
    }

    /**
     * Get short user ID for display
     */
    public String getShortUserId() {
        if (userId != null && userId.length() > 8) {
            return userId.substring(0, 8) + "...";
        }
        return userId;
    }
}