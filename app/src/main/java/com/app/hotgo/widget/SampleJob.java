package com.app.hotgo.widget;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;



public class SampleJob implements ClusterItem {

    private double latitude;
    private double longitude;

//Create constructor, getter and setter here

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}