package com.hotdog.elotto.controller;

import android.location.Location;

import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
/**
 * A fake locationcontroller class used soley for testing
 */
public class FakeLocationController {

    /**
     * Mimics the real getCurrentLocation method.
     * Ignores priority and token and just returns a fixed location.
     */
    public Task<Location> getCurrentLocation(int priority, CancellationToken token) {
        Location fakeLocation = new Location("fake");
        fakeLocation.setLatitude(41.9);
        fakeLocation.setLongitude(-87.6);

        return Tasks.forResult(fakeLocation);
    }

    /**
     *Callback for receiving coordinate results returning as a list of 2 doubles.
     */
    public interface CoordCallBack {
        void onCoordReady(List<Double> coords);
    }

    /**
     * Callback for getting latitude and longitude values separately.
     */
    public interface LocationCallBack {
        void onLocationReady(double lat, double lon);
    }

    /**
     * Converts a Task<Location> into coordinates via callback.
     */
    public static void convertToCoord(Task<Location> curLocation, CoordCallBack callback) {
        curLocation.addOnSuccessListener(location -> {
            if (location != null) {
                List<Double> coords = new ArrayList<>();
                coords.add(location.getLatitude());
                coords.add(location.getLongitude());
                callback.onCoordReady(coords);
            } else {
                callback.onCoordReady(null);
            }
        }).addOnFailureListener(e -> callback.onCoordReady(null));
    }

    /**
     * Retrieves latitude and longitude and returns them via callback.
     */
    public void getLatLon(LocationCallBack callback) {
        convertToCoord(getCurrentLocation(0, null), coords -> {
            if (coords != null) {
                callback.onLocationReady(coords.get(0), coords.get(1));
            } else {
                callback.onLocationReady(Double.NaN, Double.NaN);
            }
        });
    }
}
