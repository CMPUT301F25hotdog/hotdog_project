package com.hotdog.elotto;

import static org.junit.jupiter.api.Assertions.*;

import com.hotdog.elotto.model.Organizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class OrganizerModelUnitTest {
    private Organizer organizer;

    @BeforeEach
    void setUp() {
        organizer = new Organizer("user12345");
    }
    @Test
    void testOrgID(){
        assertEquals("user12345",organizer.getOrgID());
    }
    @Test
    void testSetOrgID(){
        organizer.setOrgID("new");
        assertEquals("new",organizer.getOrgID());
    }
    @Test
    void testGetCreatedEvents(){
        assertNotNull(organizer.getCreatedEvents());
        assertTrue(organizer.getCreatedEvents().isEmpty());
    }
    @Test
    void testAddCreatedEvent(){
        organizer.addCreatedEvent("event1");
        organizer.addCreatedEvent("event2");
        ArrayList<String> createdEvents = organizer.getCreatedEvents();

        assertEquals(2,createdEvents.size());
        assertTrue(createdEvents.contains("event1"));
        assertTrue(createdEvents.contains("event2"));
        assertFalse(createdEvents.contains("event3"));
    }
    @Test
    void testRemoveCreatedEvent(){
        organizer.addCreatedEvent("event1");
        organizer.addCreatedEvent("event2");
        organizer.removeCreatedEvent("event1");
        ArrayList<String> createdEvents = organizer.getCreatedEvents();

        assertEquals(1,createdEvents.size());
        assertFalse(createdEvents.contains("event1"));
        assertTrue(createdEvents.contains("event2"));
    }

}
