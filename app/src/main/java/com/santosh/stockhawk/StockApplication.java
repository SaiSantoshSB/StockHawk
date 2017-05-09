package com.santosh.stockhawk;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class StockApplication extends Application {

    private static final String TAG = StockApplication.class.getSimpleName();
    private RequestQueue requestQueue;
    private static StockApplication mInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized StockApplication getInstance(){
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(this.requestQueue == null){
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return this.requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
}
