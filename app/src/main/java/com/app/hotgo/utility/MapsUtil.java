package com.app.hotgo.utility;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MapsUtil {

    private static final String TAG = "MapsUtil";

    public static class GetLatLngByAddress extends
            AsyncTask<String, Void, LatLng> {

        private IMaps imap;

        public GetLatLngByAddress(IMaps imap) {
            this.imap = imap;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            try {
                String strAddress = params[0];
                if (strAddress.contains(" ")) {
                    strAddress = strAddress.replace(" ", "%20");
                }
                String uri = "http://maps.google.com/maps/api/geocode/json?address="
                        + strAddress + "&sensor=false";
                Log.e(TAG, "map: " + uri);
//                String uri = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
//                        + strAddress + "&key=AIzaSyBi14r5PixAY1JqroxVgBT6x6SsILf690s";


//                HttpGet httpGet = new HttpGet(uri);
//                HttpClient client = new DefaultHttpClient();
//                HttpResponse response;
//                StringBuilder stringBuilder = new StringBuilder();
//
//                try {
//                    response = client.execute(httpGet);
//                    HttpEntity entity = response.getEntity();
//                    InputStream stream = entity.getContent();
//                    int b;
//                    while ((b = stream.read()) != -1) {
//                        stringBuilder.append((char) b);
//                    }
//                } catch (Exception ex) {
//                    return new LatLng(0.0, 0.0);
//                }


                MultipartBody.Builder builderNew = new MultipartBody.Builder();
                builderNew.addFormDataPart("a","");

                RequestBody requestBody = builderNew.build();
                Request request = new Request.Builder()
                        .url(uri)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                okhttp3.Response response = null;
                String resString = "";
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        resString = response.body().string();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return new LatLng(0.0, 0.0);
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(resString);

                    double lng = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lng");

                    double lat = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lat");

                    return new LatLng(lat, lng);
                } catch (Exception ex) {
                    return new LatLng(0.0, 0.0);
                }
            } catch (Exception ex) {
                Log.e(TAG, "doInBackground: " + ex.toString());
                return new LatLng(0.0, 0.0);
            }
        }

        @Override
        protected void onPostExecute(LatLng result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                imap.processFinished(result);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /*
    * */
    public static LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                if (returnedAddress.getMaxAddressLineIndex() > 0) {
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        if (i < returnedAddress.getMaxAddressLineIndex() - 1) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                        } else {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("");
                        }

                    }
                } else {
                    strReturnedAddress.append(returnedAddress.getAddressLine(0));
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }
    /**/

    public static class GetAddressByLatLng extends
            AsyncTask<LatLng, Void, String> {

        private IMaps imap;

        public GetAddressByLatLng(IMaps imap) {
            this.imap = imap;
        }

        @Override
        protected String doInBackground(LatLng... params) {
            try {
                LatLng latLng = params[0];

                String uri = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + latLng.latitude
                        + ","
                        + latLng.longitude
                        + "&sensor=true";
                Log.e(TAG, "map: " + uri);

//                HttpParams par = new BasicHttpParams();
//                HttpProtocolParams.setVersion(par, HttpVersion.HTTP_1_1);
//                HttpProtocolParams.setContentCharset(par, "UTF-8");
//                par.setBooleanParameter("http.protocol.expect-continue", false);
//
//                HttpGet httpGet = new HttpGet(uri);
//                HttpClient client = new DefaultHttpClient(par);
//                HttpResponse response;
//
//                String strResponse = "";
//                try {
//                    response = client.execute(httpGet);
//                    HttpEntity entity = response.getEntity();
//
//                    strResponse = EntityUtils.toString(entity, HTTP.UTF_8);
//                } catch (Exception ex) {
//                    return "";
//                }

                MultipartBody.Builder builderNew = new MultipartBody.Builder();
                builderNew.addFormDataPart("a","");

                RequestBody requestBody = builderNew.build();
                Request request = new Request.Builder()
                        .url(uri)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                okhttp3.Response response = null;
                String resString = "";
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        resString = response.body().string();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }


                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(resString);

                    String address = ((JSONArray) jsonObject.get("results"))
                            .getJSONObject(0).getString("formatted_address");

                    return address;
                } catch (Exception ex) {
                    return "";
                }
            } catch (Exception ex) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                imap.processFinished(result);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static class GetCurrentLocation extends AsyncTask<Void, Void, Void>
            implements LocationListener {

        private GoogleMap maps;
        private int zoom;
        private boolean enableMyLoc, updateMyLoc;
        private Context context;

        public GetCurrentLocation(Context context, GoogleMap maps, int zoom,
                                  boolean enableMyLoc, boolean updateMyLoc) {
            this.context = context;
            this.maps = maps;
            this.zoom = zoom;
            this.enableMyLoc = enableMyLoc;
            this.updateMyLoc = updateMyLoc;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            maps.setMyLocationEnabled(enableMyLoc);

            // Move to current location
            LocationManager locManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);

            Criteria crit = new Criteria();

            String provider = locManager.getBestProvider(crit, true);

            Location loc = locManager.getLastKnownLocation(provider);

            if (loc != null) {
                onLocationChanged(loc);
            }

            // Update location
            if (updateMyLoc) {
                locManager.requestLocationUpdates(provider, 20000, 0, this);
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(),
                    location.getLongitude());

            maps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }
    }

    // Seems this method is working not correctly.
    public static class GetAddressCompletely extends
            AsyncTask<LatLng, Void, Address> {

        private IMaps imap;
        private Context context;

        public GetAddressCompletely(Context context, IMaps imap) {
            this.context = context;
            this.imap = imap;
        }

        @Override
        protected Address doInBackground(LatLng... params) {
            // TODO Auto-generated method stub
            LatLng latLng = params[0];

            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(context, Locale.getDefault());

            // Here 1 represent max location result to returned, by documents it
            // recommended 1 to 5
            try {
                addresses = geocoder.getFromLocation(latLng.latitude,
                        latLng.longitude, 1);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0);
            }

            return new Address(Locale.getDefault());
        }

        @Override
        protected void onPostExecute(Address result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            imap.processFinished(result);
        }
    }
}
