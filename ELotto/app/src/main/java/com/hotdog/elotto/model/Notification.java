package com.hotdog.elotto.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a single notification entity within the application.
 * <p>
 * This class acts as a data transfer object for alerts sent to users. It holds
 * everything from the message content to the timestamp, essentially serving as
 * the digital tap on the shoulder that users will likely swipe away immediately.
 * </p>
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

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Notification.class).
     * <p>
     * Do not delete this, or Firebase will silently fail and I will watch you from inside your walls.
     * </p>
     */
    public Notification() {
    }

    /**
     * Constructs a new Notification with the essential details.
     * Automatically generates a unique ID and sets the timestamp to "now",
     * because we assume you aren't sending notifications from the future.
     *
     * @param title   The headline of the notification.
     * @param message The body text explaining why we are bothering the user.
     * @param eventId The ID of the event associated with this notification.
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
     * Gets the unique identifier for this notification.
     *
     * @return The UUID string.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier. usually handled by the constructor,
     * but exposed here in case we need to manually override reality.
     *
     * @param uuid The new UUID.
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the time this notification was created.
     *
     * @return The Firebase Timestamp object.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp. useful if you need to backdate a notification
     * to make it look like you sent it on time.
     *
     * @param timestamp The new timestamp.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the notification title.
     *
     * @return The title string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the notification title.
     *
     * @param title The new title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the detailed message body.
     *
     * @return The message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message body.
     *
     * @param message The new message content.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Checks if the user has seen this notification.
     *
     * @return true if read, false if they are ignoring us.
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Updates the read status of the notification.
     *
     * @param read The new read status.
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Gets the ID of the event.
     *
     * @return The event ID string.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the related event ID.
     *
     * @param eventId The new event ID.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the title of the related event.
     *
     * @return The event title.
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Sets the event title.
     *
     * @param eventTitle The title of the event.
     */
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /**
     * Gets the URL for the event image.
     *
     * @return The image URL string.
     */
    public String getEventImageUrl() {
        return eventImageUrl;
    }

    /**
     * Sets the event image URL.
     *
     * @param eventImageUrl The URL to the image resource.
     */
    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }

    /**
     * Gets the ID of the user receiving this notification.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the target user ID.
     *
     * @param userId The ID of the user to annoy.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Helper method to convert the machine timestamp into human time minutes.
     * <p>
     * Because apparently "1698234823" isn't a very user-friendly date format.
     * </p>
     *
     * @return A formatted date string like "Nov 25 at 2:30 PM" or "Unknown time" if null.
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
     * Helper to get a truncated version of the User ID.
     * <p>
     * For tiny buttcracks of space and debugging
     * </p>
     *
     * @return The first 8 characters of the ID followed by dots, or the full ID if it's short.
     */
    public String getShortUserId() {
        if (userId != null && userId.length() > 8) {
            return userId.substring(0, 8) + "...";
        }
        return userId;
    }
}