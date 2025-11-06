package com.hotdog.elotto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hotdog.elotto.callback.FirestoreCallback;
import com.hotdog.elotto.callback.OperationCallback;
import com.hotdog.elotto.controller.OrganizerController;
import com.hotdog.elotto.model.Organizer;
import com.hotdog.elotto.repository.OrganizerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

public class OrganizerControllerAndRepositoryTest {
    private OrganizerController controller;
    private OrganizerRepository mockRepo;

    @BeforeEach
    void setUp() {
        mockRepo = mock(OrganizerRepository.class);
        controller = new OrganizerController(mockRepo);
    }

    @Test
    void testCreateAndGetAndDeleteOrganizer() {
        Organizer organizer = new Organizer("user123");

        doAnswer(invocation -> {
            OperationCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepo).createOrganizer(eq(organizer), any());

        doAnswer(invocation -> {
            FirestoreCallback<Organizer> callback = invocation.getArgument(1);
            callback.onSuccess(new Organizer("user123"));
            return null;
        }).when(mockRepo).getOrganizerById(eq("user123"), any());

        doAnswer(invocation -> {
            OperationCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepo).deleteOrganizer(eq("user123"), any());
        final boolean[] created = {false};
        controller.createOrganizer(organizer, new OperationCallback() {
            @Override
            public void onSuccess() { created[0] = true; }
            @Override
            public void onError(String errorMessage) { created[0] = false; }
        });
        assertTrue(created[0]);

        final Organizer[] retrieved = {null};
        controller.getOrganizer("user123", new FirestoreCallback<Organizer>() {
            @Override
            public void onSuccess(Organizer result) { retrieved[0] = result; }
            @Override
            public void onError(String errorMessage) { retrieved[0] = null; }
        });
        assertNotNull(retrieved[0]);
        assertEquals("user123", retrieved[0].getOrgID());
        final boolean[] deleted = {false};
        controller.deleteOrganizer("user123", new OperationCallback() {
            @Override
            public void onSuccess() { deleted[0] = true; }
            @Override
            public void onError(String errorMessage) { deleted[0] = false; }
        });
        assertTrue(deleted[0]);
    }

    @Test
    void testUpdateOrganizerEvents() {
        Organizer organizer = new Organizer("user456");
        doAnswer(invocation -> {
            OperationCallback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(mockRepo).updateOrganizer(eq("user456"), eq("event2"), any());

        doAnswer(invocation -> {
            FirestoreCallback<Organizer> callback = invocation.getArgument(1);
            callback.onSuccess(organizer);
            return null;
        }).when(mockRepo).getOrganizerById(eq("user456"), any());
        final boolean[] updated = {false};
        controller.updateOrganizerEvents("user456", "event2", new OperationCallback() {
            @Override
            public void onSuccess() { updated[0] = true; }
            @Override
            public void onError(String errorMessage) { updated[0] = false; }
        });
        assertTrue(updated[0]);

        final Organizer[] retrieved = {null};
        controller.getOrganizer("user456", new FirestoreCallback<Organizer>() {
            @Override
            public void onSuccess(Organizer result) { retrieved[0] = result; }
            @Override
            public void onError(String errorMessage) { retrieved[0] = null; }
        });
        assertNotNull(retrieved[0]);
        assertEquals("user456", retrieved[0].getOrgID());
    }
}
