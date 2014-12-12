package com.example.davelkan.mapv2;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

public class Node {
    String device;
    String color;
    LatLng center;

    public Node() {
        center = new LatLng(0,0);
    }

    public Node(String color, double lat, double lon) {
        this.color = color;
        this.center = new LatLng(lat, lon);
    }

    public void setDevice(String device) {
        this.device = device;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setLat(double lat) {
        this.center = new LatLng(lat, center.longitude);
    }
    public void setLon(double lon) {
        this.center = new LatLng(center.latitude, lon);
    }

    public String getDevice() {
        return device;
    }
    public String getColor() {
        return color;
    }
    public double getLat() {
        return center.latitude;
    }
    public double getLon() {
        return center.longitude;
    }

    public int getAlliedColor() {
        if (color.equals("Red") || color.equals("red")) return Color.RED;
        if (color.equals("Blue") || color.equals("blue")) return Color.BLUE;
        return Color.BLACK;
    }

    public int getEnemyColor() {
        if (color.equals("Red") || color.equals("red")) return Color.BLUE;
        if (color.equals("Blue") || color.equals("blue")) return Color.RED;
        return Color.BLACK;
    }
}
