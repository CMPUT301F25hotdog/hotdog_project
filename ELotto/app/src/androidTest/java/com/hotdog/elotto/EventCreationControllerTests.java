package com.hotdog.elotto;

import static org.junit.jupiter.api.Assertions.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.hotdog.elotto.controller.EventCreationController;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class EventCreationControllerTests {
    private Context context;
    private EventCreationController controller;


    static class FakeEventRepository extends EventRepository {
        public Event lastSavedEvent;
        public boolean called = false;

        @Override
        public void createEvent(Event event, com.hotdog.elotto.callback.OperationCallback callback) {
            called = true;
            lastSavedEvent = event;
            callback.onSuccess();
        }
    }

    private FakeEventRepository fakeRepo;

    @BeforeEach
    void setup() {
        context = ApplicationProvider.getApplicationContext();
        fakeRepo = new FakeEventRepository();
        controller = new EventCreationController(context, fakeRepo) {
            @Override
            public void SaveEvent(String currentUser, String name, String description, Date dateTime, Date openPeriod, Date closePeriod, int entrantLimit, int waitListSize, String location, double price, boolean requireGeo, String bannerUrl) {
                Event event = new Event(name, description, location, dateTime, openPeriod, closePeriod, entrantLimit, "todo");
                event.setPosterImageUrl(bannerUrl);
                fakeRepo.createEvent(event, new com.hotdog.elotto.callback.OperationCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError(String errorMessage) {}
                });
            }
        };
    }

    @Test
    void tesSaveEvent() {
        controller.SaveEvent("user123", "Test Event", "Desc", new Date(), new Date(), new Date(), 10, 5, "Location", 20.0, true, "banner");

        assertTrue(fakeRepo.called);
        assertNotNull(fakeRepo.lastSavedEvent);
        assertEquals("Test Event", fakeRepo.lastSavedEvent.getName());
        assertEquals("banner", fakeRepo.lastSavedEvent.getPosterImageUrl());
    }

    @Test
    void testEncodeImageNoImage() {
        controller.EncodeImage("user123", "Test Event", "Desc", new Date(), new Date(), new Date(), 10, 5, "Location", 20.0, true, null);

        assertTrue(fakeRepo.called);
        assertEquals("no_image", fakeRepo.lastSavedEvent.getPosterImageUrl());
    }
}
