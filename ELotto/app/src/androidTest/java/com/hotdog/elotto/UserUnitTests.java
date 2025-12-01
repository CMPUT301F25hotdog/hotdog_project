package com.hotdog.elotto;

import static org.junit.jupiter.api.Assertions.*;

import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class UserUnitTests {

    private User.RegisteredEvent registeredEvent;

    @BeforeEach
    void setup() {
        registeredEvent = new User.RegisteredEvent("event123");
    }


    @Test
    void testRegisteredEventCreation() {
        assertNotNull(registeredEvent);
        assertEquals("event123", registeredEvent.getEventId());
        assertEquals(Status.Pending, registeredEvent.getStatus());
        assertNotNull(registeredEvent.getRegisteredDate());
    }

    @Test
    void testRegisteredEventSetEventId() {
        registeredEvent.setEventId("newEvent456");
        assertEquals("newEvent456", registeredEvent.getEventId());
    }

    @Test
    void testRegisteredEventSetStatus() {
        registeredEvent.setStatus(Status.Invited);
        assertEquals(Status.Invited, registeredEvent.getStatus());

        registeredEvent.setStatus(Status.Accepted);
        assertEquals(Status.Accepted, registeredEvent.getStatus());

        registeredEvent.setStatus(Status.Declined);
        assertEquals(Status.Declined, registeredEvent.getStatus());
    }


    @Test
    void testRegisteredEventDefaultConstructor() {
        User.RegisteredEvent defaultEvent = new User.RegisteredEvent();

        assertNotNull(defaultEvent);
        assertEquals("", defaultEvent.getEventId());
        assertEquals(Status.Pending, defaultEvent.getStatus());
        assertNotNull(defaultEvent.getRegisteredDate());
    }


    @Test
    void testSharedStringCreation() {
        User user = new User();
        User.SharedString sharedString = user.new SharedString("Hello");

        assertEquals("Hello", sharedString.get());
    }

    @Test
    void testSharedStringSet() {
        User user = new User();
        User.SharedString sharedString = user.new SharedString("Initial");

        sharedString.set("Updated");
        assertEquals("Updated", sharedString.get());
    }

    @Test
    void testSharedStringMultipleUpdates() {
        User user = new User();
        User.SharedString sharedString = user.new SharedString("First");

        sharedString.set("Second");
        assertEquals("Second", sharedString.get());

        sharedString.set("Third");
        assertEquals("Third", sharedString.get());
    }


    @Test
    void testAllStatusValues() {
        assertEquals(Status.Pending, Status.valueOf("Pending"));
        assertEquals(Status.Invited, Status.valueOf("Invited"));
        assertEquals(Status.Waitlisted, Status.valueOf("Waitlisted"));
        assertEquals(Status.Accepted, Status.valueOf("Accepted"));
        assertEquals(Status.Declined, Status.valueOf("Declined"));
        assertEquals(Status.Withdrawn, Status.valueOf("Withdrawn"));
    }

    @Test
    void testStatusEnumCount() {
        Status[] statuses = Status.values();
        assertEquals(6, statuses.length);
    }


    @Test
    void testRegisteredEventWithNullEventId() {
        User.RegisteredEvent event = new User.RegisteredEvent();
        event.setEventId(null);
        assertNull(event.getEventId());
    }

    @Test
    void testRegisteredEventWithEmptyEventId() {
        User.RegisteredEvent event = new User.RegisteredEvent("");
        assertEquals("", event.getEventId());
    }

    @Test
    void testSharedStringWithEmptyString() {
        User user = new User();
        User.SharedString sharedString = user.new SharedString("");

        assertEquals("", sharedString.get());

        sharedString.set("Not empty");
        assertEquals("Not empty", sharedString.get());
    }

    @Test
    void testSharedStringWithNull() {
        User user = new User();
        User.SharedString sharedString = user.new SharedString(null);

        assertNull(sharedString.get());
    }
}