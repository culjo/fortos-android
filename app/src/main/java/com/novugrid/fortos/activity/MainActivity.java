package com.novugrid.fortos.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.novugrid.fortos.AppConfig;
import com.novugrid.fortos.adapters.ImagesAdapter;
import com.novugrid.fortos.animations.CrossFadingViews;
import com.novugrid.fortos.datamodel.ImageData;
import com.novugrid.fortos.listeners.OnLoadMoreListener;
import com.novugrid.fortos.listeners.OnPopupMenuBtnClickListener;
import com.novugrid.fortos.listeners.OnUploadCompleteListener;
import com.novugrid.fortos.listeners.OnUploadProgressListener;
import com.novugrid.fortos.network.ConnectWithVolley;
import com.novugrid.fortos.network.ConnectionHelper;
import com.novugrid.fortos.services.FileUploaderService;
import com.novugrid.fortos.utils.ImageCompressor;
import com.novugrid.fortos.utils.MyUtils;
import com.novugrid.fortos.utils.SelectMediaFileOnDevice;

import com.novugrid.fortos.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnUploadCompleteListener, OnUploadProgressListener, ConnectWithVolley.VolleyResponseListener {
    private final String TAG = MainActivity.class.getSimpleName();

    private final String LOAD_TAG = "images";
    private final String LOAD_MORE_TAG = "images_more";


    ///////////////////////// File Upload Service ////////////////////////
    public FileUploaderService fileUploaderService;
    public boolean fusBound;


    EditText editUpload;
    ImageButton btnUpload;
    ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ProgressBar pageLoadingBar;

    private boolean pageLoading = true;

    private SwipeRefreshLayout swipeRefresh;
    private LinearLayoutManager linearLayoutManager;
    private Parcelable recyclerState;

    private int loadDataFromPos = 0;// from the beginning
    private int loadDataThreshold = 10;
    private int loadDataTotal = 15; // only used for a refresh

    private SelectMediaFileOnDevice selectMediaFileOnDevice;
    private ImageCompressor imageCompressor;

    private ImagesAdapter rvAdapter;
    private ArrayList<ImageData> rvDataList;

    private ConnectWithVolley connectWithVolley;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode == Activity.RESULT_OK){

            if(requestCode == SelectMediaFileOnDevice.IMAGE_TYPE){
                //call the preview image function

                Uri imageUri = data.getData();
                // For android 19(Kit Kat) and above
                if(Build.VERSION.SDK_INT >= 19){
                    int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                String imageAbsPath = selectMediaFileOnDevice.getAbsPathFromUri(imageUri, SelectMediaFileOnDevice.IMAGE_TYPE );

                //displaySelectedEventImage(imageAbsPath);
                uploadSelectedImage(imageAbsPath);

            }

        }// else the user canceled the selection of media file

    }

    public void uploadSelectedImage(String imageAbsPath){

        if(imageAbsPath.isEmpty()) return;

        //String fileName = selectMediaFileOnDevice.getFileName(imageAbsPath);
        //Toast.makeText(this, fileName + " => " + imageAbsPath, Toast.LENGTH_LONG).show();

        btnUpload.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        startFileUploadService(imageAbsPath, AppConfig.URL_UPLOAD);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Init */
        selectMediaFileOnDevice = new SelectMediaFileOnDevice(this, this);
        imageCompressor = new ImageCompressor(this);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editUpload = (EditText) findViewById(R.id.edit_caption);

        btnUpload = (ImageButton) findViewById(R.id.btn_upload);
        if(btnUpload != null) btnUpload.setOnClickListener(btnUploadClickListener);

        /* Init for list */
        rvDataList = new ArrayList<>();
        rvAdapter = new ImagesAdapter(rvDataList);

        pageLoadingBar = (ProgressBar) findViewById(R.id.progress_page_loading);


        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        /* Set up the on load more listener */
        rvAdapter.setRecyclerView(recyclerView);
        rvAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                Log.e("ONLOAD MORE", "Please Load More 4 Us");

                if(! recyclerView.isComputingLayout()) {
                    rvAdapter.addItemToEnd(null); // for the progress bar

                    loadDataTotal = loadDataFromPos = loadDataFromPos + loadDataThreshold;
                    loadDataThreshold = 6;

                    connectToGetImages(LOAD_MORE_TAG);

                }

                //todo: REM to call resetLoading here
            }
        });

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.page_refresh);
        swipeRefresh.setOnRefreshListener(onRefreshListener);

        ///////////////////////////////////////////////////////////////////
        /////////////////////////// Volley Connection //////////////////////////
        connectWithVolley = new ConnectWithVolley();
        connectWithVolley.setVolleyResponseListener(this);
        ////////////////////////////////////////////////////////////////////////

        connectToGetImages(LOAD_TAG);

    }

    @Override
    public void onPause() {
        super.onPause();
        // Save the recyclerView state here
        if(linearLayoutManager != null) {
            recyclerState = linearLayoutManager.onSaveInstanceState();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        rvAdapter.setOnPopupMenuBtnClickListener(popupMenuBtnClickListener);

        recyclerView.setAdapter(rvAdapter); // attach the adapter to the recycler view

        if( !pageLoading ) {
            displayNoConnection(false);
            if(!rvDataList.isEmpty()) {

                // restore a previous state of the recycler view..
                if(recyclerState != null) {
                    linearLayoutManager.onRestoreInstanceState(recyclerState);
                }

                displayRecyclerView();
            }else { displayXmpty(); }

        }else {

            if(!ConnectionHelper.isConnected(this)) {
                displayNoConnection(true);
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* LISTENERS */
    View.OnClickListener btnUploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //Open the media selector.. for image
            Intent intent = selectMediaFileOnDevice.selectMedia(SelectMediaFileOnDevice.IMAGE_TYPE);
            if(intent != null){
                if(ConnectionHelper.isConnected(MainActivity.this)) {
                    startActivityForResult(intent, SelectMediaFileOnDevice.IMAGE_TYPE);
                }else {
                    Toast.makeText(MainActivity.this, "Please Connect to the Internet...To Get New Testimonies", Toast.LENGTH_LONG).show();
                }

            }

        }
    };

    // Swipe refresh stuff will be here
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if(ConnectionHelper.isConnected(getApplicationContext())){

                //reset the count
                loadDataFromPos = 0;
                loadDataThreshold = 15;
                connectToGetImages(LOAD_TAG);//Connect back to the Internet

            }else {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getApplicationContext(), "Please Connect to the internet and refresh.", Toast.LENGTH_LONG).show();
            }

        }
    };

    ////////////////////////////////// popMenu Click Listener //////////////////////////////////////

    public OnPopupMenuBtnClickListener popupMenuBtnClickListener = new OnPopupMenuBtnClickListener() {
        @Override
        public void onPopupMenuBtnClick(final Object itemData, final int itemPositionInAdapter, View view) {

            if(itemData instanceof ImageData){

                final ImageData imageData = (ImageData) itemData;

                final PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.delete:
                                connectToDeleteImage("delete", imageData.getImageId());
                                rvAdapter.deleteItem(itemPositionInAdapter);
                                break;

                        }

                        return true;

                    }
                });

                popupMenu.show();

            }

        }
    };

    /* NETWORKS RESPONSE */

    private void connectToGetImages(String tag) {

        Map<String, String> params = new HashMap<>();
        params.put("fpos", String.valueOf(loadDataFromPos));//from position
        params.put("thold", String.valueOf(loadDataThreshold));//To position

        Log.e(TAG, "LOADING IMAGES FROM " + loadDataFromPos + " -- " + loadDataThreshold);

        if(ConnectionHelper.isConnected(this)) {
            connectWithVolley.makeRequest(AppConfig.URL_IMAGES, params, tag);
        }else {
            Toast.makeText(this, "Please Connect to the Internet...", Toast.LENGTH_LONG).show();
        }

    }

    private void connectToDeleteImage(String tag, int image_id){
        Map<String, String> params = new HashMap<>();
        params.put("image_id", String.valueOf(image_id));
        if(ConnectionHelper.isConnected(this)) {
            connectWithVolley.makeRequest(AppConfig.URL_IMAGE_DELETE, params, tag);
        }else {
            Toast.makeText(this, "Please Connect to the Internet... To delete image", Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onVolleyResponse(int responseState, String responseData, String tag) {

        swipeRefresh.setRefreshing(false);

        if(responseState == ConnectWithVolley.SUCCESS){
            displayNoConnection(false);
            //getDataFromDb();
            Log.e(TAG, "IMAGES RESPONSE : " + responseData);

            if(tag.equals(LOAD_TAG)) {
                processJSON(responseData);
            }

            if(tag.equals(LOAD_MORE_TAG)){
                processLoadMoreJSON(responseData);
            }

            if(tag.equals("delete")){
                MyUtils.ToastScreen(this, "Image Deleted");
            }


        }
        else { // Notify the user that the connection failed.
            Toast.makeText(this, "Could Not Connect to the Internet to get New Data", Toast.LENGTH_LONG).show();
            // display no connection
            if( rvDataList.isEmpty() ) {
                displayNoConnection(true);
            }

        }

    }

    private void processJSON(String data){

        if(data != null){
            try {

                JSONObject jsonObject = new JSONObject(data);

                if(jsonObject.getBoolean("success")){

                    JSONArray jsonArray = new JSONArray(jsonObject.getString("data"));

                    if (jsonArray.length() > 0){

                        //if(jsonArray[2] == true)
                        if( ! rvDataList.isEmpty() ){ rvDataList.clear(); /*This is very important*/ }

                        for (int i = 0; i < jsonArray.length(); i++){
                            try {

                                JSONObject oJson = jsonArray.getJSONObject(i);

                                rvDataList.add(new ImageData(
                                        oJson.getInt("image_id"),
                                        oJson.getString("image_name"),
                                        oJson.getString("size"),
                                        oJson.getString("caption"),
                                        oJson.getString("created_on")
                                ));

                            }catch (JSONException e){
                                e.printStackTrace();
                                MyUtils.ToastScreen(this, "Sorry! Failed to Get Images ..");
                            }
                        }

                        fillRecyclerViewWithData();
                        rvAdapter.loadMoreResetState();


                    }else { //Note: This is nt suppose to ever show, but can occur sha, who knows
                        if (rvDataList.isEmpty()) { displayXmpty(); }
                    }

                }else { MyUtils.ToastScreen(this, "Request Failed"); }

            } catch (JSONException e) { e.printStackTrace(); }
        }

    }/* #end processJSON */

    private void processLoadMoreJSON(String data){

        if(data != null){
            try {

                JSONObject jsonObject = new JSONObject(data);

                if(jsonObject.getBoolean("success")){

                    JSONArray jsonArray = new JSONArray(jsonObject.getString("data"));

                    if (jsonArray.length() > 0){

                        rvAdapter.deleteLastItem();
                        for (int i = 0; i < jsonArray.length(); i++){
                            try {

                                JSONObject oJson = jsonArray.getJSONObject(i);

                                rvAdapter.addItemToEnd(new ImageData(
                                        oJson.getInt("image_id"),
                                        oJson.getString("image_name"),
                                        oJson.getString("size"),
                                        oJson.getString("caption"),
                                        oJson.getString("created_on")
                                ));

                            }catch (JSONException e){ Log.e(TAG, e.getMessage()); e.printStackTrace();
                                MyUtils.ToastScreen(this, "Sorry! Failed to Get More Images ..");
                            }
                        }

                        rvAdapter.loadMoreResetState();

                    }else { rvAdapter.deleteLastItem(); }

                }else { MyUtils.ToastScreen(this, "Request Failed"); }

            } catch (JSONException e) { e.printStackTrace(); }
        }

    }

    private void fillRecyclerViewWithData(){

        if(rvDataList != null) {
            if(recyclerView != null){

                rvAdapter.notifyDataSetChanged();

                pageLoading = false;// page not loading again
                displayRecyclerView(); // show the recycler view

            }else{ Log.e("RECYCLER VIEW", "The recycler view is null"); }
        }else { Log.e(TAG + "/ recyclerDataList", " rvDataList is null Please"); }

    }


    public void displayRecyclerView(){

        if(!pageLoading) {
            CrossFadingViews.fadeIn(recyclerView, pageLoadingBar,
                    CrossFadingViews.durationResource(this, CrossFadingViews.LONG_LENGTH));
        }

    }

    private void displayXmpty(){

        pageLoading = false;
        // Display no testimonies invitations yet

        TextView xmptyData = (TextView) findViewById(R.id.xmpty_data_display);
        xmptyData.setText(R.string.xmpty);
        xmptyData.setVisibility(View.VISIBLE);

        pageLoadingBar.setVisibility(View.GONE);


    }

    private void displayNoConnection(boolean state){
        // noNetwork = true;
        // Display no testimonies invitations yet

        LinearLayout viewNoNet = (LinearLayout) findViewById(R.id.no_connection_display);

        if(state){ viewNoNet.setVisibility(View.VISIBLE); }
        else { viewNoNet.setVisibility(View.GONE); }

        //swipeRefresh.setVisibility(View.GONE); // Very Important for retry button to work
        pageLoadingBar.setVisibility(View.GONE);


    }


    private void deleteItemFromRecyclerView(){

    }

    /* THE SERVICE CODE */
    /**
     * Start the File Upload Service here
     * @param filePath the abs path to the file
     * @param locUrl the url the file will be sent to
     */
    public void startFileUploadService(String filePath, String locUrl){

        Intent intent = new Intent(this, FileUploaderService.class);
        intent.putExtra("path", filePath);
        intent.putExtra("url", locUrl);

        startService(intent);// start the service b4 binding
        bindService(intent, fileUploaderServiceConnection, Context.BIND_AUTO_CREATE);// bind to the service

    }

    ///////////////////////// Connector to the File Upload Service ///////////////////////////
    private ServiceConnection fileUploaderServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            FileUploaderService.FileUploaderServiceBinder fileUploaderServiceBinder =
                    (FileUploaderService.FileUploaderServiceBinder) service;

            fileUploaderService = fileUploaderServiceBinder.getService();
            if(fileUploaderService != null){
                fusBound = true;
                serviceConnected();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "FILE UPLOAD SERVICE DISCONNECTED");
            fusBound = false;
        }

    };

    private void serviceConnected(){
        if(fusBound){
            //Then lets set the progress and complete listener for it
            fileUploaderService.setOnUploadProgressListener(this);
            fileUploaderService.setOnUploadCompleteListener(this);

            if(fusBound) {
                fileUploaderService.displayNotification("Fortos is uploading your picture");
                fileUploaderService.setPostParams(getPostParamsData());
                fileUploaderService.startUploadingFile();
            }

        }
    }

    /* SET ALL THE POST PARAMETERS THAT WILL POSTED */
    private Map<String, String> getPostParamsData() {

        Map<String, String> postData = new HashMap<>();
        postData.put("image_caption", editUpload.getText().toString());
        return postData;
    }

    @Override
    public void onUploadComplete(String dataFromServer) {

        Log.e(TAG + "Upload", dataFromServer);

        String message = "";
        progressBar.setVisibility(View.GONE);
        btnUpload.setEnabled(true);
        editUpload.setText("");

        if(!dataFromServer.isEmpty()){
            try {

                JSONObject jsonObject = new JSONObject(dataFromServer);
                if(jsonObject.getBoolean("success")){

                    //get the data associated with the uploaded image
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    //add the item to the adapter
                    if(rvAdapter != null){
                        ImageData imageData = new ImageData(jsonObject1.getInt("image_id"),
                                jsonObject1.getString("image_name"),
                                jsonObject1.getString("size"),
                                jsonObject1.getString("caption"),
                                jsonObject1.getString("created_on")
                                );
                        rvAdapter.addItem( imageData, 0);
                        recyclerView.scrollToPosition(0);
                    }

                }else {
                    message = "Photo Upload Failed";
                }

            } catch (JSONException e) {
                message = "Upload Failed";
                e.printStackTrace();
            }
            message = "Upload Successfully";
        }else {
            message = "Upload Failed";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        //fileUploaderService.displayNotification("");

        // Unbind the service when upload is complete
        if(fusBound) {
            Log.e(TAG, "UNBIND SERVICE IN ACTIVITY");
            unbindService(fileUploaderServiceConnection);
        }

    }

    @Override
    public void onUploadProgress(final long bytesWritten, final long totalSize) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //if(progressDialog.isShowing()) { progressDialog.dismiss(); }

                long div_result = 100L * bytesWritten / totalSize;

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) div_result);

                /* Log.e("Frag", "Fragment Tot-size: " + totalSize + " , bytes: " + bytesWritten + " div_res: " +
                        div_result);// + " percent: " + percent); */
            }
        });

    }



}
