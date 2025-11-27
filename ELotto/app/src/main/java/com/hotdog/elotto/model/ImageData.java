package com.hotdog.elotto.model;

/**
 * Model class representing an image uploaded to the system.
 * @author Your Name
 * @version 1.0.0
 */
public class ImageData {
    private String id;
    private String url;
    private String eventName;
    private String ownerName;
    private String ownerId;
    private String type; // "Event" or "Profile"
    private long uploadTimestamp;

    /**
     * Default constructor required for Firebase
     */
    public ImageData() {
    }

    /**
     * Constructor with all fields
     * @param id Image ID
     * @param url Image URL in Firebase Storage
     * @param eventName Associated event name (if event image)
     * @param ownerName Owner's name
     * @param ownerId Owner's user ID
     * @param type Image type (Event or Profile)
     * @param uploadTimestamp Upload timestamp
     */
    public ImageData(String id, String url, String eventName, String ownerName,
                     String ownerId, String type, long uploadTimestamp) {
        this.id = id;
        this.url = url;
        this.eventName = eventName;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.type = type;
        this.uploadTimestamp = uploadTimestamp;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEventName() {
        return eventName != null ? eventName : "[No Event]";
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }
}