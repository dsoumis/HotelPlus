package com.example.user.hotelplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Our database where the regions with hotels are saved
        final DatabaseHandler db = new DatabaseHandler(this);

        Hotels_per_Region temp = db.getHotelsOfRegion(37.983810,23.727539);//Athens
        Log.d("Hotel",temp.getHotels());


        OkHttpClient client = new OkHttpClient();
        client = client.newBuilder().retryOnConnectionFailure(true).readTimeout(15,TimeUnit.SECONDS).writeTimeout(15,TimeUnit.SECONDS).
                callTimeout(15,TimeUnit.SECONDS).build();


        String Greece_bbox = "34.9199876979,20.1500159034,41.8269046087,26.6041955909";


        String urlStr = "http://overpass-api.de/api/interpreter?data=[out:json];(node[tourism=hotel] (" +Greece_bbox+
                "););out body;";

        String[] tokens = Greece_bbox.split(",");
        final double min_lat=Double.parseDouble(tokens[0]);
        final double min_lng=Double.parseDouble(tokens[1]);
        final double max_lat=Double.parseDouble(tokens[2]);
        final double max_lng=Double.parseDouble(tokens[3]);
        Log.d("BBOX","Message : "+min_lat+" "+min_lng+" "+max_lat+" "+max_lng);



        Request request = new Request.Builder().url(urlStr).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Network_error","Message of fault: "+Log.getStackTraceString(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String result = response.body().string();
                    Log.d("PiTSA","Ti[pta "+result);
                    if(!db.regionExists(min_lat,min_lng,max_lat,max_lng)) {
                        Log.d("Here", "mpainei");
                        db.addRegionWithHotels(new Hotels_per_Region(min_lat, min_lng, max_lat, max_lng, result));
                    }
                    else{
                        Log.d("Here","already");
                    }
                    try{

                        JSONObject jsonResult = new JSONObject(result);
                        JSONArray results = jsonResult.getJSONArray("elements");
                        Log.d("PoTSA", String.valueOf(results.length()));
//                        for(int i=0; i<results.length(); ++i){
//                            JSONObject innerObject = results.getJSONObject(i);
//                            if(innerObject.getJSONObject("tags").has("name"))
//                                Log.d("PoTSA","EDW "+innerObject.getJSONObject("tags").getString("name") + i);
//                            for(Iterator it = innerObject.keys(); it.hasNext(); ) {
//
//                                String key = (String)it.next();
//                                Log.d("PloTSA",key + ":" + innerObject.get(key));
//                            }
//                        }






//                        Map hotel_value = ((Map)jsonResult.get("hotel"));
//                        // iterating hotel_value Map
//                        Iterator<Map.Entry> itr1 = hotel_value.entrySet().iterator();
//                        while (itr1.hasNext()) {
//                            Map.Entry pair = itr1.next();
//                            Log.d("POUTSA",pair.getKey() + " : " + pair.getValue());
//                        }
                    }catch (JSONException e){
                        Log.d("JSON ERROR","Message of fault: "+Log.getStackTraceString(e));
                    }
                }
            }
        });


//        urlStr = "http://dbpedia.org/sparql/?default-graph-uri=http://dbpedia.org&" +
//                "query=select+?area+?hotel+?label+?geo+" +
//                //Note to me. %23=#. Char # won't work.
//                "where+{+?hotel+<http://www.w3.org/1999/02/22-rdf-syntax-ns%23type>+<http://dbpedia.org/ontology/Hotel>+.+" +
//                "?hotel+<http://www.w3.org/2003/01/geo/wgs84_pos%23geometry>+?geo+.+" +
//                "FILTER+(<bif:st_intersects>+(?geo,+<bif:st_point>+(23.727539,+37.983810),+100))+.+" +
//                "?hotel+<http://www.w3.org/2000/01/rdf-schema%23label>+?label+.+" +
//                //Note to me. %2B=+. Char + won't work.
//                "BIND(\"Athens\"+AS+?area)+.+}";
//        request = new Request.Builder().url(urlStr).addHeader("Accept", "application/sparql-results+json").build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Log.d("POUTSA","Message of fault: "+Log.getStackTraceString(e));
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if(response.isSuccessful()){
//                    String result = response.body().string();
//                    Log.d("PuTSA","Ti[pta "+result);
//
//                    try{
//                        JSONObject jsonResult = new JSONObject(result);
//                        JSONArray results = jsonResult.getJSONObject("results").getJSONArray("bindings");
//
//                        for(int i=0; i<results.length(); ++i){
//                            JSONObject innerObject = results.getJSONObject(i);
//                            Log.d("POUTSA","EDW "+innerObject.getJSONObject("hotel").getString("value"));
//                            for(Iterator it = innerObject.keys(); it.hasNext(); ) {
//
//                                String key = (String)it.next();
//                                Log.d("POUTSA",key + ":" + innerObject.get(key));
//                            }
//                        }
//
//
//
//
//
//
//                        Map hotel_value = ((Map)jsonResult.get("hotel"));
//                        // iterating hotel_value Map
//                        Iterator<Map.Entry> itr1 = hotel_value.entrySet().iterator();
//                        while (itr1.hasNext()) {
//                            Map.Entry pair = itr1.next();
//                            Log.d("POUTSA",pair.getKey() + " : " + pair.getValue());
//                        }
//                    }catch (JSONException e){
//                        Log.d("JSON ERROR","Message of fault: "+Log.getStackTraceString(e));
//                    }
//                }
//            }
//        });




        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class); //Used to pass values from MainActivity(this) to HomeActivity
                startActivity(homeIntent); //Start the home acivity where our app takes place
                finish(); //Destructor of MainActivity
            }
        },1500); //4000ms is the time for the welcoming screen
    }
}
