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
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;
    public LocationController(Context context){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.context = context;
    }
    public Task<Location> getCurrentLocation(){
        CancellationTokenSource cancellationToken = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return Tasks.forException(new SecurityException("Permission Not Granted"));
        }
        return fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.getToken());
    }
    public interface CoordCallback{
        void onCoordReady(List<Double> coords);
    }
    public interface LocationCallBack{
        void onLocationReady(double lat, double lon);
    }
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
