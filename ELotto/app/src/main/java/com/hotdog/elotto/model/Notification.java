package com.hotdog.elotto.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a notification sent to a user.
 * Stored within a list in the user's notification document.
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
    private String userId;

    public Notification() {
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFormattedTimestamp() {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault());
            return sdf.format(date);
        }
        return "Unknown time";
    }

    public String getShortUserId() {
        if (userId != null && userId.length() > 8) {
            return userId.substring(0, 8) + "...";
        }
        return userId;
    }
}
