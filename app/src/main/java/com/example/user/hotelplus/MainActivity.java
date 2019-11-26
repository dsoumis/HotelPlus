package com.example.user.hotelplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Network calls should be done with threads or with a class which extends Async
//        Thread thread = new Thread(){
//            public void run(){
//                String queryStr = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
//                        "PREFIX dbo:<http://dbpedia.org/ontology/>\r\n" +
//                        "PREFIX geo:<http://www.w3.org/2003/01/geo/wgs84_pos#>\r\n" +
//                        "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\r\n" +
//                        "select ?hotel ?label ?geo\r\n" +
//                        "where {\r\n" +
//                        "?hotel rdf:type dbo:Hotel .\r\n" +
//                        "?hotel geo:geometry ?geo .\r\n" +
//                        "FILTER (<bif:st_intersects> (?geo, <bif:st_point> (23.727539, 37.983810), 100)) .\r\n" +
//                        "?hotel rdfs:label ?label .\r\n" +
//                        //"BIND(\"Athens\" AS ?area) .\n" +
//                        "}";
//                Query query = QueryFactory.create(queryStr,Syntax.syntaxARQ); //Create a SPARQL query from the given string.
//
//                QueryExecution qexec;
//                // Remote execution.
//                try{
//                    //Create a QueryExecution that will access a SPARQL service over HTTP
//                    String service = "http://dbpedia.org/sparql";
//                    qexec = QueryExecutionFactory.sparqlService(service, query);
//                    // Set the DBpedia specific timeout.
//                    //((QueryEngineHTTP)qexec).addParam("timeout", "30000") ; //Cast to QueryEngineHTTP to add addParam
//
//                    // Execute.
//                    //ResultSet:{
//                    //Results from a query in a table-like manner for SELECT queries.
//                    // Each row corresponds to a set of bindings which fulfil the conditions of the query.
//                    // Access to the results is by variable name.
//                    ResultSet rs = qexec.execSelect(); //Type of Select (could be CONSTRUCT,ASK..)}
//                    while (rs.hasNext()){
//                        QuerySolution s=rs.nextSolution();
//                        Log.d("POUTSA",s.getResource("?hotel").toString());
//                    }
//                    ResultSetFormatter.out(rs);
//                    qexec.close();
//                } catch (Exception e) {
//                    Log.d("POUTSA","Message of fault: "+Log.getStackTraceString(e));
//                    //e.printStackTrace();
//                }
//            }
//        };

        //thread.start();

        OkHttpClient client = new OkHttpClient();
        String urlStr = "http://dbpedia.org/sparql/?default-graph-uri=http://dbpedia.org&" +
                "query=select+?area+?hotel+?label+?geo+" +
                //Note to me. %23=#. Char # won't work.
                "where+{+?hotel+<http://www.w3.org/1999/02/22-rdf-syntax-ns%23type>+<http://dbpedia.org/ontology/Hotel>+.+" +
                "?hotel+<http://www.w3.org/2003/01/geo/wgs84_pos%23geometry>+?geo+.+" +
                "FILTER+(<bif:st_intersects>+(?geo,+<bif:st_point>+(23.727539,+37.983810),+100))+.+" +
                "?hotel+<http://www.w3.org/2000/01/rdf-schema%23label>+?label+.+" +
                //Note to me. %2B=+. Char + won't work.
                "BIND(\"Athens\"+AS+?area)+.+}";
        Request request = new Request.Builder().url(urlStr).addHeader("Accept", "application/sparql-results+json").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("POUTSA","Message of fault: "+Log.getStackTraceString(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String result = response.body().string();
                    Log.d("POUTSA","Ti[pta "+result);

                    try{
                        JSONObject jsonResult = new JSONObject(result);
                        JSONArray results = jsonResult.getJSONObject("results").getJSONArray("bindings");

                        for(int i=0; i<results.length(); ++i){
                            JSONObject innerObject = results.getJSONObject(i);
                            Log.d("POUTSA","EDW "+innerObject.getJSONObject("hotel").getString("value"));
                            for(Iterator it = innerObject.keys(); it.hasNext(); ) {

                                String key = (String)it.next();
                                Log.d("POUTSA",key + ":" + innerObject.get(key));
                            }
                        }






                        Map hotel_value = ((Map)jsonResult.get("hotel"));
                        // iterating hotel_value Map
                        Iterator<Map.Entry> itr1 = hotel_value.entrySet().iterator();
                        while (itr1.hasNext()) {
                            Map.Entry pair = itr1.next();
                            Log.d("POUTSA",pair.getKey() + " : " + pair.getValue());
                        }
                    }catch (JSONException e){
                        Log.d("JSON ERROR","Message of fault: "+Log.getStackTraceString(e));
                    }
                }
            }
        });


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
