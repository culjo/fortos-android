/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.utils;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * For working with dates
 * Created by sabre on 10/05/2016.
 */
public class DateHelper {
    long dateInMilli;

    // TODO: 8/24/2016 This is not completed try to complete it please
    private String millisToString(long dateInMillis){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return format.format(date);
    }

    public static String currentDateTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        Log.e("CURRENT_DATE", "today: " + format.format(date));
        return format.format(date);
    }

    public long stringToDate(String dateInString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(dateInString);
            dateInMilli = date.getTime();
            return dateInMilli;

        } catch (ParseException e){
            return 0;
        }
    }

    public String getDayName(long dateInMillis){

        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.
        Time t = new Time();
        t.setToNow();

        long currentTime = System.currentTimeMillis();

        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, t.gmtoff);

        if (julianDay == currentJulianDay) {
            // Date d = new Date(dateInMillis);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            return format.format(dateInMillis);

        } else if ( julianDay == currentJulianDay -1 ){
            return "yesterday";
        } else if(julianDay == currentJulianDay - 2){

            return "2 days ago";
        }else  if(julianDay == currentJulianDay - 3){
            return "3 days ago";
        }
        else  if(julianDay == currentJulianDay - 4 ){
            return "4 days ago";
        }else if(julianDay == currentJulianDay - 5){
            return "5 days ago";
        }
        else {
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMMM dd");
            return dayFormat.format(dateInMillis);
        }


    }

    public  String getDayName(Context context, String stringDate) {

        Long dateInMillis = stringToDate(stringDate);
        return getDayName(dateInMillis);
    }


}
