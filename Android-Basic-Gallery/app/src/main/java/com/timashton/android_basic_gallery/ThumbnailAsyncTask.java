package com.timashton.android_basic_gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

/*
 * Created by Tim Ashton on 30/08/15.
 *
 * AsyncTask to get thumbnails and id's from the device.
 */
public class ThumbnailAsyncTask extends AsyncTask<Long, Void, Bitmap> {

    private final ImageView mTarget;
    private Context mContext;
    private ThumbnailTaskCallbacks mCallbacks;

    public ThumbnailAsyncTask(ImageView target, Context context) {
        mContext = context;
        mTarget = target;
        mCallbacks = (ThumbnailTaskCallbacks)mContext;
    }

    /*
    Callbacks to send the bitmap and the id back to Main Activity.
    */
    public interface ThumbnailTaskCallbacks {
        void addThumbnailToCache(long id, Bitmap thumbnail);
    }

    @Override
    protected void onPreExecute() {
        mTarget.setTag(this);
    }

    @Override
    protected Bitmap doInBackground(Long... params) {
        final long id = params[0];

        Bitmap result = MediaStore.Images.Thumbnails.getThumbnail(
                mContext.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);

        // Show broken image as placeholder
        // if there is no image or an problem with image on disk.
        if(result == null){
            result = BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.ic_broken_image_white_48dp);
        }

        mCallbacks.addThumbnailToCache(id, result);

        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
// TODO tag a null result here so that the image can be intercepted by AdapterView.OnItemClickListener
        if ((mTarget.getTag() == this)) {
            mTarget.setImageBitmap(result);
            mTarget.setTag(null);
        }
    }
}