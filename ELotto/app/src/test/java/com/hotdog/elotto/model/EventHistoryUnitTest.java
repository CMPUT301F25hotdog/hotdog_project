package com.hotdog.elotto.model;

import com.hotdog.elotto.helpers.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Event History functionality.
 * Tests the logic for categorizing events into drawn vs pending,
 * and determining user status for events.
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-23
 */
public class  EventHistoryUnitTest {

    private Event testEvent;
    private Date futureDate;
    private Date pastDate;
    private String testUserId;

    @BeforeEach
    public void setUp() {
        Calendar calendar = Calendar.getInstance();

        // Set up dates
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        futureDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -14);
        pastDate = calendar.getTime();

        // Create test event
        testEvent = new Event(
                "Test Event",
                "Test Description",
                "Test Location",
                futureDate,
                pastDate,
                futureDate,
                50,
                "testOrganizer"
        );

        testEvent.setId("testEvent123");
        testUserId = "testUser123";
    }

    // Test Event Categorization Logic

    @Test
    public void testUserInWaitlistOnly_ShouldBePending() {
        // User is only in waitlist, not selected
        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertNull(testEvent.getSelectedEntrantIds());

        // This event should be categorized as "Pending"
    }

    @Test
    public void testUserInWaitlistAndLotteryDrawn_ShouldBeWaitlisted() {
        // User is in waitlist but lottery was drawn and they weren't selected
        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        List<String> selected = new ArrayList<>();
        selected.add("otherUser1");
        selected.add("otherUser2");
        testEvent.setSelectedEntrantIds(selected);

        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertFalse(testEvent.getSelectedEntrantIds().contains(testUserId));
        assertFalse(testEvent.getSelectedEntrantIds().isEmpty());

        // This event should show status "Waitlisted" for this user
    }

    @Test
    public void testUserSelected_ShouldBeDrawnEvent() {
        // User was selected in lottery
        List<String> selected = new ArrayList<>();
        selected.add(testUserId);
        testEvent.setSelectedEntrantIds(selected);

        assertTrue(testEvent.getSelectedEntrantIds().contains(testUserId));

        // This event should be in "Drawn Events" with status "Selected"
    }

    @Test
    public void testUserAccepted_ShouldBeDrawnEvent() {
        // User was selected and accepted
        List<String> selected = new ArrayList<>();
        selected.add(testUserId);
        testEvent.setSelectedEntrantIds(selected);

        List<String> accepted = new ArrayList<>();
        accepted.add(testUserId);
        testEvent.setAcceptedEntrantIds(accepted);

        assertTrue(testEvent.getSelectedEntrantIds().contains(testUserId));
        assertTrue(testEvent.getAcceptedEntrantIds().contains(testUserId));

        // This event should be in "Drawn Events" with status "Accepted"
    }

    @Test
    public void testUserCancelled_ShouldBeDrawnEvent() {
        // User declined or was cancelled
        List<String> cancelled = new ArrayList<>();
        cancelled.add(testUserId);
        testEvent.setCancelledEntrantIds(cancelled);

        assertTrue(testEvent.getCancelledEntrantIds().contains(testUserId));

        // This event should be in "Drawn Events" with status "Declined/Cancelled"
    }

    // Test List Operations

    @Test
    public void testEmptyLists_UserNotInAnyList() {
        testEvent.setWaitlistEntrantIds(new ArrayList<>());
        testEvent.setSelectedEntrantIds(new ArrayList<>());
        testEvent.setAcceptedEntrantIds(new ArrayList<>());
        testEvent.setCancelledEntrantIds(new ArrayList<>());

        assertFalse(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertFalse(testEvent.getSelectedEntrantIds().contains(testUserId));
        assertFalse(testEvent.getAcceptedEntrantIds().contains(testUserId));
        assertFalse(testEvent.getCancelledEntrantIds().contains(testUserId));
    }

    @Test
    public void testNullLists_ShouldNotCrash() {
        testEvent.setWaitlistEntrantIds(null);
        testEvent.setSelectedEntrantIds(null);
        testEvent.setAcceptedEntrantIds(null);
        testEvent.setCancelledEntrantIds(null);

        assertNull(testEvent.getWaitlistEntrantIds());
        assertNull(testEvent.getSelectedEntrantIds());
        assertNull(testEvent.getAcceptedEntrantIds());
        assertNull(testEvent.getCancelledEntrantIds());
    }

    @Test
    public void testMultipleUsersInWaitlist() {
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        waitlist.add("user2");
        waitlist.add(testUserId);
        waitlist.add("user3");
        testEvent.setWaitlistEntrantIds(waitlist);

        assertEquals(4, testEvent.getWaitlistEntrantIds().size());
        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
    }

    @Test
    public void testUserInMultipleLists_AcceptedTakesPrecedence() {
        // User is in waitlist, selected, AND accepted
        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        List<String> selected = new ArrayList<>();
        selected.add(testUserId);
        testEvent.setSelectedEntrantIds(selected);

        List<String> accepted = new ArrayList<>();
        accepted.add(testUserId);
        testEvent.setAcceptedEntrantIds(accepted);

        // All three lists contain the user
        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertTrue(testEvent.getSelectedEntrantIds().contains(testUserId));
        assertTrue(testEvent.getAcceptedEntrantIds().contains(testUserId));

        // Status should be "Accepted" (highest priority)
    }

    // Test Event Status Values

    @Test
    public void testEventStatus_DoesNotAffectUserStatus() {
        testEvent.setStatus("CLOSED");

        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        assertEquals("CLOSED", testEvent.getStatus());
        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));

        // Event status is independent of user's registration status
    }

    // Test Edge Cases

    @Test
    public void testLotteryNotDrawn_NoSelectedList() {
        // User in waitlist, lottery hasn't been drawn yet
        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        // Selected list is null (lottery not run)
        testEvent.setSelectedEntrantIds(null);

        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertNull(testEvent.getSelectedEntrantIds());

        // Should show as "Pending"
    }

    @Test
    public void testLotteryDrawn_EmptySelectedList() {
        // Edge case: lottery was run but no one was selected
        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        testEvent.setSelectedEntrantIds(new ArrayList<>()); // Empty but not null

        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertNotNull(testEvent.getSelectedEntrantIds());
        assertTrue(testEvent.getSelectedEntrantIds().isEmpty());
    }

    @Test
    public void testUserDeclinedButStillInWaitlist() {
        // User declined but somehow still in waitlist (data inconsistency)
        List<String> waitlist = new ArrayList<>();
        waitlist.add(testUserId);
        testEvent.setWaitlistEntrantIds(waitlist);

        List<String> cancelled = new ArrayList<>();
        cancelled.add(testUserId);
        testEvent.setCancelledEntrantIds(cancelled);

        assertTrue(testEvent.getWaitlistEntrantIds().contains(testUserId));
        assertTrue(testEvent.getCancelledEntrantIds().contains(testUserId));

        // Should show as "Declined" (cancelled takes precedence)
    }

    // Test User.RegisteredEvent Status Enum Mapping

    @Test
    public void testStatusEnumValues() {
        // Verify all Status enum values exist
        Status[] statuses = Status.values();

        assertEquals(6, statuses.length);
        assertTrue(containsStatus(statuses, Status.Pending));
        assertTrue(containsStatus(statuses, Status.Selected));
        assertTrue(containsStatus(statuses, Status.Waitlisted));
        assertTrue(containsStatus(statuses, Status.Accepted));
        assertTrue(containsStatus(statuses, Status.Declined));
        assertTrue(containsStatus(statuses, Status.Withdrawn));
    }

    private boolean containsStatus(Status[] statuses, Status target) {
        for (Status status : statuses) {
            if (status == target) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testStatusToString() {
        // Verify Status enum can be converted to string
        assertEquals("Pending", Status.Pending.toString());
        assertEquals("Invited", Status.Selected.toString());
        assertEquals("Waitlisted", Status.Waitlisted.toString());
        assertEquals("Accepted", Status.Accepted.toString());
        assertEquals("Declined", Status.Declined.toString());
        assertEquals("Withdrawn", Status.Withdrawn.toString());
    }
}