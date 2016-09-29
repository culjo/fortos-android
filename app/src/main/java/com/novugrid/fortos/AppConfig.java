package com.novugrid.fortos;

/**
 * Created by WeaverBird on 9/27/2016.
 */
public class AppConfig {

//    public static final String URL_HOST = "http://192.168.174.1/fortos/"; // default
    public static final String URL_HOST = "http://fortos.novugrid.com/"; // default

    public static final String URL_UPLOAD = URL_HOST + "api/upload.php";
    public static final String URL_IMAGES = URL_HOST + "api/image.php?action=get&app_id=1&app_key=asdfghjkl";
    public static final String URL_IMAGE_DELETE = URL_HOST + "api/image.php?action=delete&app_id=1&app_key=asdfghjkl";
    public static final String URL_PHOTO_DIR = URL_HOST + "photos/";


}
