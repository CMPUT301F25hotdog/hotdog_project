package com.hotdog.elotto.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class EventTest {

    private Event event;
    private Date futureDate;
    private Date pastDate;
    private Date currentDate;

    @BeforeEach
    public void setUp() {

        Calendar calendar = Calendar.getInstance();


        currentDate = calendar.getTime();


        calendar.add(Calendar.DAY_OF_MONTH, 7);
        futureDate = calendar.getTime();


        calendar.add(Calendar.DAY_OF_MONTH, -14);
        pastDate = calendar.getTime();


        event = new Event(
                "Test Event",
                "Test Description",
                "Test Location",
                futureDate,
                pastDate,
                futureDate,
                50,
                "fakeOrganizer"
        );
    }



    @Test
    public void testConstructor_CreatesEventWithCorrectValues() {
        assertEquals("Test Event", event.getName());
        assertEquals("Test Description", event.getDescription());
        assertEquals("Test Location", event.getLocation());
        assertEquals(50, event.getMaxEntrants());
        assertEquals("fakeOrganizer", event.getOrganizerId());
        assertEquals("OPEN", event.getStatus());
        assertFalse(event.isGeolocationRequired());
        assertEquals(0.0, event.getPrice(), 0.001);
    }

    @Test
    public void testDefaultConstructor_CreatesEmptyEvent() {
        Event emptyEvent = new Event();
        assertNotNull(emptyEvent);
        assertNull(emptyEvent.getName());
        assertNull(emptyEvent.getDescription());
    }



    @Test
    public void testSetAndGetId() {
        event.setId("fakeEvent");
        assertEquals("fakeEvent", event.getId());
    }

    @Test
    public void testSetAndGetName() {
        event.setName("New Event Name");
        assertEquals("New Event Name", event.getName());
    }

    @Test
    public void testSetAndGetDescription() {
        event.setDescription("New Description");
        assertEquals("New Description", event.getDescription());
    }

    @Test
    public void testSetAndGetLocation() {
        event.setLocation("New Location");
        assertEquals("New Location", event.getLocation());
    }

    @Test
    public void testSetAndGetPrice() {
        event.setPrice(25.50);
        assertEquals(25.50, event.getPrice(), 0.001);
    }

    @Test
    public void testSetAndGetGeolocationRequired() {
        event.setGeolocationRequired(true);
        assertTrue(event.isGeolocationRequired());

        event.setGeolocationRequired(false);
        assertFalse(event.isGeolocationRequired());
    }

    @Test
    public void testSetAndGetWaitlistLimit() {
        event.setWaitlistLimit(100);
        assertEquals(Integer.valueOf(100), event.getWaitlistLimit());
    }

    @Test
    public void testSetAndGetPosterImageUrl() {
        event.setPosterImageUrl("https://example.com/poster.jpg");
        assertEquals("https://example.com/poster.jpg", event.getPosterImageUrl());
    }

    @Test
    public void testSetAndGetQrCodeData() {
        event.setQrCodeData("QR123456");
        assertEquals("QR123456", event.getQrCodeData());
    }



    @Test
    public void testIsRegistrationOpen_WhenCurrentTimeIsWithinPeriod() {
        Calendar cal = Calendar.getInstance();


        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date startDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        event.setRegistrationStartDate(startDate);
        event.setRegistrationEndDate(endDate);

        assertTrue(event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WhenBeforeStartDate() {
        Calendar cal = Calendar.getInstance();


        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        Date endDate = cal.getTime();

        event.setRegistrationStartDate(startDate);
        event.setRegistrationEndDate(endDate);

        assertFalse(event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WhenAfterEndDate() {
        Calendar cal = Calendar.getInstance();


        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date startDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        Date endDate = cal.getTime();

        event.setRegistrationStartDate(startDate);
        event.setRegistrationEndDate(endDate);

        assertFalse(event.isRegistrationOpen());
    }



    @Test
    public void testGetCurrentWaitlistCount_WhenWaitlistIsNull() {
        event.setWaitlistEntrantIds(null);
        assertEquals(0, event.getCurrentWaitlistCount());
    }

    @Test
    public void testGetCurrentWaitlistCount_WhenWaitlistIsEmpty() {
        event.setWaitlistEntrantIds(new ArrayList<>());
        assertEquals(0, event.getCurrentWaitlistCount());
    }

    @Test
    public void testGetCurrentWaitlistCount_WithEntrants() {
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        waitlist.add("user2");
        waitlist.add("user3");
        event.setWaitlistEntrantIds(waitlist);

        assertEquals(3, event.getCurrentWaitlistCount());
    }

    @Test
    public void testIsFull_WhenNoLimitSet() {
        event.setWaitlistLimit(null);
        List<String> waitlist = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            waitlist.add("user" + i);
        }
        event.setWaitlistEntrantIds(waitlist);

        assertFalse(event.isFull());
    }

    @Test
    public void testIsFull_WhenUnderLimit() {
        event.setWaitlistLimit(10);
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        waitlist.add("user2");
        event.setWaitlistEntrantIds(waitlist);

        assertFalse(event.isFull());
    }

    @Test
    public void testIsFull_WhenAtLimit() {
        event.setWaitlistLimit(3);
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        waitlist.add("user2");
        waitlist.add("user3");
        event.setWaitlistEntrantIds(waitlist);

        assertTrue(event.isFull());
    }

    @Test
    public void testIsFull_WhenOverLimit() {
        event.setWaitlistLimit(2);
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        waitlist.add("user2");
        waitlist.add("user3");
        event.setWaitlistEntrantIds(waitlist);

        assertTrue(event.isFull());
    }

    @Test
    public void testIsFull_WhenWaitlistIsNull() {
        event.setWaitlistLimit(10);
        event.setWaitlistEntrantIds(null);

        assertFalse(event.isFull());
    }

    // Accepted Entrants Tests

    @Test
    public void testGetCurrentAcceptedCount_WhenAcceptedListIsNull() {
        event.setAcceptedEntrantIds(null);
        assertEquals(0, event.getCurrentAcceptedCount());
    }

    @Test
    public void testGetCurrentAcceptedCount_WhenAcceptedListIsEmpty() {
        event.setAcceptedEntrantIds(new ArrayList<>());
        assertEquals(0, event.getCurrentAcceptedCount());
    }

    @Test
    public void testGetCurrentAcceptedCount_WithAcceptedEntrants() {
        List<String> accepted = new ArrayList<>();
        accepted.add("user1");
        accepted.add("user2");
        event.setAcceptedEntrantIds(accepted);

        assertEquals(2, event.getCurrentAcceptedCount());
    }



    @Test
    public void testGetSpotsRemaining_WhenNoAcceptedEntrants() {
        event.setMaxEntrants(50);
        event.setAcceptedEntrantIds(new ArrayList<>());

        assertEquals(50, event.getSpotsRemaining());
    }

    @Test
    public void testGetSpotsRemaining_WithSomeAcceptedEntrants() {
        event.setMaxEntrants(50);
        List<String> accepted = new ArrayList<>();
        accepted.add("user1");
        accepted.add("user2");
        accepted.add("user3");
        event.setAcceptedEntrantIds(accepted);

        assertEquals(47, event.getSpotsRemaining());
    }

    @Test
    public void testGetSpotsRemaining_WhenFull() {
        event.setMaxEntrants(3);
        List<String> accepted = new ArrayList<>();
        accepted.add("user1");
        accepted.add("user2");
        accepted.add("user3");
        event.setAcceptedEntrantIds(accepted);

        assertEquals(0, event.getSpotsRemaining());
    }

    @Test
    public void testGetSpotsRemaining_WhenOverCapacity() {
        event.setMaxEntrants(2);
        List<String> accepted = new ArrayList<>();
        accepted.add("user1");
        accepted.add("user2");
        accepted.add("user3");
        event.setAcceptedEntrantIds(accepted);

        assertEquals(-1, event.getSpotsRemaining());
    }



    @Test
    public void testSetAndGetWaitlistEntrantIds() {
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        waitlist.add("user2");
        event.setWaitlistEntrantIds(waitlist);

        assertEquals(waitlist, event.getWaitlistEntrantIds());
        assertEquals(2, event.getWaitlistEntrantIds().size());
    }

    @Test
    public void testSetAndGetSelectedEntrantIds() {
        List<String> selected = new ArrayList<>();
        selected.add("user1");
        event.setSelectedEntrantIds(selected);

        assertEquals(selected, event.getSelectedEntrantIds());
    }

    @Test
    public void testSetAndGetCancelledEntrantIds() {
        List<String> cancelled = new ArrayList<>();
        cancelled.add("user1");
        event.setCancelledEntrantIds(cancelled);

        assertEquals(cancelled, event.getCancelledEntrantIds());
    }



    @Test
    public void testSetAndGetStatus() {
        event.setStatus("CLOSED");
        assertEquals("CLOSED", event.getStatus());

        event.setStatus("FULL");
        assertEquals("FULL", event.getStatus());

        event.setStatus("COMPLETED");
        assertEquals("COMPLETED", event.getStatus());
    }



    @Test
    public void testSetAndGetEventDateTime() {
        Date newDate = new Date();
        event.setEventDateTime(newDate);
        assertEquals(newDate, event.getEventDateTime());
    }

    @Test
    public void testSetAndGetRegistrationDates() {
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000); // +1 day

        event.setRegistrationStartDate(startDate);
        event.setRegistrationEndDate(endDate);

        assertEquals(startDate, event.getRegistrationStartDate());
        assertEquals(endDate, event.getRegistrationEndDate());
    }

    @Test
    public void testCreatedAtAndUpdatedAt() {
        Date createdAt = new Date();
        Date updatedAt = new Date();

        event.setCreatedAt(createdAt);
        event.setUpdatedAt(updatedAt);

        assertEquals(createdAt, event.getCreatedAt());
        assertEquals(updatedAt, event.getUpdatedAt());
    }



    @Test
    public void testSetAndGetOrganizerId() {
        event.setOrganizerId("newOrganizer");
        assertEquals("newOrganizer", event.getOrganizerId());
    }

    @Test
    public void testSetAndGetOrganizerName() {
        event.setOrganizerName("Fake Name");
        assertEquals("Fake Name", event.getOrganizerName());
    }



    @Test
    public void testToString_ContainsKeyInformation() {
        event.setId("fakeEvent");
        String result = event.toString();

        assertTrue(result.contains("fakeEvent"));
        assertTrue(result.contains("Test Event"));
        assertTrue(result.contains("Test Location"));
        assertTrue(result.contains("OPEN"));
    }



    @Test
    public void testMaxEntrants_Zero() {
        event.setMaxEntrants(0);
        assertEquals(0, event.getMaxEntrants());
        assertEquals(0, event.getSpotsRemaining());
    }

    @Test
    public void testMaxEntrants_LargeNumber() {
        event.setMaxEntrants(10000);
        assertEquals(10000, event.getMaxEntrants());
    }



    @Test
    public void testWaitlistLimit_Null() {
        event.setWaitlistLimit(null);
        assertNull(event.getWaitlistLimit());
        assertFalse(event.isFull());
    }
}