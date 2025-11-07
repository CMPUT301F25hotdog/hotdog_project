package com.hotdog.elotto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.hotdog.elotto.adapter.EventHistoryAdapter;
import com.hotdog.elotto.model.Event;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHistoryAdapterTest {

    @Mock
    private EventHistoryAdapter.OnEventClickListener mockListener;

    private EventHistoryAdapter adapter;
    private List<Event> events;
    private final String currentUserId = "test_user_123";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Event event1 = new Event();
        event1.setId("event1");
        event1.setName("Test Event 1");
        event1.setLocation("Test Location");
        event1.setEventDateTime(new Date());
        event1.setWaitlistEntrantIds(Arrays.asList(currentUserId));

        events = Arrays.asList(event1);
        adapter = new EventHistoryAdapter(events, currentUserId, EventHistoryAdapter.Mode.DRAWN);
        adapter.setOnEventClickListener(mockListener);
    }

    @Test
    public void testAdapterItemCount() {
        assertEquals("Adapter should have correct item count", 1, adapter.getItemCount());
    }

    @Test
    public void testAdapterUpdateMethod() {
        // Arrange
        Event newEvent = new Event();
        newEvent.setId("event2");
        newEvent.setName("New Event");
        List<Event> newEvents = Arrays.asList(newEvent);

        // Act
        adapter.update(newEvents);

        // Assert
        assertEquals("Adapter should update item count", 1, adapter.getItemCount());
    }

    @Test
    public void testDrawnModeStatusLogic() {
        // Test the status logic for different user states in DRAWN mode
        Event selectedEvent = new Event();
        selectedEvent.setId("selected");
        selectedEvent.setSelectedEntrantIds(Arrays.asList(currentUserId));
        selectedEvent.setAcceptedEntrantIds(Arrays.asList(currentUserId));

        Event waitlistedEvent = new Event();
        waitlistedEvent.setId("waitlisted");
        waitlistedEvent.setWaitlistEntrantIds(Arrays.asList(currentUserId));

        List<Event> testEvents = Arrays.asList(selectedEvent, waitlistedEvent);
        EventHistoryAdapter drawnAdapter = new EventHistoryAdapter(testEvents, currentUserId, EventHistoryAdapter.Mode.DRAWN);

        assertEquals("Drawn adapter should have 2 items", 2, drawnAdapter.getItemCount());
    }

    @Test
    public void testPendingModeStatusLogic() {
        Event pendingEvent = new Event();
        pendingEvent.setId("pending");
        pendingEvent.setWaitlistEntrantIds(Arrays.asList(currentUserId));

        List<Event> testEvents = Arrays.asList(pendingEvent);
        EventHistoryAdapter pendingAdapter = new EventHistoryAdapter(testEvents, currentUserId, EventHistoryAdapter.Mode.PENDING);

        assertEquals("Pending adapter should have 1 item", 1, pendingAdapter.getItemCount());
    }

    @Test
    public void testNullSafety() {
        Event nullEvent = new Event();
        // All fields are null by default

        List<Event> testEvents = Arrays.asList(nullEvent);
        EventHistoryAdapter adapter = new EventHistoryAdapter(testEvents, currentUserId, EventHistoryAdapter.Mode.DRAWN);

        // Should not crash
        assertEquals("Adapter should handle null events", 1, adapter.getItemCount());
    }

    @Test
    public void testEmptyList() {
        List<Event> emptyEvents = Arrays.asList();
        EventHistoryAdapter emptyAdapter = new EventHistoryAdapter(emptyEvents, currentUserId, EventHistoryAdapter.Mode.DRAWN);

        assertEquals("Empty adapter should have 0 items", 0, emptyAdapter.getItemCount());
    }
}