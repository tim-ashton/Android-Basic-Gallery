package com.timashton.android_basic_gallery;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

/*
 * Created by Tim Ashton on 8/09/15.
 *
 * This fragment is used to retain the memory cache across configuration changes
 *
 * From "Caching Bitmaps" Developer Guide
 *
 * http://developer.android.com/intl/ja/training/displaying-bitmaps/cache-bitmap.html#memory-cache
 *
 */
public class MemoryCacheRetainFragment extends Fragment {

    private static final String TAG = MemoryCacheRetainFragment.class.getName();
    private LruCache<Long, Bitmap> mMemoryCache;

    /*
    Find the existing fragment or return a new instance of a MemoryCacheRetainFragment
     */
    @NonNull
    public static MemoryCacheRetainFragment findOrCreateRetainFragment(@NonNull FragmentManager fm) {
        MemoryCacheRetainFragment fragment = (MemoryCacheRetainFragment) fm.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment = new MemoryCacheRetainFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    /*
    Returns a reference to the memory cache or null
     */
    @Nullable
    public LruCache<Long, Bitmap> getMemoryCache() {
        return mMemoryCache;
    }

    /*
    Set the memory Cache
     */
    public void setMemoryCache(@NonNull LruCache<Long, Bitmap> memoryCache) {
        mMemoryCache = memoryCache;
    }

}
