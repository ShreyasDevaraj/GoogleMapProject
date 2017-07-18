package com.example.sample.googlemapproject.cache;

import android.os.SystemClock;
import android.util.Log;
import android.util.LruCache;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Cache<K, V> {

    private static final String TAG = Cache.class.getSimpleName();
    private final long mExpireTime;
    private final LruCache<K, V> mCache;
    private final Map<K, Long> mExpirationTimes;

    private static Cache instance;
    private final static long expireTime = TimeUnit.DAYS.toMillis(1);

    private Cache(final long expireTime) {
        mExpireTime = expireTime;
        final int cacheSize = 1024;
        mExpirationTimes = new HashMap<>(cacheSize);
        mCache = new MyLruCache(cacheSize);
    }


    public static Cache getInstance() {
        if(instance == null) {
            instance = new Cache(expireTime);
        }
        return instance;
    }

    /**
     * returns value if found in cache or returns null if key has expired or not found
     * @param key to be searched
     * @return value if found else null
     */
    public synchronized V get(final K key) {
        final V value = mCache.get(key);
        if(value != null && elapsedRealtime() >= getExpiryTime(key)) {
            Log.d(TAG, "key not found or expired ");
            remove(key);
            return null;
        }
        Log.d(TAG, "key found returning result");
        return value;
    }

    /**
     * Inserts key and value into the map
     *
     * @param key key
     * @param value value
     * @return previous value of key if found
     */
    public synchronized V put(final K key, final V value) {
        Log.d(TAG, "Inserting into map ");
        final V oldValue = mCache.put(key, value);
        mExpirationTimes.put(key, elapsedRealtime() + mExpireTime);
        return oldValue;
    }

    private long elapsedRealtime() { // With Bill Maher
        return SystemClock.elapsedRealtime();
    }

    private long getExpiryTime(final K key) {
        final Long time = mExpirationTimes.get(key);
        if(time == null) {
            return 0;
        }
        return time;
    }

    private void removeExpiryTime(final K key) {
        mExpirationTimes.remove(key);
    }

    private V remove(final K key) {
        return mCache.remove(key);
    }


    private class MyLruCache extends LruCache<K, V> {

        MyLruCache(final int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(final boolean evicted, final K key, final V oldValue, final V newValue) {
            Cache.this.removeExpiryTime(key);
        }

        @Override
        protected int sizeOf(final K key, final V value) {
            return Cache.this.sizeOf(key, value);
        }
    }

    private int sizeOf(final K key, final V value) {
        return 1;
    }

}
