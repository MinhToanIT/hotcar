package com.app.hotgo.object;

import com.google.android.gms.maps.model.Marker;


public class DriverUpdate {
    private String id;
    private String shopName;
    private Marker marker;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
