package com.example.sample.googlemapproject.application;

import android.app.Application;
import android.util.Log;
import java.lang.ref.WeakReference;
import com.example.sample.googlemapproject.service.ChicagoCrimeNetworkService;

public class MapApplication extends Application {

    private static final String TAG = MapApplication.class.getSimpleName();
    private static WeakReference<MapApplication> instance;

    private ChicagoCrimeNetworkService networkService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "On create of application");
        instance = new WeakReference<>(this);
        networkService = new ChicagoCrimeNetworkService();
    }

    public static MapApplication getInstance() {
        return instance.get();
    }

    public ChicagoCrimeNetworkService getChicagoCrimeNetworkService() {
        return networkService;
    }
}
