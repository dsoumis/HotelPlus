package com.example.user.hotelplus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

    String DB_PATH;
    String FILES_PATH;

    private void copyDataBase() {
        Log.d("Database", "New database is being copied to device!");
        byte[] buffer = new byte[1024];
        OutputStream myOutput = null;
        int length;
        // Open your local db as the input stream
        InputStream myInput = null;
        try {
            myInput = getAssets().open("hotelsManager");
            // transfer bytes from the inputfile to the
            // outputfile
            myOutput = new FileOutputStream(DB_PATH);
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
            Log.d("Database",
                    "New database has been copied to device!");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFiles() {
        Log.d("Files", "New files are being copied to device!");
        byte[] buffer = new byte[1024];
        OutputStream myOutput;
        int length;
        // Open your local db as the input stream
        InputStream myInput;
        String[] list;
        try {
            list = getAssets().list("");
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (file.equals("images") || file.equals("hotelsManager") || file.equals("webkit"))
                        continue;
                    Log.d("asset", file);
                    myInput = getAssets().open(file);
                    // transfer bytes from the inputfile to the
                    // outputfile
                    myOutput = new FileOutputStream(FILES_PATH + "/" + file);
                    while ((length = myInput.read(buffer)) > 0) {
                        myOutput.write(buffer, 0, length);
                    }
                    myOutput.close();
                    myOutput.flush();
                    myInput.close();
                }
            } else
                Log.d("asset", "nothing");
        } catch (IOException e) {
            Log.d("copy_exception", "Message of fault: " + Log.getStackTraceString(e));
        }

        Log.d("Files",
                "New files have been copied to device!");


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Our database where the regions with hotels are saved
        final DatabaseHandler db = new DatabaseHandler(this);

//        Hotels_per_Region temp = db.getHotelsOfRegion(37.983810,23.727539);//Athens
//        Log.d("Hotel",temp.getHotels());


        OkHttpClient client = new OkHttpClient();
        client = client.newBuilder().retryOnConnectionFailure(true)
                .readTimeout(180, TimeUnit.SECONDS)
                .connectTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .callTimeout(180, TimeUnit.SECONDS).build();

        List<String> Countries_bbox = new ArrayList<String>();
        //Countries_bbox.add("34.9199876979,20.1500159034,41.8269046087,26.6041955909"); //Greece
        Countries_bbox.add("47.2701114,5.8663153,55.099161,15.0419319"); //Germany


//        DB_PATH = getDatabasePath("hotelsManager").getPath();
//        copyDataBase();
//        FILES_PATH = getFilesDir().getPath();
//        copyFiles();
//        BBoxes b = new BBoxes();
//        Countries_bbox = b.getCountries_bboxes();




        ProgressBar pb;
        pb = findViewById(R.id.progress);
        pb.setVisibility(View.VISIBLE);
        int i = 0;
        for (final String bbox : Countries_bbox) {
            String urlStr = "http://overpass-api.de/api/interpreter?data=[out:json];(node[tourism=hotel] (" + bbox +
                    "););out body;";

            String[] tokens = bbox.split(",");
            final double min_lat = Double.parseDouble(tokens[0]);
            final double min_lng = Double.parseDouble(tokens[1]);
            final double max_lat = Double.parseDouble(tokens[2]);
            final double max_lng = Double.parseDouble(tokens[3]);
            //Log.d("BBOX", "Message : " + min_lat + " " + min_lng + " " + max_lat + " " + max_lng);

            //If bounding box already in the database, skip the area
            if (db.regionExists(min_lat, min_lng, max_lat, max_lng)) {
                i++;
                Log.d("Here", "hdh mesa " + i);
                continue;
            }

            Request request = new Request.Builder().url(urlStr).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d("Network_error", "Message of fault: " + Log.getStackTraceString(e));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        Log.d("overpass_result", "Ti[pta " + result);

                        FileOutputStream fileOutputStream = openFileOutput(bbox, MODE_PRIVATE);
                        fileOutputStream.write(result.getBytes());
                        fileOutputStream.close();

                        db.addRegionWithHotels(new Hotels_per_Region(min_lat, min_lng, max_lat, max_lng, bbox));

                        try {

                            JSONObject jsonResult = new JSONObject(result);
                            JSONArray results = jsonResult.getJSONArray("elements");
                            Log.d("overpass_result_length", String.valueOf(results.length()));

                        } catch (JSONException e) {
                            Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
                        }
                    } else {
                        Log.d("overpass_result", "Ti[pta " + response);
                    }
                }
            });
        }
        //db.close();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class); //Used to pass values from MainActivity(this) to HomeActivity
                startActivity(homeIntent); //Start the home acivity where our app takes place
                finish(); //Destructor of MainActivity
            }
        }, 1500); //1500ms is the time for the welcoming screen


    }
}
