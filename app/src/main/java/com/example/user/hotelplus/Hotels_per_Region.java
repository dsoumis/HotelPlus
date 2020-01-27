package com.example.user.hotelplus;

import org.json.JSONArray;

public class Hotels_per_Region {
    private int _id;
    private double min_lat,min_lng,max_lat,max_lng; //Bounding box
    private String hotels; //Hotels in bounding box (remember to cast it to JSONARRAY)
    public Hotels_per_Region(int id,double given_min_lat,double given_min_lng,double given_max_lat,double given_max_lng, String given_hotels){
        this._id =id;
        this.min_lat=given_min_lat;
        this.min_lng=given_min_lng;
        this.max_lat=given_max_lat;
        this.max_lng=given_max_lng;
        this.hotels=given_hotels;
    }
    public Hotels_per_Region(double given_min_lat,double given_min_lng,double given_max_lat,double given_max_lng, String given_hotels){
        this.min_lat=given_min_lat;
        this.min_lng=given_min_lng;
        this.max_lat=given_max_lat;
        this.max_lng=given_max_lng;
        this.hotels=given_hotels;
    }
    public int getID(){
        return this._id;
    }
    public String getHotels(){
        return this.hotels;
    }
    public double getMin_lat(){
        return this.min_lat;
    }
    public double getMin_lng(){
        return this.min_lng;
    }
    public double getMax_lat(){
        return this.max_lat;
    }
    public double getMax_lng(){
        return this.max_lng;
    }
}
