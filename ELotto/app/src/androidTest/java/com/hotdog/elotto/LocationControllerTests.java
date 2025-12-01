package com.hotdog.elotto;

import static org.junit.Assert.*;

import android.location.Location;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.hotdog.elotto.controller.LocationController;

import org.junit.Test;


public class LocationControllerTests {
    @Test
    public void testConvertToCoord_success() {
        Location fakeLocation = new Location("test");
        fakeLocation.setLatitude(41.88);
        fakeLocation.setLongitude(-87.65);
        Task<Location> successTask = Tasks.forResult(fakeLocation);
        LocationController.convertToCoord(successTask, coords -> {
            assertNotNull(coords);
            assertEquals(2, coords.size());
            assertEquals(41.88, coords.get(0), 0.0001);
            assertEquals(-87.65, coords.get(1), 0.0001);
        });
    }

    @Test
    public void testConvertToCoord_failure() {
        Task<Location> failureTask = Tasks.forException(new Exception("Fake failure"));
        LocationController.convertToCoord(failureTask, coords -> {
            assertNull(coords);
        });
    }
}
