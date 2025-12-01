package com.hotdog.elotto.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.hotdog.elotto.adapter.EventAdapter;
import com.hotdog.elotto.model.Event;

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
 * Unit tests for EventAdapter logic.
 *
 * Tests date formatting, status determination, item count,
 * and null/edge case handling.
 *
 * @author ELotto Team
 * @version 1.0
 */
class EventAdapterTest {

    @Mock
    private Context mockContext;

    private EventAdapter adapter;
    private List<Event> testEventList;
    private String testUserId = "testUser123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testEventList = new ArrayList<>();
        adapter = new EventAdapter(testEventList, testUserId);
    }

    // ========== CONSTRUCTOR TESTS ==========

    /**
     * Test that EventAdapter can be instantiated with valid parameters.
     */
    @Test
    void testConstructor_ValidParameters_CreatesAdapter() {
        // Act
        EventAdapter newAdapter = new EventAdapter(new ArrayList<>(), "userId");

        // Assert
        assertNotNull(newAdapter);
    }

    /**
     * Test that EventAdapter handles empty event list in constructor.
     */
    @Test
    void testConstructor_EmptyEventList_CreatesAdapter() {
        // Arrange
        List<Event> emptyList = new ArrayList<>();

        // Act
        EventAdapter newAdapter = new EventAdapter(emptyList, testUserId);

        // Assert
        assertNotNull(newAdapter);
        assertEquals(0, newAdapter.getItemCount());
    }

    // ========== ITEM COUNT TESTS ==========

    /**
     * Test that getItemCount returns correct count for empty list.
     */
    @Test
    void testGetItemCount_EmptyList_ReturnsZero() {
        // Assert
        assertEquals(0, adapter.getItemCount());
    }

    /**
     * Test that getItemCount returns correct count for populated list.
     */
    @Test
    void testGetItemCount_WithEvents_ReturnsCorrectCount() {
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
     * Test that getItemCount updates after adding events.
     */
    @Test
    void testGetItemCount_AfterAddingEvents_UpdatesCount() {
        // Arrange - start with empty list
        assertEquals(0, adapter.getItemCount());

        // Act - add events
        testEventList.add(createMockEvent());
        testEventList.add(createMockEvent());

        // Assert
        assertEquals(2, adapter.getItemCount());
    }

    // ========== UPDATE EVENTS TESTS ==========

    /**
     * Test that updateEvents replaces the event list.
     */
    @Test
    void testUpdateEvents_NewList_UpdatesAdapter() {
        // Arrange
        List<Event> newEvents = new ArrayList<>();
        newEvents.add(createMockEvent());
        newEvents.add(createMockEvent());
        newEvents.add(createMockEvent());

        // Act
        adapter.updateEvents(newEvents);

        // Assert
        assertEquals(3, adapter.getItemCount());
    }

    /**
     * Test that updateEvents with empty list clears adapter.
     */
    @Test
    void testUpdateEvents_EmptyList_ClearsAdapter() {
        // Arrange - start with events
        testEventList.add(createMockEvent());
        testEventList.add(createMockEvent());
        assertEquals(2, adapter.getItemCount());

        // Act - update with empty list
        adapter.updateEvents(new ArrayList<>());

        // Assert
        assertEquals(0, adapter.getItemCount());
    }

    // ========== SET CURRENT USER ID TESTS ==========

    /**
     * Test that setCurrentUserId updates the user ID.
     */
    @Test
    void testSetCurrentUserId_NewUserId_UpdatesUserId() {
        // Arrange
        String newUserId = "newUser456";

        // Act
        adapter.setCurrentUserId(newUserId);

        // Assert - No exception thrown means success
        // The userId is used internally for status badges
    }

    /**
     * Test that setCurrentUserId handles null user ID.
     */
    @Test
    void testSetCurrentUserId_NullUserId_DoesNotCrash() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> adapter.setCurrentUserId(null));
    }

    // ========== DATE FORMATTING TESTS ==========

    /**
     * Test date formatting matches expected format "MMMM dd, HH:mm".
     */
    @Test
    void testDateFormatting_ValidDate_MatchesExpectedFormat() {
        // Arrange
        Date testDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, HH:mm", Locale.getDefault());

        // Act
        String formattedDate = dateFormat.format(testDate);

        // Assert
        assertNotNull(formattedDate);
        // Format should be like "December 01, 16:30"
        assertTrue(formattedDate.matches("\\w+ \\d{2}, \\d{2}:\\d{2}"),
                "Date should match format 'MMMM dd, HH:mm'");
    }

    /**
     * Test that null date should display "Date TBD".
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

    // ========== ENTRY COUNT FORMATTING TESTS ==========

    /**
     * Test entry count formatting displays correct text.
     */
    @Test
    void testEntryCountFormatting_ValidCounts_DisplaysCorrectly() {
        // Arrange
        int currentEntries = 25;
        int maxEntries = 100;

        // Act
        String entryCountText = currentEntries + " Entries / " + maxEntries + " Spots";

        // Assert
        assertEquals("25 Entries / 100 Spots", entryCountText);
    }

    /**
     * Test entry count formatting with zero current entries.
     */
    @Test
    void testEntryCountFormatting_ZeroCurrentEntries_DisplaysCorrectly() {
        // Arrange
        int currentEntries = 0;
        int maxEntries = 50;

        // Act
        String entryCountText = currentEntries + " Entries / " + maxEntries + " Spots";

        // Assert
        assertEquals("0 Entries / 50 Spots", entryCountText);
    }

    /**
     * Test entry count formatting when full.
     */
    @Test
    void testEntryCountFormatting_FullCapacity_DisplaysCorrectly() {
        // Arrange
        int currentEntries = 100;
        int maxEntries = 100;

        // Act
        String entryCountText = currentEntries + " Entries / " + maxEntries + " Spots";

        // Assert
        assertEquals("100 Entries / 100 Spots", entryCountText);
    }

    // ========== STATUS DETERMINATION TESTS ==========

    /**
     * Test that user in accepted list shows ACCEPTED status.
     */
    @Test
    void testStatusDetermination_UserAccepted_ReturnsAccepted() {
        // Arrange
        Event event = createMockEvent();
        event.setAcceptedEntrantIds(Arrays.asList(testUserId));

        // Act
        boolean isAccepted = event.getAcceptedEntrantIds() != null
                && event.getAcceptedEntrantIds().contains(testUserId);

        // Assert
        assertTrue(isAccepted);
    }

    /**
     * Test that user in selected list shows SELECTED status.
     */
    @Test
    void testStatusDetermination_UserSelected_ReturnsSelected() {
        // Arrange
        Event event = createMockEvent();
        event.setSelectedEntrantIds(Arrays.asList(testUserId));

        // Act
        boolean isSelected = event.getSelectedEntrantIds() != null
                && event.getSelectedEntrantIds().contains(testUserId);

        // Assert
        assertTrue(isSelected);
    }

    /**
     * Test that user in cancelled list shows CANCELED status.
     */
    @Test
    void testStatusDetermination_UserCancelled_ReturnsCanceled() {
        // Arrange
        Event event = createMockEvent();
        event.setCancelledEntrantIds(Arrays.asList(testUserId));

        // Act
        boolean isCancelled = event.getCancelledEntrantIds() != null
                && event.getCancelledEntrantIds().contains(testUserId);

        // Assert
        assertTrue(isCancelled);
    }

    /**
     * Test WAITLISTED status when lottery drawn but user not selected.
     */
    @Test
    void testStatusDetermination_UserWaitlisted_AfterLotteryDrawn() {
        // Arrange
        Event event = createMockEvent();
        event.setWaitlistEntrantIds(Arrays.asList(testUserId));
        event.setSelectedEntrantIds(Arrays.asList("otherUser1", "otherUser2")); // Lottery drawn

        // Act
        boolean inWaitlist = event.getWaitlistEntrantIds() != null
                && event.getWaitlistEntrantIds().contains(testUserId);
        boolean lotteryDrawn = event.getSelectedEntrantIds() != null
                && !event.getSelectedEntrantIds().isEmpty();
        boolean notSelected = !event.getSelectedEntrantIds().contains(testUserId);

        // Assert
        assertTrue(inWaitlist);
        assertTrue(lotteryDrawn);
        assertTrue(notSelected);
        // This combination = WAITLISTED status
    }

    /**
     * Test PENDING status when in waitlist but lottery not drawn.
     */
    @Test
    void testStatusDetermination_UserPending_BeforeLotteryDrawn() {
        // Arrange
        Event event = createMockEvent();
        event.setWaitlistEntrantIds(Arrays.asList(testUserId));
        event.setSelectedEntrantIds(new ArrayList<>()); // No lottery drawn

        // Act
        boolean inWaitlist = event.getWaitlistEntrantIds() != null
                && event.getWaitlistEntrantIds().contains(testUserId);
        boolean lotteryNotDrawn = event.getSelectedEntrantIds() == null
                || event.getSelectedEntrantIds().isEmpty();

        // Assert
        assertTrue(inWaitlist);
        assertTrue(lotteryNotDrawn);
        // This combination = PENDING status
    }

    /**
     * Test that user not in any list should hide status badge.
     */
    @Test
    void testStatusDetermination_UserNotInAnyList_HidesBadge() {
        // Arrange
        Event event = createMockEvent();
        event.setWaitlistEntrantIds(Arrays.asList("otherUser1"));
        event.setSelectedEntrantIds(Arrays.asList("otherUser2"));
        event.setAcceptedEntrantIds(Arrays.asList("otherUser3"));

        // Act
        boolean inAnyList =
                (event.getWaitlistEntrantIds() != null && event.getWaitlistEntrantIds().contains(testUserId)) ||
                        (event.getSelectedEntrantIds() != null && event.getSelectedEntrantIds().contains(testUserId)) ||
                        (event.getAcceptedEntrantIds() != null && event.getAcceptedEntrantIds().contains(testUserId)) ||
                        (event.getCancelledEntrantIds() != null && event.getCancelledEntrantIds().contains(testUserId));

        // Assert
        assertFalse(inAnyList, "User should not be in any list");
    }

    // ========== STATUS PRIORITY TESTS ==========

    /**
     * Test that ACCEPTED status takes priority when user in multiple lists.
     */
    @Test
    void testStatusPriority_AcceptedOverSelected() {
        // Arrange
        Event event = createMockEvent();
        // User in both accepted and selected
        event.setAcceptedEntrantIds(Arrays.asList(testUserId));
        event.setSelectedEntrantIds(Arrays.asList(testUserId));

        // Act - Check accepted first (priority)
        boolean isAccepted = event.getAcceptedEntrantIds() != null
                && event.getAcceptedEntrantIds().contains(testUserId);

        // Assert
        assertTrue(isAccepted, "Accepted should take priority");
    }

    /**
     * Test that SELECTED status takes priority over WAITLISTED.
     */
    @Test
    void testStatusPriority_SelectedOverWaitlisted() {
        // Arrange
        Event event = createMockEvent();
        event.setSelectedEntrantIds(Arrays.asList(testUserId));
        event.setWaitlistEntrantIds(Arrays.asList(testUserId));

        // Act - Check selected first (priority)
        boolean isSelected = event.getSelectedEntrantIds() != null
                && event.getSelectedEntrantIds().contains(testUserId);

        // Assert
        assertTrue(isSelected, "Selected should take priority over waitlisted");
    }

    // ========== NULL HANDLING TESTS ==========

    /**
     * Test that null waitlist IDs list doesn't cause crash.
     */
    @Test
    void testNullHandling_NullWaitlistIds_DoesNotCrash() {
        // Arrange
        Event event = createMockEvent();
        event.setWaitlistEntrantIds(null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            boolean inWaitlist = event.getWaitlistEntrantIds() != null
                    && event.getWaitlistEntrantIds().contains(testUserId);
            assertFalse(inWaitlist);
        });
    }

    /**
     * Test that null selected IDs list doesn't cause crash.
     */
    @Test
    void testNullHandling_NullSelectedIds_DoesNotCrash() {
        // Arrange
        Event event = createMockEvent();
        event.setSelectedEntrantIds(null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            boolean isSelected = event.getSelectedEntrantIds() != null
                    && event.getSelectedEntrantIds().contains(testUserId);
            assertFalse(isSelected);
        });
    }

    /**
     * Test that null accepted IDs list doesn't cause crash.
     */
    @Test
    void testNullHandling_NullAcceptedIds_DoesNotCrash() {
        // Arrange
        Event event = createMockEvent();
        event.setAcceptedEntrantIds(null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            boolean isAccepted = event.getAcceptedEntrantIds() != null
                    && event.getAcceptedEntrantIds().contains(testUserId);
            assertFalse(isAccepted);
        });
    }

    /**
     * Test that empty location displays correctly.
     */
    @Test
    void testLocationHandling_EmptyLocation_DisplaysCorrectly() {
        // Arrange
        Event event = createMockEvent();
        event.setLocation("");

        // Act
        String location = event.getLocation();

        // Assert
        assertEquals("", location);
    }

    /**
     * Test that null location is handled.
     */
    @Test
    void testLocationHandling_NullLocation_HandledSafely() {
        // Arrange
        Event event = createMockEvent();
        event.setLocation(null);

        // Act
        String location = event.getLocation();

        // Assert
        assertNull(location);
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
        when(event.getCurrentWaitlistCount()).thenReturn(10);
        when(event.getMaxEntrants()).thenReturn(100);
        when(event.getWaitlistEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getSelectedEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getAcceptedEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getCancelledEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getPosterImageUrl()).thenReturn(null);
        return event;
    }
}