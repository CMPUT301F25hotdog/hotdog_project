package com.hotdog.elotto.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Model class representing a notification sent to a user.
 *
 * <p>Notifications are associated with events and stored in the user's notification
 * collection in Firestore. Each notification contains a title, message, timestamp,
 * read/unread status, and references to the associated event and user.</p>
 *
 * <p>Model layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Bhuvnesh Batta
 * @version 1.0
 * @since 2025-11-26
 */
public class Notification {
    /**
     * Unique identifier for the notification, generated using UUID.
     */
    private String uuid;

    /**
     * Timestamp when the notification was created.
     */
    private Timestamp timestamp;

    /**
     * The title/subject of the notification.
     */
    private String title;

    /**
     * The detailed message content of the notification.
     */
    private String message;

    /**
     * Flag indicating whether the notification has been read by the user.
     */
    private boolean isRead;

    /**
     * The ID of the event associated with this notification.
     */
    private String eventId;

    /**
     * The title of the event associated with this notification.
     */
    private String eventTitle;

    /**
     * The URL or Base64 string of the event's poster image.
     */
    private String eventImageUrl;

    /**
     * The ID of the user who received this notification.
     */
    private String userId;

    /**
     * Default constructor required for Firestore serialization.
     */
    public Notification() {
    }

    /**
     * Constructs a new Notification with the specified title, message, and event ID.
     *
     * <p>Automatically generates a UUID, sets the current timestamp, and marks
     * the notification as unread.</p>
     *
     * @param title the notification title
     * @param message the notification message content
     * @param eventId the ID of the associated event
     */
    public Notification(String title, String message, String eventId) {
        this.uuid = UUID.randomUUID().toString();
        this.timestamp = Timestamp.now();
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.eventId = eventId;
    }

    /**
     * Gets the unique identifier of the notification.
     *
     * @return the UUID string
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier of the notification.
     *
     * @param uuid the UUID string to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the timestamp when the notification was created.
     *
     * @return the Firestore Timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the notification was created.
     *
     * @param timestamp the Firestore Timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the notification title.
     *
     * @return the title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the notification title.
     *
     * @param title the title string to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the notification message content.
     *
     * @return the message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message content.
     *
     * @param message the message string to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Checks if the notification has been read.
     *
     * @return true if read, false if unread
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Sets the read status of the notification.
     *
     * @param read true to mark as read, false to mark as unread
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Gets the ID of the associated event.
     *
     * @return the event ID string
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the ID of the associated event.
     *
     * @param eventId the event ID string to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the title of the associated event.
     *
     * @return the event title string
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Sets the title of the associated event.
     *
     * @param eventTitle the event title string to set
     */
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /**
     * Gets the URL or Base64 string of the event's poster image.
     *
     * @return the event image URL string
     */
    public String getEventImageUrl() {
        return eventImageUrl;
    }

    /**
     * Sets the URL or Base64 string of the event's poster image.
     *
     * @param eventImageUrl the event image URL string to set
     */
    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }

    /**
     * Gets the ID of the user who received this notification.
     *
     * @return the user ID string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who received this notification.
     *
     * @param userId the user ID string to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets a formatted timestamp string for display purposes.
     *
     * <p>Returns the timestamp in the format "MMM dd 'at' h:mm a" (e.g., "Nov 26 at 3:45 PM").
     * If the timestamp is null, returns "Unknown time".</p>
     *
     * @return formatted timestamp string
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
     * Gets a shortened version of the user ID for display purposes.
     *
     * <p>If the user ID is longer than 8 characters, returns the first 8 characters
     * followed by "...". Otherwise, returns the full user ID.</p>
     *
     * @return shortened user ID string
     */
    public String getShortUserId() {
        if (userId != null && userId.length() > 8) {
            return userId.substring(0, 8) + "...";
        }
        return userId;
    }
}