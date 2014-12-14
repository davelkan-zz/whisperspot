package com.example.davelkan.mapv2;

import java.util.HashMap;
import java.util.List;

public class RawNode {
    private String color;
    private int ownership;
    private double lat;
    private double lon;
    private HashMap<String, List<Owner>> owners;

    public RawNode() {}

    public RawNode(String color, int ownership, double lat, double lon, HashMap<String, List<Owner>> owners) {
        this.color = color;
        this.ownership = ownership;
        this.lat = lat;
        this.lon = lon;
        this.owners = owners;
    }

    public String getColor() {
        return color;
    }
    public int getOwnership() {
        return ownership;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public HashMap<String, List<Owner>> getOwners() {
        return owners;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public void setOwnership(int ownership) {
        this.ownership = ownership;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public void setOwners(HashMap<String, List<Owner>> owners) {
        this.owners = owners;
    }
}
