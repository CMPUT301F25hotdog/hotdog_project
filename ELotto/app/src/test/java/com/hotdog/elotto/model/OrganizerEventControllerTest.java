package com.hotdog.elotto.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.firebase.Timestamp;
import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.FirestoreListCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.OrganizerEventController;
import com.hotdog.elotto.helpers.Status;
import com.hotdog.elotto.model.EntrantInfo;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for OrganizerEventController.
 *
 * Tests organizer-specific operations including loading entrants,
 * running lottery draws, and sending notifications.
 *
 * @author ELotto Team
 * @version 1.0
 */
class OrganizerEventControllerTest {

    @Mock
    private EventRepository mockEventRepository;

    @Mock
    private UserRepository mockUserRepository;

    private OrganizerEventController controller;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new OrganizerEventController(mockEventRepository, mockUserRepository);
        mockEvent = createMockEvent();
    }

    // ========== LOAD WAITLIST ENTRANTS TESTS ==========

    /**
     * Test that loadWaitingListEntrants successfully loads entrants.
     */
    @Test
    void testLoadWaitingListEntrants_Success() {
        // Arrange
        String eventId = "event123";
        List<String> waitlistIds = Arrays.asList("user1", "user2", "user3");
        mockEvent.setWaitlistEntrantIds(waitlistIds);

        List<User> mockUsers = createMockUsers(waitlistIds);

        // Mock event repository
        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        // Mock user repository
        doAnswer(invocation -> {
            FirestoreListCallback<User> callback = invocation.getArgument(1);
            callback.onSuccess(mockUsers);
            return null;
        }).when(mockUserRepository).getUsersByIds(eq(waitlistIds), any(FirestoreListCallback.class));

        // Act
        controller.loadWaitingListEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> results) {
                // Assert
                assertEquals(3, results.size());
                assertEquals("User user1", results.get(0).getName());
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });

        // Verify interactions
        verify(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));
        verify(mockUserRepository).getUsersByIds(eq(waitlistIds), any(FirestoreListCallback.class));
    }

    /**
     * Test that loadWaitingListEntrants returns empty list when waitlist is empty.
     */
    @Test
    void testLoadWaitingListEntrants_EmptyWaitlist_ReturnsEmptyList() {
        // Arrange
        String eventId = "event123";
        mockEvent.setWaitlistEntrantIds(new ArrayList<>());

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        // Act
        controller.loadWaitingListEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> results) {
                // Assert
                assertTrue(results.isEmpty());
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });
    }

    /**
     * Test that loadWaitingListEntrants handles event not found error.
     */
    @Test
    void testLoadWaitingListEntrants_EventNotFound_ReturnsError() {
        // Arrange
        String eventId = "nonexistent";

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onError("Event not found");
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        // Act
        controller.loadWaitingListEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> results) {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Event not found", errorMessage);
            }
        });
    }

    // ========== LOAD SELECTED ENTRANTS TESTS ==========

    /**
     * Test that loadSelectedEntrants successfully loads selected users.
     */
    @Test
    void testLoadSelectedEntrants_Success() {
        // Arrange
        String eventId = "event123";
        List<String> selectedIds = Arrays.asList("user1", "user2");
        mockEvent.setSelectedEntrantIds(selectedIds);

        List<User> mockUsers = createMockUsers(selectedIds);

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        doAnswer(invocation -> {
            FirestoreListCallback<User> callback = invocation.getArgument(1);
            callback.onSuccess(mockUsers);
            return null;
        }).when(mockUserRepository).getUsersByIds(eq(selectedIds), any(FirestoreListCallback.class));

        // Act
        controller.loadSelectedEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> results) {
                // Assert
                assertEquals(2, results.size());
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });
    }

    // ========== LOAD ACCEPTED ENTRANTS TESTS ==========

    /**
     * Test that loadAcceptedEntrants successfully loads accepted users.
     */
    @Test
    void testLoadAcceptedEntrants_Success() {
        // Arrange
        String eventId = "event123";
        List<String> acceptedIds = Arrays.asList("user1");
        mockEvent.setAcceptedEntrantIds(acceptedIds);

        List<User> mockUsers = createMockUsers(acceptedIds);

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        doAnswer(invocation -> {
            FirestoreListCallback<User> callback = invocation.getArgument(1);
            callback.onSuccess(mockUsers);
            return null;
        }).when(mockUserRepository).getUsersByIds(eq(acceptedIds), any(FirestoreListCallback.class));

        // Act
        controller.loadAcceptedEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> results) {
                // Assert
                assertEquals(1, results.size());
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });
    }

    // ========== LOAD CANCELLED ENTRANTS TESTS ==========

    /**
     * Test that loadCancelledEntrants successfully loads cancelled users.
     */
    @Test
    void testLoadCancelledEntrants_Success() {
        // Arrange
        String eventId = "event123";
        List<String> cancelledIds = Arrays.asList("user1", "user2");
        mockEvent.setCancelledEntrantIds(cancelledIds);

        List<User> mockUsers = createMockUsers(cancelledIds);

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        doAnswer(invocation -> {
            FirestoreListCallback<User> callback = invocation.getArgument(1);
            callback.onSuccess(mockUsers);
            return null;
        }).when(mockUserRepository).getUsersByIds(eq(cancelledIds), any(FirestoreListCallback.class));

        // Act
        controller.loadCancelledEntrants(eventId, new FirestoreListCallback<EntrantInfo>() {
            @Override
            public void onSuccess(List<EntrantInfo> results) {
                // Assert
                assertEquals(2, results.size());
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });
    }

    // ========== LOTTERY DRAW TESTS ==========

    /**
     * Test that runLotteryDraw successfully selects winners.
     */
    @Test
    void testRunLotteryDraw_Success() {
        // Arrange
        String eventId = "event123";
        int numberToSelect = 2;
        List<String> waitlistIds = Arrays.asList("user1", "user2", "user3", "user4", "user5");
        mockEvent.setWaitlistEntrantIds(new ArrayList<>(waitlistIds));

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        doAnswer(invocation -> {
            OperationCallback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(mockEventRepository).moveEntrantsToSelected(eq(eventId), any(List.class), any(OperationCallback.class));

        // Act
        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                // Assert - success callback called
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });

        // Verify moveEntrantsToSelected was called
        ArgumentCaptor<List> winnersCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockEventRepository).moveEntrantsToSelected(eq(eventId), winnersCaptor.capture(), any(OperationCallback.class));

        // Verify correct number of winners selected
        assertEquals(numberToSelect, winnersCaptor.getValue().size());
    }

    /**
     * Test that runLotteryDraw fails when numberToSelect is zero.
     */
    @Test
    void testRunLotteryDraw_ZeroNumber_ReturnsError() {
        // Arrange
        String eventId = "event123";
        int numberToSelect = 0;

        // Act
        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Number to select must be greater than 0", errorMessage);
            }
        });
    }

    /**
     * Test that runLotteryDraw fails when numberToSelect is negative.
     */
    @Test
    void testRunLotteryDraw_NegativeNumber_ReturnsError() {
        // Arrange
        String eventId = "event123";
        int numberToSelect = -5;

        // Act
        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Number to select must be greater than 0", errorMessage);
            }
        });
    }

    /**
     * Test that runLotteryDraw fails when waitlist is empty.
     */
    @Test
    void testRunLotteryDraw_EmptyWaitlist_ReturnsError() {
        // Arrange
        String eventId = "event123";
        int numberToSelect = 2;
        mockEvent.setWaitlistEntrantIds(new ArrayList<>());

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        // Act
        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Waiting list is empty", errorMessage);
            }
        });
    }

    /**
     * Test that runLotteryDraw fails when numberToSelect exceeds waitlist size.
     */
    @Test
    void testRunLotteryDraw_ExceedsWaitlistSize_ReturnsError() {
        // Arrange
        String eventId = "event123";
        int numberToSelect = 10;
        List<String> waitlistIds = Arrays.asList("user1", "user2", "user3");
        mockEvent.setWaitlistEntrantIds(new ArrayList<>(waitlistIds));

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        // Act
        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertTrue(errorMessage.contains("Cannot select 10 entrants"));
                assertTrue(errorMessage.contains("Only 3 in waiting list"));
            }
        });
    }

    /**
     * Test that runLotteryDraw can select all waitlist members.
     */
    @Test
    void testRunLotteryDraw_SelectAllWaitlist_Success() {
        // Arrange
        String eventId = "event123";
        List<String> waitlistIds = Arrays.asList("user1", "user2", "user3");
        int numberToSelect = waitlistIds.size();
        mockEvent.setWaitlistEntrantIds(new ArrayList<>(waitlistIds));

        doAnswer(invocation -> {
            FirestoreCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepository).getEventById(eq(eventId), any(FirestoreCallback.class));

        doAnswer(invocation -> {
            OperationCallback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(mockEventRepository).moveEntrantsToSelected(eq(eventId), any(List.class), any(OperationCallback.class));

        // Act
        controller.runLotteryDraw(eventId, numberToSelect, new OperationCallback() {
            @Override
            public void onSuccess() {
                // Assert - success
            }

            @Override
            public void onError(String errorMessage) {
                fail("Should not fail: " + errorMessage);
            }
        });

        // Verify all were selected
        ArgumentCaptor<List> winnersCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockEventRepository).moveEntrantsToSelected(eq(eventId), winnersCaptor.capture(), any(OperationCallback.class));
        assertEquals(3, winnersCaptor.getValue().size());
    }

    // ========== SEND NOTIFICATION TESTS ==========

    /**
     * Test that sendNotificationToEntrants fails with empty user list.
     */
    @Test
    void testSendNotificationToEntrants_EmptyUserList_ReturnsError() {
        // Arrange
        String eventId = "event123";
        List<String> emptyList = new ArrayList<>();
        String message = "Test message";

        // Act
        controller.sendNotificationToEntrants(eventId, emptyList, message, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("No entrants to notify", errorMessage);
            }
        });
    }

    /**
     * Test that sendNotificationToEntrants fails with null user list.
     */
    @Test
    void testSendNotificationToEntrants_NullUserList_ReturnsError() {
        // Arrange
        String eventId = "event123";
        String message = "Test message";

        // Act
        controller.sendNotificationToEntrants(eventId, null, message, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("No entrants to notify", errorMessage);
            }
        });
    }

    /**
     * Test that sendNotificationToEntrants fails with empty message.
     */
    @Test
    void testSendNotificationToEntrants_EmptyMessage_ReturnsError() {
        // Arrange
        String eventId = "event123";
        List<String> userIds = Arrays.asList("user1", "user2");
        String emptyMessage = "";

        // Act
        controller.sendNotificationToEntrants(eventId, userIds, emptyMessage, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Notification message cannot be empty", errorMessage);
            }
        });
    }

    /**
     * Test that sendNotificationToEntrants fails with null message.
     */
    @Test
    void testSendNotificationToEntrants_NullMessage_ReturnsError() {
        // Arrange
        String eventId = "event123";
        List<String> userIds = Arrays.asList("user1", "user2");

        // Act
        controller.sendNotificationToEntrants(eventId, userIds, null, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Notification message cannot be empty", errorMessage);
            }
        });
    }

    /**
     * Test that sendNotificationToEntrants fails with whitespace-only message.
     */
    @Test
    void testSendNotificationToEntrants_WhitespaceMessage_ReturnsError() {
        // Arrange
        String eventId = "event123";
        List<String> userIds = Arrays.asList("user1");
        String whitespaceMessage = "   ";

        // Act
        controller.sendNotificationToEntrants(eventId, userIds, whitespaceMessage, new OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed");
            }

            @Override
            public void onError(String errorMessage) {
                // Assert
                assertEquals("Notification message cannot be empty", errorMessage);
            }
        });
    }

    // ========== HELPER METHODS ==========

    /**
     * Create a mock Event for testing.
     */
    private Event createMockEvent() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn("event123");
        when(event.getName()).thenReturn("Test Event");
        when(event.getPosterImageUrl()).thenReturn("base64image");
        when(event.getWaitlistEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getSelectedEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getAcceptedEntrantIds()).thenReturn(new ArrayList<>());
        when(event.getCancelledEntrantIds()).thenReturn(new ArrayList<>());
        return event;
    }

    /**
     * Create a list of mock Users for testing.
     */
    private List<User> createMockUsers(List<String> userIds) {
        List<User> users = new ArrayList<>();
        for (String userId : userIds) {
            User user = mock(User.class);
            when(user.getId()).thenReturn(userId);
            when(user.getName()).thenReturn("User " + userId);
            when(user.getEmail()).thenReturn(userId + "@example.com");

            // Mock RegisteredEvent with timestamp
            User.RegisteredEvent regEvent = mock(User.RegisteredEvent.class);
            when(regEvent.getEventId()).thenReturn("event123");
            when(regEvent.getRegisteredDate()).thenReturn(new Timestamp(new Date()));
            when(regEvent.getStatus()).thenReturn(Status.Pending);

            when(user.getRegEvents()).thenReturn(Arrays.asList(regEvent));

            users.add(user);
        }
        return users;
    }
}