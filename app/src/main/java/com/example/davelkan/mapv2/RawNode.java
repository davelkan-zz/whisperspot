package com.example.davelkan.mapv2;

public class RawNode {
    private String color;
    private int ownership;
    private double lat;
    private double lon;

    public RawNode() {}

    public RawNode(String color, int ownership, double lat, double lon) {
        this.color = color;
        this.ownership = ownership;
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public int getOwnership() {
        return ownership;
    }
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public void setOwnership(int ownership) {
        this.ownership = ownership;
    }
}
