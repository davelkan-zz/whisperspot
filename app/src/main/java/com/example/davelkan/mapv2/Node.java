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
    private HashMap<String, List<Owner>> owners = new HashMap<>();

    public Node() {
        center = new LatLng(0, 0);
        owners = new HashMap<>();
    }

    public Node(RawNode rawNode, String device) {
        this.device = device;
        this.color = rawNode.getColor();
        this.ownership = rawNode.getOwnership();
        this.center = new LatLng(rawNode.getLat(), rawNode.getLon());
        this.owners = rawNode.getOwners();
    }

    public Node(String device, String color, int ownership, LatLng center, HashMap<String, List<Owner>> owners) {
        this.device = device;
        this.color = color;
        this.ownership = ownership;
        this.center = center;
        this.owners = owners;
    }

    public RawNode getRawNode() {
        return new RawNode(color, ownership, center.latitude, center.longitude, owners);
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
    public void setCenter(LatLng center) {
        this.center = center;
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
    public String getOtherColor(String color) {
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
    public CaptureResult captureByPoints(String userName, String captureColor, int points) {
        // add capturing team's color to list of owners
        if (owners.get(captureColor) == null) {
            owners.put(captureColor, new ArrayList<Owner>());
        }

        // set new ownership number and color according to capture details
        int usedPoints = points;
        boolean wasCaptured = false;
        if (color.equalsIgnoreCase(this.color)) {
            ownership += points;
        } else {
            ownership -= points;
            // switch node color if ownership drops below 0
            if (ownership < 0) {
                ownership *= -1;
                setColor(captureColor);
                wasCaptured = true;
            }
        }

        // check if node is over-captured
        if (ownership > 100) {
            int wastedPoints = ownership - 100;
            usedPoints = points - wastedPoints;
            ownership = 100;
        }

        // subtract used capture points from other team's list of owners
        List<Owner> otherOwners = owners.get(getOtherColor(captureColor));
        int pointsToRemove = usedPoints;
        while (pointsToRemove > 0) {
            // start removing from beginning of list (oldest owners)
            Owner otherOwner = otherOwners.get(0);
            // remove owner if all their points are removed, otherwise decrease their points
            if (pointsToRemove >= otherOwner.getPoints()) {
                otherOwners.remove(0);
                pointsToRemove -= otherOwner.getPoints();
            } else {
                otherOwner.subtractPoints(pointsToRemove);
            }
        }

        // add used capture points to the end of this team's list of owners
        if (usedPoints > 0) {
            owners.get(color).add(new Owner(userName, usedPoints));
        }

        return new CaptureResult(usedPoints, wasCaptured);
    }
}
