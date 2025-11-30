package com.hotdog.elotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Notification model for admin notification logs
 */
public class Notification {
    @DocumentId
    private String id;
    private String eventId;
    private String message;
    private boolean read;
    private Timestamp timestamp;
    private String userId;

    // Empty constructor for Firestore
    public Notification() {
    }

    public Notification(String eventId, String message, boolean read, Timestamp timestamp, String userId) {
        this.eventId = eventId;
        this.message = message;
        this.read = read;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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