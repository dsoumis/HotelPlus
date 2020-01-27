package com.example.user.hotelplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ListOfAreasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listofareas);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String area = intent.getStringExtra("area");



        ListView listview = findViewById(R.id.listview);
        ArrayList<String> arrayList = new ArrayList<>();

        try {
            JSONObject jsonResult = new JSONObject(area);
            JSONArray resourceSets = jsonResult.getJSONArray("resourceSets");
            Log.d("arrea", String.valueOf(resourceSets.getJSONObject(0)));
            JSONObject temp = resourceSets.getJSONObject(0);
            final JSONArray resources = temp.getJSONArray("resources");
            for(int i=0; i<resources.length(); ++i) {
                JSONObject innerObject = resources.getJSONObject(i);
                if (innerObject.has("name")) {
                    if (innerObject.has("address")){
                        //Add to list the options which match user's search (only name and country)
                        arrayList.add(innerObject.getString("name")+" | Country: "+innerObject.getJSONObject("address").getString("countryRegion"));
                    }
                }
            }

            //Returns a view for each object in a collection of data objects you provide
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,arrayList);

            listview.setAdapter(arrayAdapter);

            //When user taps on an area, latitude and longtitude are saved for use in the map
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        double lng,lat;

                        JSONObject userChoice = resources.getJSONObject(i);

                        //Used to pass values from ListOfAreasActivity to MapActivity
                        Intent intent = new Intent(ListOfAreasActivity.this, MapActivity.class);
                        if (userChoice.has("point")){
                            JSONArray coords = userChoice.getJSONObject("point").getJSONArray("coordinates");
                            lat = coords.getDouble(0);
                            lng = coords.getDouble(1);

                            intent.putExtra("lat", lat);
                            intent.putExtra("lng", lng);
                        }
                        if (userChoice.has("name")) {
                            String name = userChoice.getString("name");
                            intent.putExtra("name", name);
                        }
                        startActivity(intent);
                    }catch (JSONException e){
                        Log.d("JSON ERROR","Message of fault: "+Log.getStackTraceString(e));
                    }

                }
            });


        }catch (JSONException e){
            Log.d("JSON ERROR","Message of fault: "+Log.getStackTraceString(e));
        }




        // Capture the layout's TextView and set the string as its text
//        TextView textView = findViewById(R.id.textView);
//        textView.setText(message);

    }
}
