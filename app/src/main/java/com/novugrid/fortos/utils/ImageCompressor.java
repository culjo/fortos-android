/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class ImageCompressor {

    private final String TAG = ImageCompressor.class.getSimpleName();

    Context context;
    private WeakReference<ImageView> imageViewWeakReference;

    private String imageMimeType;
    private int compressQuality = 100;

    private ImageCompressorListener imageCompressorListener;

    public interface ImageCompressorListener{
        void onImageCompressFinished(String compressedFilePath, Bitmap bitmap);
    }


    /*public void setImageCompressorListener(ImageCompressorListener imageCompressorListener){
        this.imageCompressorListener = imageCompressorListener;
    }*/


    public ImageCompressor(Context context) {
        this.context = context;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

        }
        Log.e("IMAGE SAMPLE SIZE", " ******* " + inSampleSize);
        return inSampleSize;

    }

    private Bitmap compressBitmapFromPath(String filePath, int reqWidth, int reqHeight){

        Bitmap origBitmap = null, newBitmap = null;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        // First we decode with inJustDecodeBounds to check dimensions
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bitmapOptions); // This returns null
        Log.e(TAG, "ORIGINAL UNDECODED BITMAP DIMENSION: " + bitmapOptions.outWidth + " X " + bitmapOptions.outHeight);

        /////////////////////////////////////////////////////////////////////////////
        float actualWidth = (float) bitmapOptions.outWidth;
        float actualHeight = (float) bitmapOptions.outHeight;

        float actualRatio = actualWidth / actualHeight;
        float reqRatio = reqWidth / reqHeight;

        if(actualHeight > reqHeight || actualWidth > reqWidth){

            if (actualRatio < reqRatio){
                // Adjust width according to required height
                actualRatio = reqHeight / actualHeight;
                actualWidth = actualRatio * actualWidth;
                actualHeight = reqHeight;

            }else if (actualRatio > reqRatio){
                // Adjust height according to required width
                actualRatio = reqWidth / actualWidth;
                actualHeight = actualRatio * actualHeight;
                actualWidth = reqWidth;

            }else {
                actualHeight = reqHeight;
                actualWidth = reqWidth;
            }
        }

        imageMimeType = bitmapOptions.outMimeType;

        if (imageMimeType.equals("image/jpeg") || imageMimeType.equals("image/png")){

            bitmapOptions.inSampleSize = calculateInSampleSize(bitmapOptions, reqWidth, reqHeight); // problem
            bitmapOptions.inJustDecodeBounds = false;

            origBitmap = BitmapFactory.decodeFile(filePath, bitmapOptions);

            if (origBitmap != null){
                Log.e(TAG, "DECODED BITMAP SIZE: " + origBitmap.getByteCount());
                Log.e(TAG, "DECODED BITMAP DIMENSION: " + origBitmap.getWidth() + " X " + origBitmap.getHeight());

                if ( origBitmap.getWidth() > actualWidth || origBitmap.getHeight() > actualHeight) {

                    origBitmap = Bitmap.createScaledBitmap(origBitmap, Math.round(actualWidth), Math.round(actualHeight), true);

                    origBitmap = Bitmap.createBitmap(origBitmap, 0, 0, Math.round(actualWidth), Math.round(actualHeight), getImageMatrix(filePath), true);
                    //origBitmap = Bitmap.createBitmap(origBitmap, 0, 0, Math.round(actualWidth), Math.round(actualHeight), getImageMatrix(filePath), true);
                    //origBitmap.recycle();// Clean the former bitmap
                }

                Log.e(TAG, "NEW BITMAP SIZE: " + origBitmap.getByteCount());
                Log.e(TAG, "NEW BITMAP DIMENSION: " + origBitmap.getWidth() + " X " + origBitmap.getHeight());


            }

        }

        return origBitmap;


    }

    // This will become basically useless now
    private Bitmap decodeBitmapFromPath(String filePath, int reqWidth, int reqHeight){

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        // First we decode with inJustDecodeBounds to check dimensions
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bitmapOptions); // This returns null
        /////////////////////////////////////////////////////////////////////////////

        imageMimeType = bitmapOptions.outMimeType;

        if(imageMimeType.equals("image/jpeg") || imageMimeType.equals("image/png")){

            bitmapOptions.inSampleSize = this.calculateInSampleSize(bitmapOptions, reqWidth, reqHeight);//8;
            bitmapOptions.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath, bitmapOptions);
        }
        return null;
    }

    private String getExtension(String mime){
        if(mime.equals("image/png")){
            return ".png";
        }else{
            return ".jpg";
        }

    }

    private String saveBitmapToInternalStorage(Bitmap bitmap){

        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());

        File directory = contextWrapper.getDir("vpd_image_dir", Context.MODE_PRIVATE);

        File mPath = new File(directory, "vpd_temp_image" + getExtension(imageMimeType));

        OutputStream fileOutputStream = null;

        try{

            fileOutputStream = new BufferedOutputStream(new FileOutputStream(mPath));
            if(imageMimeType.equals("image/png")){
                /* todo: recheck this .compress method and why it is deforming image */
                bitmap.compress(Bitmap.CompressFormat.PNG, compressQuality, fileOutputStream);
            }else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, fileOutputStream);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return mPath.getAbsolutePath();
    }



    public void compress(final String filePath, final int reqWidth, final int reqHeight, int quality, ImageCompressorListener imageCompressorListener ){
        compress( filePath, reqWidth, reqHeight, quality, null, imageCompressorListener);
    }

    public void compress(final String filePath, final int reqWidth, final int reqHeight, final int quality,
                         ImageView imageView, ImageCompressorListener imageCompressorListener){

        if (imageCompressorListener != null){
            this.imageCompressorListener = imageCompressorListener;
        }

        if(imageView != null){
            imageViewWeakReference = new WeakReference<ImageView>(imageView); // ImageView can be garbage collected
        }

        compressQuality = quality; // set the compression quality

        long length = new File(filePath).length();

        Log.e(TAG, "Original File Size is: " + length);

        ( new AsyncTask<Void, Void, Bitmap>() {

            protected void onPreExecute() {}

            @Override
            protected Bitmap doInBackground(Void... params) {

                Log.e(TAG,  " Compressing the image: " + filePath);
                return compressBitmapFromPath(filePath, reqWidth, reqHeight);

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                //super.onPostExecute(bitmap);

                Log.e(TAG, "Finished decoding the original file to bitmap");

                if(imageViewWeakReference != null && bitmap != null){
                    final ImageView imageView1 = imageViewWeakReference.get();
                    if(imageView1 != null){
                        imageView1.setImageBitmap(null);
                        imageView1.setImageBitmap(bitmap);
                        imageView1.setVisibility(View.VISIBLE);
                    }
                }

                writeBitmapToFile(bitmap);

            }

        }).execute();

    }// #end compress()

    public void writeBitmapToFile(final Bitmap bitmap){

        ( new AsyncTask<Bitmap, Void, String>() {

            protected void onPreExecute() {}

            @Override
            protected String doInBackground(Bitmap... params) {

                Log.e(TAG,  " WRITING BITMAP TO FILE ");
                return saveBitmapToInternalStorage(params[0]);

            }

            @Override
            protected void onPostExecute(String compressedFilePath) {
                //super.onPostExecute(bitmap);

                Log.e(TAG, "Finished Writing Bitmap to file : path : " + compressedFilePath);

                long length = new File(compressedFilePath).length();

                Log.e(TAG, "Compressed File Size is: " + length);

                if(imageCompressorListener != null){
                    imageCompressorListener.onImageCompressFinished(compressedFilePath, bitmap);/* todo: try removing the bitmap from here*/
                    /* todo: imageview should be able to load bitmap from a file directly */
                }

            }

        }).execute(bitmap);


    }

    public Matrix getImageMatrix(String fileAbsPath){

        Matrix matrix = new Matrix();

        try {
            ExifInterface exifInterface = new ExifInterface(fileAbsPath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.e(TAG, "Image EXIF : " + orientation);


            if(orientation == 6){
                matrix.postRotate(90);
            }else if (orientation == 3){
                matrix.postRotate(180); //ExifInterface.ORIENTATION_ROTATE_180;
            }else if (orientation == 8){
                matrix.postRotate(270);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return matrix;
    }


}
