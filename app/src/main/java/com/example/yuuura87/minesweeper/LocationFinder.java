package com.example.yuuura87.minesweeper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


class LocationFinder implements LocationListener{

    private Activity gameActivity;
    private Location myLocation = null;
    private String address;
    private LocationManager locationManager;
    private boolean didAlreadyRequestLocationPermission;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = -100;

    public LocationFinder(Activity gameActivity) {
        this.gameActivity = gameActivity;

        didAlreadyRequestLocationPermission = false;

        locationManager = (LocationManager) gameActivity.getApplicationContext().getSystemService(LOCATION_SERVICE);
        getCurrentLocation();
    }

    private void getAddressForLocation(Context context, Location location) throws IOException {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        address = addresses.get(0).getAddressLine(0);
        // String city = addresses.get(0).getAddressLine(1);
        //String country = addresses.get(0).getAddressLine(2);
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        try {
            getAddressForLocation(gameActivity, myLocation);
        } catch (Exception e) {
            Toast.makeText(gameActivity, "error getting address.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(gameActivity, "onLocationChanged " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public String getAddress() {
        return address;
    }

    public Location getMyLocation() {
        return myLocation;
    }

    public static int getLocationPermissionRequestCode() {
        return LOCATION_PERMISSION_REQUEST_CODE;
    }

    public void onStop() {
        locationManager.removeUpdates(this);
    }

    public void getCurrentLocation() {
        boolean isAccessGranted = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
            String coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
            if (gameActivity.getApplicationContext().checkSelfPermission(fineLocationPermission) != PackageManager.PERMISSION_GRANTED ||
                    gameActivity.getApplicationContext().checkSelfPermission(coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) {
                // The user blocked the location services of THIS app / not yet approved
                isAccessGranted = false;
                if (!didAlreadyRequestLocationPermission) {
                    didAlreadyRequestLocationPermission = true;
                    String[] permissionsToAsk = new String[]{fineLocationPermission, coarseLocationPermission};
                    gameActivity.requestPermissions(permissionsToAsk, LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        } else {
            // Because the user's permissions started only from Android M and on...
            isAccessGranted = true;
        }

        if (isAccessGranted) {
            float metersToUpdate = 1;
            long intervalMilliseconds = 1000;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalMilliseconds, metersToUpdate, this);

            if (myLocation == null) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);          //GPS
                if (myLocation == null)
                    myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);     //network
            }
        }
        try {
            getAddressForLocation(gameActivity, myLocation);
        } catch (Exception e) {
            Toast.makeText(gameActivity, "error getting address.", Toast.LENGTH_SHORT).show();
        }
    }
}
