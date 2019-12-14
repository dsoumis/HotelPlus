package com.example.user.hotelplus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.user.hotelplus.Constants.bingapikey;

//Remove this import and use your own Bing Api Key

public class HomeActivity extends AppCompatActivity {
    ConstraintLayout layout;
    AnimationDrawable animationDrawable;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private void requestLocationPermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == 10) {
            if (permissions.length > 0 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Second argument is how often to refresh the location
                //Third argument is how much the user should have moved in meters to refresh the location
                try {
                    locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                } catch (SecurityException e) {
                    Log.d("Security", "" + e);
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        layout = findViewById(R.id.Layout);
        animationDrawable = (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(1500);
        animationDrawable.start();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Corrd", "" + location.getLatitude() + " " + location.getLongitude());

                OkHttpClient client = new OkHttpClient();
                client = client.newBuilder().retryOnConnectionFailure(true).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).
                        callTimeout(60, TimeUnit.SECONDS).build();
                String urlStr = "http://dev.virtualearth.net/REST/v1/Locations/" + location.getLatitude() + "," + location.getLongitude() + "?o=json&key=" + bingapikey;
                Request request = new Request.Builder().url(urlStr).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d("Network_error_bing", "Message of fault: " + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        //Note to self: response.body().string() can be consumed only once. Calling it twice will give a FATAL EXCEPTION: OkHttp Dispatcher
                        String area = response.body().string();
                        Log.d("bingy", area);
                        try {
                            JSONObject jsonResult = new JSONObject(area);
                            JSONArray resourceSets = jsonResult.getJSONArray("resourceSets");
                            JSONObject temp = resourceSets.getJSONObject(0);
                            final JSONArray resources = temp.getJSONArray("resources");
                            final JSONObject innerObject = resources.getJSONObject(0);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    EditText editText = findViewById(R.id.editText); //Finds the id "editText" from activity_home.xml
                                    try {
                                        editText.setText(innerObject.getString("name"));
                                    } catch (JSONException e) {
                                        Log.d("Json Exception", "" + e);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            Log.d("Json Exception", "" + e);
                        }
                    }
                });
                //To stop continuous tracking
                locationManager.removeUpdates(locationListener);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Second argument is how often to refresh the location
            //Third argument is how much the user should have moved in meters to refresh the location
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        } else {
            requestLocationPermission();
        }


    }

    // Called when the user taps the SEARCH button
    public void searchForHotels(View view) {// Do something in response to button

        EditText editText = findViewById(R.id.editText); //Finds the id "editText" from activity_home.xml

        OkHttpClient client = new OkHttpClient();
        client = client.newBuilder().retryOnConnectionFailure(true).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).
                callTimeout(60, TimeUnit.SECONDS).build();


        String urlStr = "http://dev.virtualearth.net/REST/v1/Locations?q=" + editText.getText().toString() +
                "&o=json&key=" + bingapikey;
        Request request = new Request.Builder().url(urlStr).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Network_error_bing","Message of fault: "+Log.getStackTraceString(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //Note to self: response.body().string() can be consumed only once. Calling it twice will give a FATAL EXCEPTION: OkHttp Dispatcher
                String area = response.body().string();
                Log.d("bing",area);
                Intent intent = new Intent(HomeActivity.this, ListOfAreasActivity.class); //Used to pass values from HomeActivity(this) to ListOfAreasActivity
                intent.putExtra("area", area); //Extra is a pair with key area and value message
                startActivity(intent);
            }
        });
    }

    public void aboutFun(View view) {// Do something in response to button
        startActivity(new Intent(HomeActivity.this, AboutActivity.class));
    }
}
