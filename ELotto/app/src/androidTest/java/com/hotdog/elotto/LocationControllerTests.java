package com.hotdog.elotto;

import static org.junit.Assert.assertEquals;

import android.location.Location;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.hotdog.elotto.controller.FakeLocationController;

import org.junit.Before;
import org.junit.Test;


public class LocationControllerTests {

    private FakeLocationController controller;

    @Before
    public void setUp() {
        controller = new FakeLocationController();
    }
    @Test
    public void testGetCurrentLocation_ReturnsFakeLocation() throws Exception {
        Task<Location> task = controller.getCurrentLocation(0, null);
        Location location = Tasks.await(task);

        assertEquals(41.9, location.getLatitude(), 0.001);
        assertEquals(-87.6, location.getLongitude(), 0.001);
    }
    @Test
    public void testGetLatLon_ReturnsCorrectCoordinates() {
        controller.getLatLon((lat, lon) -> {
            assertEquals(41.9, lat, 0.001);
            assertEquals(-87.6, lon, 0.001);
        });
    }
    @Test
    public void testConvertToCoord_Success() throws Exception {
        Location location = new Location("test");
        location.setLatitude(41.9);
        location.setLongitude(-87.6);
        Task<Location> task = Tasks.forResult(location);

        FakeLocationController.convertToCoord(task, coords -> {
            assertEquals(2, coords.size());
            assertEquals(41.9, coords.get(0), 0.001);
            assertEquals(-87.6, coords.get(1), 0.001);
        });
    }
    @Test
    public void testConvertToCoord_Failure() {
        Task<Location> failedTask = Tasks.forException(new Exception("fail"));
        FakeLocationController.convertToCoord(failedTask, coords -> {
            assertEquals(null, coords);
        });
    }
}
