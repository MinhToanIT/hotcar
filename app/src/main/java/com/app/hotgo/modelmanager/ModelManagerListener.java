package com.app.hotgo.modelmanager;

public interface ModelManagerListener {
    //public void onError(VolleyError error);
    public void onError();

    public void onSuccess(String json);
}
