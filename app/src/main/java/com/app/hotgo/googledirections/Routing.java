package com.app.hotgo.googledirections;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Routing extends AbstractRouting<LatLng> {
    private static final String TAG = Routing.class.getSimpleName();

    public Routing(TravelMode mTravelMode) {
        super(mTravelMode);
    }

    protected String constructURL(LatLng... points) {
        LatLng start = points[0];
        LatLng dest = points[1];

        final StringBuffer mBuf = new StringBuffer(AbstractRouting.DIRECTIONS_API_URL);
        mBuf.append("origin=");
        mBuf.append(start.latitude);
        mBuf.append(',');
        mBuf.append(start.longitude);
        mBuf.append("&destination=");
        mBuf.append(dest.latitude);
        mBuf.append(',');
        mBuf.append(dest.longitude);
        mBuf.append("&key=AIzaSyBcMRS-mBh4aukh7dMy_liYSFpOS4AC0T4");
        mBuf.append("&sensor=true&mode=");
        mBuf.append(_mTravelMode.getValue());
        Log.e(TAG, "constructURL: " + mBuf.toString());
        return mBuf.toString();
    }
}
