/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.novugrid.fortos.R;
import com.novugrid.fortos.listeners.OnUploadCompleteListener;
import com.novugrid.fortos.listeners.OnUploadProgressListener;
import com.novugrid.fortos.uploader.ArcosticUploader;
import com.novugrid.fortos.uploader.ArcosticUploaderResponseHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class FileUploaderService extends Service {

    private static String TAG = FileUploaderService.class.getSimpleName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private int startId;

    //Handler that receives message from the thread
    private final class ServiceHandler extends Handler{

        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            //perform the upload here from..
            Log.e(TAG, "File Uploader Service Handler : called the performTheFileUpload()");

//            performTheFileUpload();
            performFileUpload();

            // stop the service using that startId, so that we don't stop
            // the service in the middle of handling another job
            // stopSelf(msg.arg1);

        }

    }


    private static int NOTIFICATION_ID = 1;
    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;

    ArcosticUploader arcosticUploader;

    private Map<String, String> postParams;

    private String filePath;
    private String uploadLocationUrl;// = "http://192.168.1.103:8000/arcostic/video_upload.php";
    private long fileSize;

    private boolean showNotification = false;
    private String notificationText = "Your File is Uploading...";

    Intent intentFrom;

    ///////////////////////////////////////////////////////////////////////
    OnUploadProgressListener onUploadProgressListener;
    OnUploadCompleteListener onUploadCompleteListener;

    ///////////////////////////// Sets for listeners //////////////////////////////////////
    public void setOnUploadProgressListener(OnUploadProgressListener onUploadProgressListener){
        this.onUploadProgressListener = onUploadProgressListener;
    }

    public void setOnUploadCompleteListener(OnUploadCompleteListener onUploadCompleteListener){
        this.onUploadCompleteListener = onUploadCompleteListener;
    }


    //////////////////////////////////  BINDER CLASS  /////////////////////////////////////////////////////////
    public class FileUploaderServiceBinder extends Binder {

        public FileUploaderService getService(){
            //Return this instance of DrumPlayerService so clients can call public methods
            return FileUploaderService.this;
        }

    }
    ///////////////////////////////////////////////////////////////////////////////////////////

    private  final IBinder fileUploaderServiceBinder = new FileUploaderServiceBinder();

    public FileUploaderService() {}// Constructor


    @Override
    public void onCreate() {
        // To init anything usable
        // Ref: Android docs - Services
        //the background priority so CPU-intensive work will not disrupt our UI
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        //Get the HandlerThread's Looper and use it for our handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e("ON_BIND_FILE_SERVICE", "Bind the service here");
        return fileUploaderServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "ON UNBIND INSIDE SERVICE CLASS");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("ON START_COMMAND", "The onStartCommand for file service");
        this.startId = startId;
        intentFrom = intent; // Collect the incoming intent
        return START_STICKY;

    }// #end onStartCommand()

    @Override
    public void onDestroy() {
        super.onDestroy();
        //////////////////////////////////////////////////////////////////////////////////
        //*****************UPDATE TO NOTIFICATION MANAGER*******************************//
        //////////////////////////////////////////////////////////////////////////////////

//        mBuilder.setContentText("Upload complete")
//                // Removes the progress bar
//                .setProgress(0, 0, false);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.e(TAG, "Upload service has been destroyed");
    }


    //////////////////////////  NOTIFICATION MANAGER  //////////////////////////////////////////
    private void setUpNotification(){

        if(showNotification) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("Fortos is Uploading")
                    .setContentText(notificationText)
                    .setSmallIcon(R.mipmap.ic_launcher);// TODO: 4/28/2016 set icon to app icon
        }

    }

    /**
     * This will update the notification messages and info that is displayed
     * @param title the new title for the notification
     * @param contentText the text description for the notification
     */
    private void notifyNotification(String title, String contentText){

        if(showNotification) {
            mBuilder.setContentTitle(title) // "Arcostic Upload Complete")
                    .setContentText(contentText) // "Upload completed successfully")
                    // Removes the progress bar
                    .setProgress(0, 0, false);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

    }


    public void displayNotification(String msg){
        displayNotification(true, msg);
    }
    public void displayNotification(Boolean display){
        displayNotification(false, "");
    }
    private void displayNotification(boolean display, String msg){
        showNotification = display;
        notificationText = msg;
    }
    ////////////////////// END NOTIFICATION MANAGER ////////////////////////////

    /**
     * this set the file path of the file that we will be uploading.
     * @param intent the intent that was passed from the activiy that called this service
     */
    private void setFilePaths(Intent intent){

        if(intent != null && intent.hasExtra("path") && intent.hasExtra("url")){
            filePath = intent.getStringExtra("path");
            uploadLocationUrl = intent.getStringExtra("url");

        }

    }

    /**
     * This is use to set additional
     * @param pParams
     */
    public void setPostParams(Map<String, String> pParams){
        this.postParams = pParams;
    }


    // This will be called from the component (Activity) that need to upload a file.
    public void startUploadingFile(){

        setFilePaths(intentFrom);

        if (filePath == null || uploadLocationUrl.isEmpty()) return;// kill the function from running
        ///////////////////////////////////////////////////////////////////////////////////////////////

        setUpNotification(); //set up the notification


        ////////////////// Send it to the Thread
        ///////////////// send message to the thread ////////////////
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        /////////////////////////////////////////////////////////////


    }// #end startUploadingFile


    private void performFileUpload(){

        arcosticUploader = new ArcosticUploader(uploadLocationUrl, filePath,
                new ArcosticUploaderResponseHandler() {
            @Override
            public void onSuccess(String serverResponse) {
                //send a callback to the activity calling the uploader
                Log.e(TAG, "Success Response From File Upload : " + serverResponse);

                // TODO: 4/29/2016 Call a broadcast receiver here.
                // todo: Break down the JSON here please
                notifyNotification("Fortos", "File Upload Completed" );

                if (onUploadCompleteListener != null){
                    onUploadCompleteListener.onUploadComplete(serverResponse);
                }
                //Toast.makeText(getApplicationContext(), new String(bytes), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                // Log.e(TAG, "Service Tot-size: " + totalSize + " , bytes: " + bytesWritten);

                // Displays the progress bar in notification.
                if(showNotification) {
                    mBuilder.setProgress((int) totalSize, (int) bytesWritten, false);
                    mBuilder.setTicker("Upload started");
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                }

                /////////////////////// Update Progress on Activity ////////////////////
                if(onUploadProgressListener != null){
                    onUploadProgressListener.onUploadProgress(bytesWritten, totalSize);
                }
                ////////////////////////////////////////////////////////////////////////

            }

            @Override
            public void onFailure(String serverResponse) {
                // TODO: 7/19/2016 make this callback very robust for the failure of a file..
                notifyNotification("Arcostic Upload Failed", "Sorry! Your file upload has failed... ");
            }

            @Override
            public void onFinished() {
                Log.e(TAG, "On finish of Android async................");
                stopSelf();
            }
        });

        arcosticUploader.setPostParams(postParams);
        arcosticUploader.execute();

    }

    public void cancelUpload(){

    }

}
