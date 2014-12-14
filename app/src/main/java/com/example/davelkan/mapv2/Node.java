package com.example.davelkan.mapv2;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    private String device;
    private String color;
    private int ownership;
    private LatLng center;
    private HashMap<String, List<String>> owners = new HashMap<>();
    private List<String> colors = new ArrayList<>();

    public Node() {
        center = new LatLng(0,0);
    }

    public Node(RawNode rawNode, String device) {
        this.device = device;
        this.color = rawNode.getColor();
        this.ownership = rawNode.getOwnership();
        this.center = new LatLng(rawNode.getLat(), rawNode.getLon());
    }

    public Node(String color, int ownership, double lat, double lon) {
        this.color = color;
        this.ownership = ownership;
        this.center = new LatLng(lat, lon);
    }

    public Node(String color, LatLng center) {
        this.color = color;
        this.center = center;
    }

    public void setDevice(String device) {
        this.device = device;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setOwnership(int ownership) {
        this.ownership = ownership;
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

    public int getOwnership() {
        return ownership;
    }
    public double getLat() {
        return center.latitude;
    }
    public double getLon() {
        return center.longitude;
    }
    public LatLng getCenter() {
        return center;
    }

    // get color string of color of this node
    public String getColor() {
        return color;
    }

    // get color string of opposite color of this node
    public String getOtherColor() {
        if (color.equalsIgnoreCase("red")) return "blue";
        if (color.equalsIgnoreCase("blue")) return "red";
        return "black";
    }

    // get Android color value of color of this node
    public int getAllyColor() {
        if (color.equalsIgnoreCase("red")) return Color.RED;
        if (color.equalsIgnoreCase("blue")) return Color.BLUE;
        return Color.BLACK;
    }

    // get Android color value of opposite color of this node
    public int getEnemyColor() {
        if (color.equalsIgnoreCase("red")) return Color.BLUE;
        if (color.equalsIgnoreCase("blue")) return Color.RED;
        return Color.BLACK;
    }

    // add points to a node by color of capturing team
    public int captureByPoints(String color, int points) {
        int usedPoints = points;
        if (color.equalsIgnoreCase(this.color)) {
            ownership += points;
        } else {
            ownership -= points;
            // switch node color if ownership drops below 0
            if (ownership < 0) {
                ownership *= -1;
                setColor(color);
            }
        }
        if (ownership > 100) {
            int wastedPoints = ownership - 100;
            usedPoints = points - wastedPoints;
            ownership = 100;
        }
        return usedPoints;
    }
}
