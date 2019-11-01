package com.app.hotgo.modelmanager;

import com.android.volley.VolleyError;


public interface ManagerListener {
    public void onError(VolleyError error);

    public void onSuccess(String json);
}
