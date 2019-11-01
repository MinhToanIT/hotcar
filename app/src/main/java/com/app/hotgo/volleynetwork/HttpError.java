package com.app.hotgo.volleynetwork;

import com.android.volley.VolleyError;


public interface HttpError {
    void onHttpError(VolleyError volleyError);
}
