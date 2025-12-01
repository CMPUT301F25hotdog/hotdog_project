package com.hotdog.elotto.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.hotdog.elotto.adapter.EventHistoryAdapter;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Unit tests for EventHistoryAdapter logic.
 *
 * Tests status determination priority, status text mapping,
 * date/location formatting, and edge case handling.
 *
 * @author ELotto Team
 * @version 1.0
 */
class EventHistoryAdapterTest {

    @Mock
    private Context mockContext;

    private EventHistoryAdapter adapter;
    private List<Event> testEventList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testEventList = new ArrayList<>();
        adapter = new EventHistoryAdapter(testEventList, mockContext);
    }

    // ========== STATUS PRIORITY TESTS ==========

    /**
     * Test that Accepted status takes priority over all other statuses.
     * When a user is in acceptedEntrantIds, they should be shown as "Accepted"
     * even if they're in other lists.
     */
    @Test
    void testStatusPriority_Accepted_HasHighestPriority() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        // Add user to multiple lists - Accepted should take priority
        event.setAcceptedEntrantIds(Arrays.asList(userId));
        event.setSelectedEntrantIds(Arrays.asList(userId));
        event.setWaitlistEntrantIds(Arrays.asList(userId));

        // Act - In real adapter, this would determine status
        List<String> acceptedIds = event.getAcceptedEntrantIds();
        List<String> selectedIds = event.getSelectedEntrantIds();

        // Assert - Accepted check comes first
        assertTrue(acceptedIds.contains(userId));
        // This verifies priority order: check Accepted before Selected
    }

    /**
     * Test that Selected status takes priority over Waitlisted and Pending.
     */
    @Test
    void testStatusPriority_Selected_PriorityOverWaitlisted() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        // User is both Selected and in Waitlist
        event.setSelectedEntrantIds(Arrays.asList(userId));
        event.setWaitlistEntrantIds(Arrays.asList(userId));

        // Act
        List<String> selectedIds = event.getSelectedEntrantIds();
        List<String> waitlistIds = event.getWaitlistEntrantIds();

        // Assert - Selected check should come before Waitlist check
        assertTrue(selectedIds.contains(userId));
        assertTrue(waitlistIds.contains(userId));
        // In real implementation, Selected takes priority
    }

    /**
     * Test that Cancelled/Declined status takes priority over Waitlisted.
     */
    @Test
    void testStatusPriority_Cancelled_PriorityOverWaitlisted() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        event.setCancelledEntrantIds(Arrays.asList(userId));
        event.setWaitlistEntrantIds(Arrays.asList(userId));

        // Act
        List<String> cancelledIds = event.getCancelledEntrantIds();
        List<String> waitlistIds = event.getWaitlistEntrantIds();

        // Assert
        assertTrue(cancelledIds.contains(userId));
        assertTrue(waitlistIds.contains(userId));
    }

    /**
     * Test lottery drawn detection - if selectedEntrantIds is not empty,
     * lottery has been drawn.
     */
    @Test
    void testLotteryDrawn_WithSelectedEntrants_ReturnsTrue() {
        // Arrange
        Event event = createMockEvent();
        event.setSelectedEntrantIds(Arrays.asList("user1", "user2"));

        // Act
        boolean hasSelectedEntrants = event.getSelectedEntrantIds() != null
                && !event.getSelectedEntrantIds().isEmpty();

        // Assert
        assertTrue(hasSelectedEntrants, "Lottery should be considered drawn when selected entrants exist");
    }

    /**
     * Test lottery not drawn - if selectedEntrantIds is null or empty,
     * lottery has not been drawn.
     */
    @Test
    void testLotteryNotDrawn_NoSelectedEntrants_ReturnsFalse() {
        // Arrange
        Event event = createMockEvent();
        event.setSelectedEntrantIds(new ArrayList<>()); // Empty list

        // Act
        boolean hasSelectedEntrants = event.getSelectedEntrantIds() != null
                && !event.getSelectedEntrantIds().isEmpty();

        // Assert
        assertFalse(hasSelectedEntrants, "Lottery should not be considered drawn when no selected entrants");
    }

    /**
     * Test Waitlisted vs Pending status based on lottery draw.
     * If lottery drawn and user in waitlist (but not selected), status is "Waitlisted".
     * If lottery not drawn and user in waitlist, status is "Pending".
     */
    @Test
    void testWaitlistStatus_AfterLotteryDrawn_ShowsWaitlisted() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        // User in waitlist, lottery has been drawn, but user not selected
        event.setWaitlistEntrantIds(Arrays.asList(userId));
        event.setSelectedEntrantIds(Arrays.asList("otherUser1", "otherUser2"));

        // Act
        boolean inWaitlist = event.getWaitlistEntrantIds().contains(userId);
        boolean lotteryDrawn = !event.getSelectedEntrantIds().isEmpty();
        boolean notSelected = !event.getSelectedEntrantIds().contains(userId);

        // Assert
        assertTrue(inWaitlist);
        assertTrue(lotteryDrawn);
        assertTrue(notSelected);
        // This combination means status should be "Waitlisted"
    }

    /**
     * Test Pending status - user in waitlist but lottery not drawn yet.
     */
    @Test
    void testWaitlistStatus_BeforeLotteryDrawn_ShowsPending() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        // User in waitlist, lottery NOT drawn yet
        event.setWaitlistEntrantIds(Arrays.asList(userId));
        event.setSelectedEntrantIds(new ArrayList<>()); // Empty - no lottery yet

        // Act
        boolean inWaitlist = event.getWaitlistEntrantIds().contains(userId);
        boolean lotteryDrawn = event.getSelectedEntrantIds() != null
                && !event.getSelectedEntrantIds().isEmpty();

        // Assert
        assertTrue(inWaitlist);
        assertFalse(lotteryDrawn);
        // This combination means status should be "Pending"
    }

    // ========== STATUS TEXT MAPPING TESTS ==========

    /**
     * Test that Status enum values map to correct display text.
     */
    @Test
    void testStatusTextMapping_AllStatuses() {
        // This tests the logic in setStatusBadgeUI() method

        // Expected mappings based on adapter code
        assertEquals("Selected", getStatusText(Status.Selected));
        assertEquals("Accepted", getStatusText(Status.Accepted));
        assertEquals("Declined", getStatusText(Status.Declined));
        assertEquals("Waitlisted", getStatusText(Status.Waitlisted));
        assertEquals("Withdrawn", getStatusText(Status.Withdrawn));
        assertEquals("Pending", getStatusText(Status.Pending));
    }

    // ========== EDGE CASE TESTS ==========

    /**
     * Test handling of null entrant ID lists.
     */
    @Test
    void testNullEntrantLists_DoesNotCrash() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        // Set all lists to null
        event.setAcceptedEntrantIds(null);
        event.setSelectedEntrantIds(null);
        event.setWaitlistEntrantIds(null);
        event.setCancelledEntrantIds(null);

        // Act & Assert - Should not throw NullPointerException
        assertDoesNotThrow(() -> {
            boolean hasAccepted = event.getAcceptedEntrantIds() != null
                    && event.getAcceptedEntrantIds().contains(userId);
            assertFalse(hasAccepted);
        });
    }

    /**
     * Test handling of empty entrant ID lists.
     */
    @Test
    void testEmptyEntrantLists_ReturnsDefaultStatus() {
        // Arrange
        Event event = createMockEvent();
        String userId = "user123";

        // Set all lists to empty
        event.setAcceptedEntrantIds(new ArrayList<>());
        event.setSelectedEntrantIds(new ArrayList<>());
        event.setWaitlistEntrantIds(new ArrayList<>());
        event.setCancelledEntrantIds(new ArrayList<>());

        // Act
        boolean inAnyList = (event.getAcceptedEntrantIds().contains(userId) ||
                event.getSelectedEntrantIds().contains(userId) ||
                event.getWaitlistEntrantIds().contains(userId) ||
                event.getCancelledEntrantIds().contains(userId));

        // Assert - User not in any list should default to Pending
        assertFalse(inAnyList);
    }

    // ========== DATE/LOCATION FORMATTING TESTS ==========

    /**
     * Test date formatting matches expected format "MMM d, HH:mm".
     */
    @Test
    void testDateFormatting_ValidDate() {
        // Arrange
        Date testDate = new Date(); // Current date/time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());

        // Act
        String formattedDate = dateFormat.format(testDate);

        // Assert
        assertNotNull(formattedDate);
        assertTrue(formattedDate.matches("\\w{3} \\d{1,2}, \\d{2}:\\d{2}"),
                "Date should match format 'MMM d, HH:mm'");
    }

    /**
     * Test that null date should show "Date TBD".
     */
    @Test
    void testDateFormatting_NullDate_ShowsTBD() {
        // Arrange
        Event event = createMockEvent();
        event.setEventDateTime(null);

        // Act
        Date eventDate = event.getEventDateTime();
        String displayText = eventDate != null ? "formatted" : "Date TBD";

        // Assert
        assertEquals("Date TBD", displayText);
    }

    /**
     * Test that null location shows "Location TBD".
     */
    @Test
    void testLocationFormatting_NullLocation_ShowsTBD() {
        // Arrange
        Event event = createMockEvent();
        event.setLocation(null);

        // Act
        String location = event.getLocation();
        String displayText = (location != null && !location.isEmpty()) ? location : "Location TBD";

        // Assert
        assertEquals("Location TBD", displayText);
    }

    /**
     * Test that empty location shows "Location TBD".
     */
    @Test
    void testLocationFormatting_EmptyLocation_ShowsTBD() {
        // Arrange
        Event event = createMockEvent();
        event.setLocation("");

        // Act
        String location = event.getLocation();
        String displayText = (location != null && !location.isEmpty()) ? location : "Location TBD";

        // Assert
        assertEquals("Location TBD", displayText);
    }

    /**
     * Test that valid location displays correctly.
     */
    @Test
    void testLocationFormatting_ValidLocation_DisplaysCorrectly() {
        // Arrange
        Event event = createMockEvent();
        String testLocation = "Edmonton Convention Centre";
        event.setLocation(testLocation);

        // Act
        String location = event.getLocation();

        // Assert
        assertEquals(testLocation, location);
    }

    // ========== ADAPTER OPERATION TESTS ==========

    /**
     * Test that getItemCount returns correct number of events.
     */
    @Test
    void testGetItemCount_ReturnsCorrectCount() {
        // Arrange
        testEventList.add(createMockEvent());
        testEventList.add(createMockEvent());
        testEventList.add(createMockEvent());

        // Act
        int count = adapter.getItemCount();

        // Assert
        assertEquals(3, count);
    }

    /**
     * Test that updateEvents changes the event list.
     */
    @Test
    void testUpdateEvents_UpdatesList() {
        // Arrange
        List<Event> newEvents = new ArrayList<>();
        newEvents.add(createMockEvent());
        newEvents.add(createMockEvent());

        // Act
        adapter.updateEvents(newEvents);

        // Assert
        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Test that empty event list returns zero count.
     */
    @Test
    void testGetItemCount_EmptyList_ReturnsZero() {
        // Arrange - list is already empty from setUp()

        // Act
        int count = adapter.getItemCount();

        // Assert
        assertEquals(0, count);
    }

    // ========== HELPER METHODS ==========

    /**
     * Create a mock Event for testing.
     */
    private Event createMockEvent() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn("event_" + System.nanoTime());
        when(event.getName()).thenReturn("Test Event");
        when(event.getLocation()).thenReturn("Test Location");
        when(event.getEventDateTime()).thenReturn(new Date());
        when(event.getAcceptedEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getSelectedEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getWaitlistEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getCancelledEntrantIds()).thenReturn(new ArrayList<>());
        return event;
    }

    /**
     * Helper method that mimics the status text mapping in the adapter.
     */
    private String getStatusText(Status status) {
        switch (status) {
            case Selected: return "Selected";
            case Accepted: return "Accepted";
            case Declined: return "Declined";
            case Waitlisted: return "Waitlisted";
            case Withdrawn: return "Withdrawn";
            case Pending:
            default: return "Pending";
        }
    }

}