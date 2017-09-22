package com.example.yuuura87.minesweeper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class FragmentMap extends Fragment {

    private GoogleMap googleMap;
    private MapView mMapView;
    private View mView;
    private ArrayList<Person> arr = null;
    private int cnt;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_map, container, false);

        if (isGoogleMapsInstalled()) {

            mMapView = mView.findViewById(R.id.map);
            if (mMapView != null) {
                mMapView.onCreate(null);
                mMapView.onResume();
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        setGoogleMap(googleMap);
                    }
                });

            } else {
                // Notify the user he should install GoogleMaps (after installing Google Play Services)
                FrameLayout mapsPlaceHolder = getActivity().findViewById(R.id.mapsPlaceHolder);
                TextView errorMessageTextView = new TextView(getActivity().getApplicationContext());
                errorMessageTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                errorMessageTextView.setText(R.string.missing_google_maps_error_message);
                errorMessageTextView.setTextColor(Color.RED);
                mapsPlaceHolder.addView(errorMessageTextView);
            }
        }
        return mView;
    }

    /**
     * Sets and configures the map
     *
     * @param googleMap The map
     */
    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Unmark to see the changes...

        boolean isAllowedToUseLocation = hasPermissionForLocationServices(getActivity().getApplicationContext());
        if (isAllowedToUseLocation) {
            try {
                // Allow to (try to) set
                googleMap.setMyLocationEnabled(true);
                addMarkers();
            } catch (SecurityException exception) {
                Toast.makeText(getContext(), "Error getting location.", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(getContext(), "Unable to find your location.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Location is blocked in this app.", Toast.LENGTH_SHORT).show();
        }
    }

    /*public void setCurrentPosition(double[] location) throws NullPointerException{
        googleMap.addMarker(new MarkerOptions().position(new LatLng(location[0], location[1]));
        CameraPosition Liberty = CameraPosition.builder().target(new LatLng(location[0], location[1])).zoom(16).bearing(0).tilt(45).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(Liberty));
    }*/

    public void setPosition(String name, int time, String address, double[] location) throws NullPointerException{
        googleMap.addMarker(new MarkerOptions().position(new LatLng(location[0], location[1])).title(name + ", Time: " + time).snippet("Address: " + address));
        CameraPosition Liberty = CameraPosition.builder().target(new LatLng(location[0], location[1])).zoom(16).bearing(0).tilt(45).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(Liberty));
    }

    public void setArray(ArrayList<Person> arr, int cnt) {
        this.arr = arr;
        this.cnt = cnt;
        if(googleMap != null) {
            addMarkers();
        }
    }

    public void addMarkers() {
        for(int i = 0 ; i < cnt; i++) {
            String name = arr.get(i).getName();
            String address = arr.get(i).getAddress();
            int time = arr.get(i).getTime();
            double[] location = arr.get(i).getPlace();
            googleMap.addMarker(new MarkerOptions().position(new LatLng(location[0], location[1])).title(name + ", Time: " + time).snippet("Address: " + address));
        }

    }

    public static boolean hasPermissionForLocationServices(Context context) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Because the user's permissions started only from Android M and on...
            return true;
        }

        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // The user blocked the location services of THIS app
            return false;
        }
        return true;
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
