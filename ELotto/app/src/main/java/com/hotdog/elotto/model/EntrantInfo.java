package com.hotdog.elotto.model;

import java.util.Date;

/**
 * Helper class that combines User information with their event registration date.
 * Used by the organizer to view entrant details including when they joined an event.
 *
 * <p>This class is not stored in Firebase - it's created on-the-fly by combining
 * data from the User document and the Event document.</p>
 *
 * <p>Model layer helper class.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-24
 */
public class EntrantInfo {
    private User user;
    private Date joinedDate;

    /**
     * Default constructor required for potential future Firebase serialization.
     */
    public EntrantInfo() {
    }

    /**
     * Constructs an EntrantInfo with user and their join date.
     *
     * @param user the User object containing entrant information
     * @param joinedDate the date when this user joined the event's waiting list
     */
    public EntrantInfo(User user, Date joinedDate) {
        this.user = user;
        this.joinedDate = joinedDate;
    }

    /**
     * Gets the User object.
     *
     * @return the User
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the User object.
     *
     * @param user the User to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the date when the user joined this event.
     *
     * @return the joined date
     */
    public Date getJoinedDate() {
        return joinedDate;
    }

    /**
     * Sets the joined date.
     *
     * @param joinedDate the date to set
     */
    public void setJoinedDate(Date joinedDate) {
        this.joinedDate = joinedDate;
    }

    /**
     * Gets the user's name from the User object.
     * Convenience method to avoid null checks in UI code.
     *
     * @return the user's name, or "Unknown" if user is null
     */
    public String getName() {
        return user != null ? user.getName() : "Unknown";
    }

    /**
     * Gets the user's email from the User object.
     * Convenience method to avoid null checks in UI code.
     *
     * @return the user's email, or empty string if user is null
     */
    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }

    /**
     * Gets the user's ID from the User object.
     * Convenience method to avoid null checks in UI code.
     *
     * @return the user's device ID, or empty string if user is null
     */
    public String getUserId() {
        return user != null ? user.getId() : "";
    }
}