package com.app.hotgo.googledirections;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.AsyncTask;

public abstract class AbstractRouting<T> extends AsyncTask<T, Void, Route> {
	
	protected ArrayList<RoutingListener> _aListeners;
    protected TravelMode _mTravelMode;

    protected static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";

    public enum TravelMode {
        BIKING("biking"),
        DRIVING("driving"),
        WALKING("walking"),
        TRANSIT("transit");

        protected String _sValue;

        private TravelMode(String sValue) {
            this._sValue = sValue;
        }

        protected String getValue() {
            return _sValue;
        }
    }


    public AbstractRouting(TravelMode mTravelMode) {
        this._aListeners = new ArrayList<RoutingListener>();
        this._mTravelMode = mTravelMode;
    }

    public void registerListener(RoutingListener mListener) {
        _aListeners.add(mListener);
    }
    

    protected void dispatchOnStart() {
        for (RoutingListener mListener : _aListeners) {
            mListener.onRoutingStart();
        }
    }

    protected void dispatchOnFailure() {
        for (RoutingListener mListener : _aListeners) {
            mListener.onRoutingFailure();
        }
    }

    protected void dispatchOnSuccess(PolylineOptions mOptions, Route route) {
        for (RoutingListener mListener : _aListeners) {
            mListener.onRoutingSuccess(mOptions, route);
        }
    }

    /**
     * Performs the call to the google maps API to acquire routing data and
     * deserializes it to a format the map can display.
     *
     * @param aPoints
     * @return
     */
    @Override
    protected Route doInBackground(T... aPoints) {
        for (T mPoint : aPoints) {
            if (mPoint == null) return null;
        }

        return new GoogleParser(constructURL(aPoints)).parse();
    }

    protected abstract String constructURL(T... points);

    @Override
    protected void onPreExecute() {
        dispatchOnStart();
    }

    @Override
    protected void onPostExecute(Route result) {
        if (result == null) {
            dispatchOnFailure();
        } else {
            PolylineOptions mOptions = new PolylineOptions();

            for (LatLng point : result.getPoints()) {
                mOptions.add(point);
            }

            dispatchOnSuccess(mOptions, result);
        }
    }
}
