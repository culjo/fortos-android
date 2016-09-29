package com.novugrid.fortos.datamodel;

/**
 * Created by WeaverBird on 9/27/2016.
 */
public class ImageData {

    private int imageId;
    private String url, size, caption, createdOn;

    public ImageData(int imageId, String url, String size, String caption, String createdOn) {
        this.imageId = imageId;
        this.url = url;
        this.size = size;
        this.caption = caption;
        this.createdOn = createdOn;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

}
