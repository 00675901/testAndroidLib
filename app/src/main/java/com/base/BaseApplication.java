package com.base;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by ggyyer on 16/2/25.
 */
public class BaseApplication extends Application {
    public static RequestQueue requestQueue;

    @Override
    public void onCreate(){
        super.onCreate();

        requestQueue = Volley.newRequestQueue(this);
    }
}
