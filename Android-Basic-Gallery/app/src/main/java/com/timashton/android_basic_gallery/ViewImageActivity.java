package com.timashton.android_basic_gallery;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

/*
 * Created by Tim Ashton on 20/08/15.
 *
 * An Activity for viewing the selected picture from the gallery.
 *
 * An image uri is passed from the main activity to this activity so that a full sized image
 * can be loaded and viewed.
 */
public class ViewImageActivity extends Activity {

    private static final String TAG = ViewImageActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView mImage = (ImageView) findViewById(android.R.id.icon);

        // TODO: Make image loading async, but blocking here makes the Allocation logic simpler to understand.

        Bitmap mIn = loadBitmap(getContentResolver(), getIntent().getData());

        // If an image cannot be loaded, return to the Main Activity
        if(mIn == null){
            finish();
            return;
        }

        // Show the Image
        mImage.setImageBitmap(mIn);
    }


    /*
     * Load and return given uri as a Bitmap
     */
    public static Bitmap loadBitmap(ContentResolver resolver, Uri uri) {
        try {
            final InputStream is = resolver.openInputStream(uri);
            try {
                final BitmapFactory.Options opts = new BitmapFactory.Options();
                // Down sample to keep processing fast
                opts.inSampleSize = 4;
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeStream(is, null, opts);
            } finally {
                is.close();
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Problem reading image", e);
        }
    }
}