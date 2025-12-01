package com.hotdog.elotto.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

public class LocationController {
    //https://stackoverflow.com/questions/64951824/how-to-get-current-location-with-android-studio used for a majority of the methods
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;
    /**
     * Creates a new LocationController instance using the provided context.
     *
     * @param context the context used to access location services
     */
    public LocationController(Context context){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.context = context;
    }

    /**
     * Retrieves the current device location using fine location.
     *
     * @return a Task that coincides to the current location, or a failed Task if permissions are missing
     */
    public Task<Location> getCurrentLocation(){
        CancellationTokenSource cancellationToken = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return Tasks.forException(new SecurityException("Permission Not Granted"));
        }
        return fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.getToken());
    }
    /**
     * Callback interface for receiving coordinate results returning as a list of 2 doubles.
     */
    public interface CoordCallback{
        void onCoordReady(List<Double> coords);
    }
    /**
     * Callback interface for getting latitude and longitude values separately.
     */
    public interface LocationCallBack{
        void onLocationReady(double lat, double lon);
    }
    /**
     * Converts a Task into coordinate values and delivers them via a callback.
     *
     * @param curLocation the Task that will provide a location
     * @param callback    the callback invoked with latitude/longitude data or null if failed
     */
    public void convertToCoord(Task<Location> curLocation, CoordCallback callback){
        curLocation.addOnSuccessListener(location-> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                List<Double> coords = new ArrayList<>();
                coords.add(lat);
                coords.add(lon);
                callback.onCoordReady(coords);
            }
        }).addOnFailureListener(e->{
            callback.onCoordReady(null);
        });
    }
    /**
     * Retrieves the current latitude and longitude and provides them through LocationCallBack.
     *
     * @param callback the callback invoked with the resolved latitude and longitude
     */
    public void getLatLon(LocationCallBack callback){
        convertToCoord(getCurrentLocation(), coords ->{
            if(coords != null){
                callback.onLocationReady(coords.get(0),coords.get(1));
            }
            else{
                callback.onLocationReady(Double.NaN,Double.NaN);
            }
        });
    }


}
