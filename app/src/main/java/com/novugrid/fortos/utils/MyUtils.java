package com.novugrid.fortos.utils;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by WeaverBird on 8/12/2016.
 */
public class MyUtils {

    public static void ToastScreen(Context context, String message){

//        getActivity().getApplicationContext()
        if(context == null) return;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    public static JSONArray processJSONArray(String data){

        if (data != null) {
            try {

                JSONArray jsonArray = new JSONArray(data);

                if (jsonArray.length() > 0) {
                    return jsonArray;
                }

            } catch (JSONException e) { e.printStackTrace(); }
        }

        return null;
    }


}
