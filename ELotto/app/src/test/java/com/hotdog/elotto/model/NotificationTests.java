package com.hotdog.elotto.model;

import static org.junit.jupiter.api.Assertions.*;

import com.google.firebase.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;


public class NotificationTests {

    private Notification notification;

    @BeforeEach
    void setup() {
        notification = new Notification("Test Title", "Test Message", "event123");
    }


    @Test
    void testConstructorSetsBasicFields() {
        assertNotNull(notification.getUuid());
        assertNotNull(notification.getTimestamp());
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Message", notification.getMessage());
        assertEquals("event123", notification.getEventId());
        assertFalse(notification.isRead()); // Should default to false
    }

    @Test
    void testConstructorGeneratesUniqueUuid() {
        Notification notification1 = new Notification("Title1", "Message1", "event1");
        Notification notification2 = new Notification("Title2", "Message2", "event2");

        assertNotEquals(notification1.getUuid(), notification2.getUuid());
    }

    @Test
    void testConstructorSetsTimestampToNow() {
        Date before = new Date();
        Notification newNotification = new Notification("Title", "Message", "event1");
        Date after = new Date();

        Date notificationDate = newNotification.getTimestamp().toDate();
        assertTrue(notificationDate.after(before) || notificationDate.equals(before));
        assertTrue(notificationDate.before(after) || notificationDate.equals(after));
    }

    @Test
    void testDefaultConstructor() {
        Notification emptyNotification = new Notification();
        assertNotNull(emptyNotification);
        assertNull(emptyNotification.getUuid());
        assertNull(emptyNotification.getTitle());
    }



    @Test
    void testSetAndGetUuid() {
        notification.setUuid("custom-uuid-123");
        assertEquals("custom-uuid-123", notification.getUuid());
    }

    @Test
    void testSetAndGetTitle() {
        notification.setTitle("New Title");
        assertEquals("New Title", notification.getTitle());
    }

    @Test
    void testSetAndGetMessage() {
        notification.setMessage("New Message");
        assertEquals("New Message", notification.getMessage());
    }

    @Test
    void testSetAndGetEventId() {
        notification.setEventId("event456");
        assertEquals("event456", notification.getEventId());
    }

    @Test
    void testSetAndGetEventTitle() {
        notification.setEventTitle("Swimming Lessons");
        assertEquals("Swimming Lessons", notification.getEventTitle());
    }

    @Test
    void testSetAndGetEventImageUrl() {
        notification.setEventImageUrl("https://example.com/image.png");
        assertEquals("https://example.com/image.png", notification.getEventImageUrl());
    }

    @Test
    void testSetAndGetUserId() {
        notification.setUserId("user123");
        assertEquals("user123", notification.getUserId());
    }

    @Test
    void testSetAndGetTimestamp() {
        Timestamp customTimestamp = new Timestamp(new Date(1000000000000L));
        notification.setTimestamp(customTimestamp);
        assertEquals(customTimestamp, notification.getTimestamp());
    }

    @Test
    void testIsReadDefaultsFalse() {
        assertFalse(notification.isRead());
    }

    @Test
    void testSetAndGetReadStatus() {
        notification.setRead(true);
        assertTrue(notification.isRead());

        notification.setRead(false);
        assertFalse(notification.isRead());
    }


    @Test
    void testGetFormattedTimestampWithValidTimestamp() {
        // im setting a specific date like Nov 25, 2023 at 2:30 PM
        Date specificDate = new Date(1700928600000L);
        notification.setTimestamp(new Timestamp(specificDate));

        String formatted = notification.getFormattedTimestamp();
        assertNotNull(formatted);
        assertTrue(formatted.contains("Nov"));
        assertTrue(formatted.contains("25"));
        assertTrue(formatted.contains("at"));
    }

    @Test
    void testGetFormattedTimestampWithNullTimestamp() {
        notification.setTimestamp(null);
        assertEquals("Unknown time", notification.getFormattedTimestamp());
    }


    @Test
    void testGetShortUserIdWithShortId() {
        notification.setUserId("short");
        assertEquals("short", notification.getShortUserId());
    }

    @Test
    void testGetShortUserIdWith8CharacterId() {
        notification.setUserId("exactly8");
        assertEquals("exactly8", notification.getShortUserId());
    }

    @Test
    void testGetShortUserIdWith9CharacterId() {
        notification.setUserId("exactly89");
        assertEquals("exactly8...", notification.getShortUserId());
    }

    @Test
    void testGetShortUserIdWithNullUserId() {
        notification.setUserId(null);
        assertNull(notification.getShortUserId());
    }


    @Test
    void testNotificationWithNullTitle() {
        Notification nullTitleNotification = new Notification(null, "Message", "event1");
        assertNull(nullTitleNotification.getTitle());
    }

    @Test
    void testNotificationWithEmptyMessage() {
        Notification emptyMessageNotification = new Notification("Title", "", "event1");
        assertEquals("", emptyMessageNotification.getMessage());
    }

    @Test
    void testNotificationWithNullEventId() {
        Notification nullEventNotification = new Notification("Title", "Message", null);
        assertNull(nullEventNotification.getEventId());
    }

    @Test
    void testSetNullEventTitle() {
        notification.setEventTitle(null);
        assertNull(notification.getEventTitle());
    }

    @Test
    void testSetEmptyEventImageUrl() {
        notification.setEventImageUrl("");
        assertEquals("", notification.getEventImageUrl());
    }

    @Test
    void testMultipleReadStatusChanges() {
        assertFalse(notification.isRead());

        notification.setRead(true);
        assertTrue(notification.isRead());

        notification.setRead(false);
        assertFalse(notification.isRead());

        notification.setRead(true);
        assertTrue(notification.isRead());
    }

    @Test
    void testUuidImmutableAfterConstruction() {
        String originalUuid = notification.getUuid();

        Notification anotherNotification = new Notification("Title2", "Message2", "event2");

        assertEquals(originalUuid, notification.getUuid());
    }
}
