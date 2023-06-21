package com.mads03.nssfweather.data.network.api;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keep track of all pending network requests and set {@link androidx.lifecycle.LiveData} "loading"
 * to true when there remaining pending requests and false when all requests have been responded to.
 *
 * LiveData Object "loading" is used to show a progress bar in the UI.
 *
 * TODO(cassigbe@): Improve Pending requests count according to http/b/199924571.
 *
 */
public class PendingRequestCounter {
    private static final String TAG = "RequestCounter";
    private final MutableLiveData<Boolean> loading;

    /**
     * Track the number of pending server requests.
     */
    private AtomicInteger pendingRequestCount = new AtomicInteger();

    public PendingRequestCounter() {
        loading = new MutableLiveData<>();
    }

    /**
     * Increment request count and update loading value.
     * Must plan on calling {@link #decrementRequestCount} when the request completes.
     *
     */
    public void incrementRequestCount() {
        int newPendingRequestCount = pendingRequestCount.incrementAndGet();
        Log.i(TAG, "Pending Server Requests: " + newPendingRequestCount);
        if (newPendingRequestCount <= 0) {
            Log.w(TAG, "Unexpectedly low request count after new request: "
                    + newPendingRequestCount);
            loading.postValue(false);
        } else {
            loading.postValue(true);
        }
    }

    /**
     * Decrement request count and update loading value.
     * Must call {@link #incrementRequestCount()} each time a network call is made.
     * and call {@link #decrementRequestCount()} when the server responds to the request.
     *
     */
    public void decrementRequestCount() {
        int newPendingRequestCount = pendingRequestCount.decrementAndGet();
        Log.i(TAG, "Pending Server Requests: " + newPendingRequestCount);
        if (newPendingRequestCount < 0) {
            Log.w(TAG, "Unexpectedly negative request count: "
                    + newPendingRequestCount);
            loading.postValue(false);
        } else if (newPendingRequestCount == 0) {
            loading.postValue(false);
        }
    }

    /**
     * Live data is true when there are pending network requests.
     *
     * @return loading a LiveData object
     */
    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }
}
