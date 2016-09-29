/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by WeaverBird on 4/13/2016.
 */
public class ConnectionHelper {

    private static final String TAG = "ConnectionHelper";

    public static long lastNoConnectionTs = -1;
    public static boolean isOnline = true;

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        return isConnected;
    }

    public static boolean isConnectedOrConnecting(Context context) {
        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static int getNetworkConnectionType(Context context){

        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {

            return activeInfo.getType(); /*== ConnectivityManager.TYPE_WIFI;*/

        } else {
            return -1;//Not connected
        }

    }


/*
    public static void checkNetworkConnectionType(Context context){
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if(wifiConnected) {
                Log.i(TAG, getString(R.string.wifi_connection));
            } else if (mobileConnected){
                Log.i(TAG, getString(R.string.mobile_connection));
            }
        } else {
            Log.i(TAG, getString(R.string.no_wifi_or_mobile));
        }


    }
*/

}
