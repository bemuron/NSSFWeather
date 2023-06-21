package com.mads03.nssfweather.app;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.mads03.nssfweather.helpers.SessionManager;


public class MyApplication extends Application
        implements LifecycleObserver {

    public static final String TAG = MyApplication.class
            .getSimpleName();

    public boolean myApplicationStatus;

    public static boolean isAppInBg;

    private static MyApplication mInstance;

    private SessionManager pref;

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }


    public SessionManager getPrefManager() {
        if (pref == null) {
            pref = new SessionManager(this);
        }

        return pref;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connectListener() {
        Log.d(TAG, "resumed observing lifecycle.");
        mInstance.myApplicationStatus = true;
        isAppInBg = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disconnectListener() {
        Log.d(TAG, "paused observing lifecycle.");
        mInstance.myApplicationStatus = false;
        isAppInBg = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        Log.d(TAG, "Returning to foreground…");
        isAppInBg = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        Log.d(TAG, "Moving to background…");
        isAppInBg = true;
    }

}
