package com.base;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;

/**
 * Created by ggyyer on 16/2/25.
 */
public class BaseApplication extends Application {
    public static RequestQueue requestQueue;
    public static ImageLoader imageLoader;

    @Override
    public void onCreate(){
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);
        imageLoader = new ImageLoader(requestQueue, new Cache(this));
    }

    /**
    *网络图片缓存
    */
    static class Cache implements ImageLoader.ImageCache {
        DiskBasedCache mCache;

        public Cache(Context context) {
            mCache = new DiskBasedCache(context.getExternalCacheDir(), 100*1024*1024);
        }

        @Override
        public Bitmap getBitmap(String url) {
            com.android.volley.Cache.Entry entry = mCache.get(url);
            if(entry != null) {
                return BitmapFactory.decodeByteArray(entry.data, 0, entry.data.length);
            }
            return null;
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            com.android.volley.Cache.Entry entry = new com.android.volley.Cache.Entry();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
            entry.data = bos.toByteArray();
            mCache.put(url, entry);
        }
    }
}
