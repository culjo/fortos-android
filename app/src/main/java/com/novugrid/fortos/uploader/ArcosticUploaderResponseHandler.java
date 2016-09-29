/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.uploader;

/**
 * Created by WeaverBird on 7/21/2016.
 */
public interface ArcosticUploaderResponseHandler {

    void onSuccess(String serverResponse);
    void onProgress(long bytesWritten, long totalSize);
    void onFailure(String serverResponse);
    void onFinished();

}
