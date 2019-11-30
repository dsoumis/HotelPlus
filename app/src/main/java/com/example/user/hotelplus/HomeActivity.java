package com.example.user.hotelplus;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        layout=findViewById(R.id.Layout);
        animationDrawable= (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(1500);
        animationDrawable.start();


    }

    // Called when the user taps the SEARCH button
    public void searchForHotels(View view) {// Do something in response to button

        EditText editText = findViewById(R.id.editText); //Finds the id "editText" from activity_home.xml

        OkHttpClient client = new OkHttpClient();
        client = client.newBuilder().retryOnConnectionFailure(true).readTimeout(15, TimeUnit.SECONDS).writeTimeout(15,TimeUnit.SECONDS).
                callTimeout(15,TimeUnit.SECONDS).build();


        //editText.getText().toString()
        String urlStr = "http://dev.virtualearth.net/REST/v1/Locations?q="+"athens"+
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
}
