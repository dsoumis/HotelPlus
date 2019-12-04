package com.example.user.hotelplus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int LOCATION_PERMISSION_CODE = 1;

    private double location_lat, location_lng;

    private JSONArray hotels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent listofareas_intent = getIntent();
        location_lat = listofareas_intent.getDoubleExtra("lat", 0);
        location_lng = listofareas_intent.getDoubleExtra("lng", 0);
        Log.d("Map", "Message " + location_lat + " " + location_lng);


        //Our database where the regions with hotels are saved
        final DatabaseHandler db = new DatabaseHandler(this);

        Hotels_per_Region temp = db.getHotelsOfRegion(location_lat, location_lng);
        Log.d("Map", temp.getHotels());

        try {
            JSONObject jsonResult = new JSONObject(temp.getHotels());
            hotels = jsonResult.getJSONArray("elements");
        } catch (JSONException e) {
            Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
        }
    }

    private void requestLocationPermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (permissions.length > 0 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings mapUiSettings = mMap.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(true);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestLocationPermission();
        }

        try {
            for (int i = 0; i < hotels.length(); ++i) {

                JSONObject hotel_info = hotels.getJSONObject(i);
                MarkerOptions hotel_marker = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(hotel_info.getString("lat")), Double.parseDouble(hotel_info.getString("lon"))))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_hotel));
                if (hotel_info.getJSONObject("tags").has("name"))
                    hotel_marker.title(hotel_info.getJSONObject("tags").getString("name"));
                else
                    hotel_marker.title("Name is not provided");
                if (hotel_info.getJSONObject("tags").has("website"))
                    hotel_marker.snippet(hotel_info.getJSONObject("tags").getString("website"));

                mMap.addMarker(hotel_marker);
            }
        } catch (JSONException e) {
            Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
        }


        LatLng athens = new LatLng(location_lat, location_lng);
//        mMap.addMarker(new MarkerOptions().position(athens).title("Marker in Athens").snippet("Population: 4,137,400")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_hotel)));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(-34.1, 151)).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(athens, 14.0f));
    }
}
