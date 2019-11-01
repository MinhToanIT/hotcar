package com.app.hotgo.volleynetwork;


public interface HttpListener<T> {
    void onHttpResponse(T response);

}
