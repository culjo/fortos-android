/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.network;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.novugrid.fortos.AppController;

import java.util.Map;

/**
 * Created by WeaverBird on 4/10/2016.
 */
public class ConnectWithVolley {

    private static String TAG = ConnectWithVolley.class.getSimpleName();

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    private VolleyResponseListener volleyResponseListener;
    private int responseState;
    private String responseData;


    public interface VolleyResponseListener{

        void onVolleyResponse(int responseState, String responseData, String tag);

    }


    public ConnectWithVolley() {}

    public void setVolleyResponseListener(VolleyResponseListener volleyResponseListener){
        this.volleyResponseListener = volleyResponseListener;
    }

    /**
     * Will be called when the request has been completed,
     * regardless of failure or success in the request
     */
    private void requestCompleted(String tag){
        if(volleyResponseListener != null){
            volleyResponseListener.onVolleyResponse(responseState, responseData, tag);
        }
    }

    public void makeRequest(String url, Map<String, String> params){
        makeRequest(url, params, "");
    }

    public void makeRequest(String url, final Map<String, String> params, final String tag){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseState = SUCCESS;
                        responseData = response;
                        requestCompleted(tag);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        Log.e(TAG, "Error in connecting with volley to the server");
                        responseState = FAILURE;
                        responseData = null;
                        requestCompleted(tag);
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams(){
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tag);

    }


    /**
     * @deprecated this has been deprecated use the makeRequest(String url, Map params)
     * @param url the target url it shoul connect to
     * @param params the parameters that should be sent to the server
     * @param volleySuccessListener the success listener
     */
    public void makeRequest(String url, final Map<String, String> params,
                            Response.Listener<String> volleySuccessListener){

        sendRequest(url, params, volleySuccessListener);
    }

    private void sendRequest(String url, final Map<String, String> params,
                            Response.Listener<String> volleySuccessListener){

        final boolean[] e = new boolean[1];
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,

                volleySuccessListener,

                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                Log.e("CONNECT_WITH_VOLEY", "Error in connecting");
                //SingtUtils.toastScreen(, "Could not Connect to the Internet");
                e[0] = true;
            }
        }
        ){
            @Override
            protected Map<String, String> getParams(){
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest);

    }





}
