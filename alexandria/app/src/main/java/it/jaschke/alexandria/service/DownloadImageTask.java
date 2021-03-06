package it.jaschke.alexandria.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by saj on 11/01/15.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView mBmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.mBmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap bookCover = null;

        try {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            bookCover = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        return bookCover;
    }

    protected void onPostExecute(Bitmap result) {
        mBmImage.setImageBitmap(result);
    }
}

