package com.example.user.hotelplus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int LOCATION_PERMISSION_CODE = 1;

    private double location_lat, location_lng;

    private boolean hotelsExistInDatabase = false;
    private JSONArray hotels;
    private double radius;
    private String measuringUnit = "K";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_map);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent listofareas_intent = getIntent();
        location_lat = listofareas_intent.getDoubleExtra("lat", 0);
        location_lng = listofareas_intent.getDoubleExtra("lng", 0);

        TextView area = findViewById(R.id.textView2);
        area.setText(listofareas_intent.getStringExtra("name"));

        EditText radiusT = findViewById(R.id.radiusNum);
        radiusT.setText("5");
        radius = Double.parseDouble(radiusT.getText().toString());
        //This technique allows to reload the map with markers whenever the radius is changed from the user
        radiusT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable rad) {
                if (rad.length() > 0)
                    radius = Double.parseDouble(rad.toString());
                onResume();
            }
        });

        Spinner measuringUnits = findViewById(R.id.measuringUnit);
        ArrayAdapter<String> measUnitsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.measuringUnits));
        measUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measuringUnits.setAdapter(measUnitsAdapter);

        measuringUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();

                if (selectedItem.equals("Km"))
                    measuringUnit = "K";
                else if (selectedItem.equals("Miles"))
                    measuringUnit = "M";
                else if (selectedItem.equals("Naut Miles"))
                    measuringUnit = "N";
                onResume();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Log.d("radius", "" + radius);
        Log.d("Map", "Message " + location_lat + " " + location_lng);


        //Our database where the regions with hotels are saved
        final DatabaseHandler db = new DatabaseHandler(this);

        Hotels_per_Region temp = db.getHotelsOfRegion(location_lat, location_lng);
        if (temp != null) {
            hotelsExistInDatabase = true;
            Log.d("Map", temp.getHotels());

            try {
                JSONObject jsonResult = new JSONObject(temp.getHotels());
                hotels = jsonResult.getJSONArray("elements");
            } catch (JSONException e) {
                Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
            }
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

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    private void hotelsOnMap() {
        try {
            if (hotelsExistInDatabase) {
                for (int i = 0; i < hotels.length(); ++i) {
                    JSONObject hotel_info = hotels.getJSONObject(i);

                    //Don't show the hotels that are more far than a specific radius from current region
                    if (distance(location_lat, location_lng,
                            Double.parseDouble(hotel_info.getString("lat")), Double.parseDouble(hotel_info.getString("lon")),
                            measuringUnit) > radius)
                        continue;


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
            }
        } catch (JSONException e) {
            Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
        }
    }

    private void pointsOfInterestOnMap() {
        String urlStr = "http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&" +
                "query=select+?thing+?Ftype+?typeName+?long+?lat+" +
                "where+{+?thing+<http://www.w3.org/2003/01/geo/wgs84_pos%23geometry>+?geo+.+" +
                "FILTER+(<bif:st_intersects>+(?geo,+<bif:st_point>+(23.727539,+37.983810),+100))+.+" +
                "optional+{+" +
                "?thing+a+?type+.+" +
                "VALUES+?type+{dbo:Museum}+" +
                "BIND(+\"Museum\"+as+?typeName+)+}+" +
                "optional+{+" +
                "?thing+a+?type+.+" +
                "VALUES+?type+{dbo:Pyramid}+" +
                "BIND(+\"Pyramid\"+as+?typeName+)+}+" +
                "optional+{+" +
                "?thing+a+?type+.+" +
                "VALUES+?type+{yago:Skyscraper104233124}+" +
                "BIND(+\"Skyscraper\"+as+?typeName+)+}+" +
                "optional+{+" +
                "?thing+a+?type+.+" +
                "VALUES+?type+{dbo:Park}+" +
                "BIND(+\"Park\"+as+?typeName+)+}+" +
                "optional+{+" +
                "?thing+a+?type+.+" +
                "VALUES+?type+{yago:Church103028079}+" +
                "BIND(+\"Church\"+as+?typeName+)+}+" +
                "optional+{+" +
                "?thing+geo:long+?long.+" +
                "?thing+geo:lat+?lat+}+" +
                "filter+(BOUND+(?type))+}";
        Request request = new Request.Builder().url(urlStr).addHeader("Accept", "application/sparql-results+json").build();
        OkHttpClient client = new OkHttpClient();
        client = client.newBuilder().retryOnConnectionFailure(true).readTimeout(15, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).
                callTimeout(15, TimeUnit.SECONDS).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Fault", "Message of fault: " + Log.getStackTraceString(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    Log.d("PuTSA", "Ti[pta " + result);

                    try {
                        JSONObject jsonResult = new JSONObject(result);
                        JSONArray results = jsonResult.getJSONObject("results").getJSONArray("bindings");
                        Log.d("mhkos", "Ti[pta " + results.length());
                        for (int i = 0; i < results.length(); ++i) {
                            JSONObject innerObject = results.getJSONObject(i);
                            Log.d("1prwto", "EDW " + innerObject.getJSONObject("thing").getString("value"));
                            String attractionName = innerObject.getJSONObject("thing").getString("value");
                            attractionName = attractionName.substring(attractionName.lastIndexOf("/") + 1);
                            Log.d("attt", "EDW " + attractionName);

                            final MarkerOptions attraction_marker = new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(innerObject.getJSONObject("lat").getString("value")),
                                            Double.parseDouble(innerObject.getJSONObject("long").getString("value"))));
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_hotel));

                            attraction_marker.title(attractionName);

                            attraction_marker.snippet(innerObject.getJSONObject("thing").getString("value"));

                            //running on main thread because can not add marker.
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.addMarker(attraction_marker);
                                }
                            });


                        }
                    } catch (JSONException e) {
                        Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
                    }
                }
            }
        });
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

        hotelsOnMap();
        pointsOfInterestOnMap();


        LatLng region = new LatLng(location_lat, location_lng);
//        mMap.addMarker(new MarkerOptions().position(athens).title("Marker in Athens").snippet("Population: 4,137,400")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_hotel)));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(-34.1, 151)).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(region, 14.0f));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final String website = marker.getSnippet();
                if (!TextUtils.isEmpty(website)) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                    startActivity(browserIntent);
                }
            }
        });
    }

    public void reloadMap(View view) {// Do something in response to button
        EditText radiusT = findViewById(R.id.radiusNum);
        radius = Double.parseDouble(radiusT.getText().toString());
        onResume();
        pointsOfInterestOnMap();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMap != null) { //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            mMap.clear();

            hotelsOnMap();
            pointsOfInterestOnMap();
        }
    }
}
