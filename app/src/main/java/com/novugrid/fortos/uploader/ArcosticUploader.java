/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.uploader;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by WeaverBird on 7/21/2016.
 */
public class ArcosticUploader extends AsyncTask<Void, Integer, String> {

    private final String TAG = ArcosticUploader.class.getSimpleName();

    private final String filePostName = "image_file";

    //ProgressBar progressBar = null;
    TextView txtPercentage;

    Map<String, String> postParams = null;

    long totalSize = 0;
    private String filePath, fileUploadURL;

    AndroidMultiPartEntity entity;

    public ArcosticUploaderResponseHandler arcosticUploaderResponseHandler;


    public ArcosticUploader(String fileUploadURL, String filePath){//, ProgressBar progressBar){

        this.fileUploadURL = fileUploadURL;
        this.filePath = filePath;
        //this.progressBar = progressBar;
    }

    public ArcosticUploader(String fileUploadURL, String filePath,
                            ArcosticUploaderResponseHandler arcosticUploaderResponseHandler){
        this.fileUploadURL = fileUploadURL;
        this.filePath = filePath;
        this.arcosticUploaderResponseHandler = arcosticUploaderResponseHandler;
    }

    /**
     * This will set the form inputs that is to go with the file to the server.
     * @param params the parameters that is to be sent to the server
     */
    public void setPostParams(Map<String, String> params){
        //params.size();
        this.postParams = params;
    }

    /**
     * Process The form data that will be sent to the server.
     */
    private void processPostParams(){

        if(this.entity != null){

            if(this.postParams != null) {
                //Get the set of entries
                Set set = this.postParams.entrySet();

                //Get an iterator
                Iterator iterator = set.iterator();

                //display or get the elements
                while (iterator.hasNext()) {

                    Map.Entry mapEntry = (Map.Entry) iterator.next();//get the next element

                    //Log.e("POST PARAMS", mapEntry.getKey().toString() + " : " + mapEntry.getValue().toString());
                    this.entity.addPart(mapEntry.getKey().toString(),
                            new StringBody(mapEntry.getValue().toString(), ContentType.TEXT_PLAIN));

                }
            }else{ Log.e("POST PARAMS", "The post params are empty"); }

        }else{
            Log.e("POST_ENTITY_NULL", "Post entity is null here");
        }

    }// end processPostParams

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected String doInBackground(Void... params) {
        return uploadFile(fileUploadURL);
    }

    @SuppressWarnings("deprecation")
    private String uploadFile(String fileUploadURL) {
        String responseString = null;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(fileUploadURL);

        try {
            // sets the progress count...
            this.entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            // publishProgress((int) ((num / (float) totalSize) * 100));
                            arcosticUploaderResponseHandler.onProgress(num, totalSize);
                        }
                    });

            if(filePath != null){ // make sure that the path exist

                File sourceFile = new File(filePath);
                Log.e(TAG, " *** THE IMAGE SELECTED SIZE : " + sourceFile.length());

                if(sourceFile.exists()) {
                    // Adding file data to http body
                    this.entity.addPart(filePostName, new FileBody(sourceFile));
                }else {
                    Log.e(TAG, " *** THE IMAGE SELECTED IS NOT A FILE");
                }
            }

            // Extra parameters if you want to pass to server
            if(this.postParams != null){
                processPostParams();
            }

            totalSize = this.entity.getContentLength();
            httppost.setEntity(this.entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
                Log.e(TAG, "Call me in do in background, hu ah hu aha aha... ");
                // Call success here but.... i don't know how to do it..

            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode;
                arcosticUploaderResponseHandler.onFailure(responseString);
            }

        } catch (ClientProtocolException e) {
            responseString = e.toString();
        } catch (IOException e) {
            responseString = e.toString();
            arcosticUploaderResponseHandler.onFailure(responseString);

        }

        return responseString;

    }


    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(result);

        Log.e("ARCOSTIC UPLOADER", "Upload file to server Response from server is: " + result);

        //this.uploadFinishedListener.onUploadFinished(result);
        arcosticUploaderResponseHandler.onSuccess(result); // i might remove this from this place into uploadfile
        arcosticUploaderResponseHandler.onFinished();

    }
}
