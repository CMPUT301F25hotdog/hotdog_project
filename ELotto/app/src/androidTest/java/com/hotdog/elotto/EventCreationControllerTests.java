package com.hotdog.elotto;

import static org.junit.jupiter.api.Assertions.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.hotdog.elotto.controller.EventCreationController;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
            public void SaveEvent(String name, String description, Date dateTime, Date openPeriod, Date closePeriod, int entrantLimit, int waitListSize, String location, double price, boolean requireGeo, String bannerUrl, ArrayList<String> tagList) {
                Event event = new Event(name, description, location, dateTime, openPeriod, closePeriod, entrantLimit, "todo");
                event.setPosterImageUrl(bannerUrl);
                event.setTagList(tagList);
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
        ArrayList<String> empty = new ArrayList<>();
        controller.SaveEvent("Test Event", "Desc", new Date(), new Date(), new Date(), 10, 5, "Location", 20.0, true, "banner",empty);

        assertTrue(fakeRepo.called);
        assertNotNull(fakeRepo.lastSavedEvent);
        assertEquals("Test Event", fakeRepo.lastSavedEvent.getName());
        assertEquals("banner", fakeRepo.lastSavedEvent.getPosterImageUrl());
    }

    @Test
    void testEncodeImageNoImage() {
        ArrayList<String> empty = new ArrayList<>();
        controller.EncodeImage("Test Event", "Desc", new Date(), new Date(), new Date(), 10, 5, "Location", 20.0, true, null,empty);

        assertTrue(fakeRepo.called);
        assertEquals("no_image", fakeRepo.lastSavedEvent.getPosterImageUrl());
    }
}
