package com.timashton.android_basic_gallery;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity
        implements ThumbnailAsyncTask.ThumbnailTaskCallbacks, PhotoAdapter.PhotoAdapterCallbacks {

    private static final String TAG = MainActivity.class.getName();

    // TODO: Handle config changes as per docs
    // TODO: http://developer.android.com/intl/ja/training/displaying-bitmaps/cache-bitmap.html#memory-cache
    private static final int LOADER_CURSOR = 1;

    //private ThumbnailCache mCache;
    private LruCache<Long, Bitmap> mCache;
    private PhotoAdapter mAdapter;
    private GridView mGridView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get max available VM memory.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        // Create a cache
        mCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };


        mAdapter = new PhotoAdapter(this);

        mGridView = (GridView) findViewById(android.R.id.list);
        //mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setAdapter(mAdapter);

        mGridView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                // Release strong reference when a view is recycled
                final ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
                imageView.setImageBitmap(null);
            }
        });

        mGridView.setOnItemClickListener(mPhotoClickListener);

        // Kick off loader for Cursor with list of photos
        getLoaderManager().initLoader(LOADER_CURSOR, null, mCursorCallbacks);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.swapCursor(null);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.v(TAG, "onTrimMemory() with level=" + level);

        // Memory we can release here will help overall system performance, and
        // make us a smaller target as the system looks for memory

        if (level >= TRIM_MEMORY_MODERATE) { // 60
            // Nearing middle of list of cached background apps; evict our
            // entire thumbnail cache
            Log.v(TAG, "evicting entire thumbnail cache");
            mCache.evictAll();

        } else if (level >= TRIM_MEMORY_BACKGROUND) { // 40
            // Entering list of cached background apps; evict oldest half of our
            // thumbnail cache
            Log.v(TAG, "evicting oldest half of thumbnail cache");
            mCache.trimToSize(mCache.size() / 2);
        }
    }

    private AdapterView.OnItemClickListener mPhotoClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // TODO - check for null image (as tagged by async task) and return
            // TODO do not try to launch the intent if there is a broken image
            //View firstChild = ((ViewGroup)view).getChildAt(0);

            // User clicked on photo, open our viewer
            final Intent intent = new Intent(MainActivity.this, ViewImageActivity.class);
            final Uri data = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            intent.setData(data);
            startActivity(intent);
        }
    };

    private final LoaderManager.LoaderCallbacks<Cursor> mCursorCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            final String[] columns = {BaseColumns._ID};
            return new CursorLoader(MainActivity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };


    /*
    Add an item to the LRU cache
    (Straight from the documentation)
     */
    public void addBitmapToMemoryCache(Long key, Bitmap bitmap) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "addBitmapToMemoryCache() Key:" + key);
        }

        if (getBitmapFromMemCache(key) == null) {
            mCache.put(key, bitmap);
        }
    }

    /*
    Get an item from the LRU cache
    (Straight from the documentation)
     */
    public Bitmap getBitmapFromMemCache(Long key) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getBitmapFromMemCache() Key:" + key);
        }

        return mCache.get(key);
    }


    /*
    Implementation of the PhotoAdapter callback to load a new image when there is not
    one available in the LRU cache.
     */
    @Override
    public void requestNewThumbnail(ImageView imageView, long photoId) {
        // need to request a new task to an image into the image view
        final ThumbnailAsyncTask task = new ThumbnailAsyncTask(imageView, this);
        imageView.setImageBitmap(null);
        imageView.setTag(task);
        task.execute(photoId);
    }


    /*
    Implementation of the callback to add a new thumbnail to the cache when the async loader
    reads one from disk.
     */
    @Override
    public void addThumbnailToCache(long id, Bitmap thumbnail) {
        addBitmapToMemoryCache(id, thumbnail);
    }
}