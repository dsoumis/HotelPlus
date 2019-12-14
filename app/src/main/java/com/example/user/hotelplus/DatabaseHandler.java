package com.example.user.hotelplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

//https://www.javatpoint.com/android-sqlite-tutorial Studied from here

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "hotelsManager";
    private static final String TABLE_HOTELS = "hotels_table";
    //Columns of table
    private static final String KEY_ID = "id";
    private static final String KEY_MIN_LAT = "minimum_latitude";
    private static final String KEY_MIN_LNG = "minimum_longtitude";
    private static final String KEY_MAX_LAT = "maximum_latitude";
    private static final String KEY_MAX_LNG = "maximum_longtitude";
    private static final String KEY_ARRAY = "array";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HOTELS_TABLE = "CREATE TABLE " + TABLE_HOTELS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_MIN_LAT + " REAL,"
                + KEY_MIN_LNG + " REAL,"
                + KEY_MAX_LAT + " REAL,"
                + KEY_MAX_LNG + " REAL,"
                + KEY_ARRAY + " TEXT" + ")";
        db.execSQL(CREATE_HOTELS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTELS);

        // Create tables again
        onCreate(db);
    }

    // code to add the new region with hotels
    void addRegionWithHotels(@NotNull Hotels_per_Region region) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MIN_LAT, region.getMin_lat());
        values.put(KEY_MIN_LNG, region.getMin_lng());
        values.put(KEY_MAX_LAT, region.getMax_lat());
        values.put(KEY_MAX_LNG, region.getMax_lng());
        values.put(KEY_ARRAY, region.getHotels());

        // Inserting Row
        db.insert(TABLE_HOTELS, null, values);
        //2nd argument is String containing nullColumnHack
        //db.close(); // Closing database connection
    }

    // code to get the hotels of a specific region
    Hotels_per_Region getHotelsOfRegion(double given_lat, double given_lng) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HOTELS, new String[]{KEY_ID, KEY_MIN_LAT, KEY_MIN_LNG, KEY_MAX_LAT, KEY_MAX_LNG, KEY_ARRAY},
                KEY_MIN_LAT + "<=? AND " + KEY_MAX_LAT + ">=? AND " + KEY_MIN_LNG + "<=? AND " + KEY_MAX_LNG + ">=?",
                new String[]{String.valueOf(given_lat), String.valueOf(given_lat), String.valueOf(given_lng), String.valueOf(given_lng)},
                null, null, null, null);
        if (cursor != null)
            if (!cursor.moveToFirst()) //If there are no results
                return null;


        Hotels_per_Region hotels = new Hotels_per_Region(
                Integer.parseInt(cursor.getString(0)),
                Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(2)),
                Double.parseDouble(cursor.getString(3)), Double.parseDouble(cursor.getString(4)),
                cursor.getString(5));

        cursor.close();
        //db.close();
        return hotels;
    }

    boolean regionExists(double given_min_lat, double given_min_lng, double given_max_lat, double given_max_lng) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HOTELS, new String[]{KEY_ID, KEY_MIN_LAT, KEY_MIN_LNG, KEY_MAX_LAT, KEY_MAX_LNG, KEY_ARRAY},
                KEY_MIN_LAT + "=? AND " + KEY_MAX_LAT + "=? AND " + KEY_MIN_LNG + "=? AND " + KEY_MAX_LNG + "=?",
                new String[]{String.valueOf(given_min_lat), String.valueOf(given_max_lat), String.valueOf(given_min_lng), String.valueOf(given_max_lng)},
                null, null, null, null);

        boolean regionInDb = false;
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            regionInDb = true;
        }
        //db.close();
        return regionInDb;
    }
}
