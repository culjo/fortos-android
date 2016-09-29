/*
 * Copyright (c) 2016. Novugrid Technologies
 */

package com.novugrid.fortos.listeners;

/**
 * Created by WeaverBird on 4/23/2016.
 */
public interface OnUploadProgressListener {

    void onUploadProgress(long bytesWritten, long totalSize);

}
