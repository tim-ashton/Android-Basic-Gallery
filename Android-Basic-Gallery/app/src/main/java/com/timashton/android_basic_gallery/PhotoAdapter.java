package com.timashton.android_basic_gallery;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/*
 * Created by Tim Ashton on 30/08/15.
 *
 * This adapter shows the list of pictures in a GridView.
 *
 * It will load images from the LRU cache in main activity or request an image from the
 * async loader task if there is not one in the cache.
 */
public class PhotoAdapter extends CursorAdapter {

    PhotoAdapterCallbacks mCallbacks;
    private WeakReference<MainActivity> mainActivityWeakReference;

    public PhotoAdapter(Context context) {
        super(context, null, false);
        mainActivityWeakReference = new WeakReference<> ((MainActivity)context);
        mCallbacks = (PhotoAdapterCallbacks)context;
    }

    public interface PhotoAdapterCallbacks {
        void requestNewThumbnail(ImageView imageView, long photoId);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final long photoId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

        final ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);

        // Cancel any pending thumbnail task, since this view is now bound to a new thumbnail
        final ThumbnailAsyncTask oldTask = (ThumbnailAsyncTask) imageView.getTag();
        if (oldTask != null) {
            oldTask.cancel(false);
        }

        final Bitmap cachedResult = mainActivityWeakReference.get().getBitmapFromMemCache(photoId);
        if (cachedResult != null) {
            imageView.setImageBitmap(cachedResult);
            return;
        }

        // must be a cache miss so request an image for this ImageView
        mCallbacks.requestNewThumbnail(imageView, photoId);
    }
}