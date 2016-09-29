package com.novugrid.fortos.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.File;

/**
 * Class by the singteractive team -> yemi Adekunle
 * This method select media using intent and return no value
 * for selecting images set type = 0, for audio type = 1, for video = 2
 * The result of the selected is handle by @onActivityResult
 */
public class SelectMediaFileOnDevice {

    public static final int IMAGE_TYPE = 100;
    public static final int AUDIO_TYPE = 200;
    public static final int VIDEO_TYPE = 300;

    private Activity currentActivity;
    private Context currentContext;


    public SelectMediaFileOnDevice(Activity currentActivity, Context currentContext){
        this.currentActivity = currentActivity;
        this.currentContext = currentContext;
    }

    public Intent selectMedia(int type){

        String fileType = null;
        String title = "";
        switch (type){
            case IMAGE_TYPE:
                //media to select is image
                fileType = "image/*";
                title = "Select Picture";
                break;
            case AUDIO_TYPE:
                //media to select is audio
                fileType = "audio/*";
                title = "Select Audio";
                break;
            case VIDEO_TYPE:
                //media to select is video
                fileType = "video/*";
                title = "Select Video";
                break;
            default:{

            }

        }

        Intent intent = null;
        if(Build.VERSION.SDK_INT < 19){ // open up the old chooser
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);

            intent = Intent.createChooser(intent, title);
        }else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.putExtra(Intent.EXTRA_MIME_TYPES, "image/*");

        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setType(fileType);

        // Verify that the intent will resolve to an activity
        if (intent.resolveActivity(currentContext.getPackageManager()) != null) {
            return intent;

        }else{
            Snackbar.make(currentActivity.getCurrentFocus(), "Sorry No Application To handle your request ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return null;
        }

    }

    /**
     * Get the string absolute path to a uri
     * @param uri
     * @param type
     * @return
     */
    public String getAbsPathFromUri(Uri uri, int type) {

        Log.e("IMAGE_URI", uri.toString());

        String store = null;
        String path = null;
        switch (type) {
            case IMAGE_TYPE:
                store = MediaStore.Images.Media.DATA;
                break;
            case AUDIO_TYPE:
                store = MediaStore.Audio.Media.DATA;
                break;
            case VIDEO_TYPE:
                store = MediaStore.Video.Media.DATA;
                break;
        }


        if(uri != null) {
            if(Build.VERSION.SDK_INT < 19) {

                String[] projection = {store};
                //Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
                Cursor cursor = currentActivity.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {

                    // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
                    int column_index = cursor.getColumnIndexOrThrow(store);
                    cursor.moveToFirst();
                    path = cursor.getString(column_index);

                    cursor.close();
                }
            }else {// Kit Kat and Above

                String docId = DocumentsContract.getDocumentId(uri);//uri.getLastPathSegment().split(":")[1];
                final String[] imageColumns = {store};

                //ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    Log.e("IMAGE_URI_TYPE", "external Storage");

                    final String[] splitId = docId.split(":");
                    final String splitType = splitId[0];

                    if ("primary".equalsIgnoreCase(splitType)) {
                        path = Environment.getExternalStorageDirectory() + "/" + splitId[1];
                    }

                } else if(isDownloadsDocument(uri)){ //Download Provider
                    Log.e("IMAGE_URI_TYPE", "Download Documents Storage");

                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    path = getColumn(contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    Log.e("IMAGE_URI_TYPE", "Media Document Storage");

                    final String[] splitId = docId.split(":");
                    final String splitType = splitId[0];

                    Uri contentUri = null;
                    if ("image".equals(splitType)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(splitType)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(splitType)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            splitId[1]
                    };

                    path = getColumn(contentUri, selection, selectionArgs);
                }
                // MediaStore (and general)
                else if ("content".equalsIgnoreCase(uri.getScheme())) {

                    Log.e("IMAGE_URI_TYPE", "Media Store / General");

                    // Return the remote address
                    if (isGooglePhotosUri(uri))
                        path =  uri.getLastPathSegment();

                    path =  getColumn(uri, null, null);
                }
                // File
                else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    Log.e("IMAGE_URI_TYPE", "Direct File Storage");
                    path = uri.getPath();
                }

            }
        }
        Log.e("IMAGE_PATH", path);

        return path;
    }

    /**
     * Gets the path from the uri
     * @param uri
     * @param selection
     * @return
     */
    private String getColumn(Uri uri, String selection, String[] selectioinArgs){

        final String column = "_data";
        final String[] projection = { column };

        Cursor imageCursor = currentActivity.getContentResolver().query(uri, projection,
                selection, selectioinArgs, null);

        if(imageCursor != null) {
            if (imageCursor.moveToFirst()) {
                return imageCursor.getString(imageCursor.getColumnIndex(column));
            }
        }

        return null;
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public String getFileName(String filePath){
        if(filePath == null) return "";

        File file = new File(filePath);
        //Log.e("FILE SIZE", "Size in bytes " + fileLenght);
        return file.getName();
    }

    public long getFileSize(String filePath){
        return (new File(filePath)).length();
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
        Log.e("IMAGE SAMPLE SIZE", " ************** " + inSampleSize);
        return inSampleSize;

    }

    /**
     * Note : This method is not thread safe it can freez the UI thread
     * @deprecated use ImageCompressor class instead
     * @param filePath the absolute path to the image
     * @return the converted bitmap
     */
    public Bitmap getSelectedImageBitmap(String filePath){

        Bitmap imageBitmap = null;

        if(filePath != null){

            if(!filePath.isEmpty()){

                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, bitmapOptions);//This returns null just for getting bitmap properties

                /*int imageHeight = bitmapOptions.outHeight;
                int width = bitmapOptions.outWidth;*/
                /* TODO: Use this to very the type of file that want to be uploaded from the user device */
                String imageType = bitmapOptions.outMimeType;

                if(imageType.equals("image/jpeg") || imageType.equals("image/png")){
                    bitmapOptions.inSampleSize = this.calculateInSampleSize(bitmapOptions, 300, 300);//8;
                    bitmapOptions.inJustDecodeBounds = false;
                    imageBitmap = BitmapFactory.decodeFile(filePath, bitmapOptions);
                    /*imageView.setImageBitmap();
                    imageView.setVisibility(View.VISIBLE);*/
                }
            }
        }
        return imageBitmap;
    }

    public void selectedSongPreview(){

    }

}
