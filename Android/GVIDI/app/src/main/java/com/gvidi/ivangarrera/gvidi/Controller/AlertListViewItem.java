package com.example.ivangarrera.example.Controller;

import android.widget.ImageView;

public class AlertListViewItem {
    private String timestamp;
    private String coordinates;
    private ImageView imageView;

    public AlertListViewItem(String timestamp, String coordinates, ImageView imageView) {
        super();
        this.timestamp = timestamp;
        this.coordinates = coordinates;
        this.imageView = imageView;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public ImageView getImageView() { return imageView; }
}
