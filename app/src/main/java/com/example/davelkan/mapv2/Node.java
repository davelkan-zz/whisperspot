package com.example.davelkan.mapv2;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    private String device;
    private String name;
    private String color;
    private int ownership;
    private LatLng center;
    private HashMap<String, List<Owner>> owners;

    public Node() {
        this.device = "testDevice";
        this.name = "testName";
        this.color = "black";
        this.ownership = 100;
        this.center = new LatLng(0, 0);
        this.owners = new HashMap<>();
    }

    public Node(RawNode rawNode, String device) {
        this.device = device;
        this.name = rawNode.getName();
        this.color = rawNode.getColor();
        this.ownership = rawNode.getOwnership();
        this.center = new LatLng(rawNode.getLat(), rawNode.getLon());
        this.owners = rawNode.getOwners();
    }

    public Node(String device, String name, String color, int ownership, LatLng center, HashMap<String, List<Owner>> owners) {
        this.device = device;
        this.name = name;
        this.color = color;
        this.ownership = ownership;
        this.center = center;
        this.owners = owners;
    }

    public RawNode getRawNode() {
        return new RawNode(name, color, ownership, center.latitude, center.longitude, owners);
    }

    public void setDevice(String device) {
        this.device = device;
    }
    public void setName(String name) {
        this.name = name;
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
    public void setOwners(HashMap<String, List<Owner>> owners) {
        this.owners = owners;
    }

    public String getDevice() {
        return device;
    }
    public String getName() {
        return name;
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
    public HashMap<String, List<Owner>> getOwners() {
        return owners;
    }

    // get color string of color of this node
    public String getColor() {
        return color;
    }

    // get color string of opposite color of this node
    public static String getOtherColor(String color) {
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

    // update this node's information with new data
    public void update(RawNode data) {
        setName(data.getName());
        setColor(data.getColor());
        setOwnership(data.getOwnership());
        setCenter(new LatLng(data.getLat(), data.getLon()));
        setOwners(data.getOwners());
    }

    // add points to this node by color of capturing team
    public CaptureResult captureByPoints(String userName, String captureColor, int points) {
        // add capturing team's color to list of owners
        Log.i("CAPTURE EVENT", userName + " captures " + device + " with " + points + " points.");
        if (owners.get(captureColor) == null) {
            owners.put(captureColor, new ArrayList<Owner>());
        }

        // set new ownership number and color according to capture details
        int usedPoints = points;
        boolean wasCaptured = false;
        if (captureColor.equalsIgnoreCase(color)) {
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

        Log.i("CAPTURE EVENT", userName + " uses " + usedPoints + " points at " + name);

        // subtract used capture points from other team's list of owners
        List<Owner> otherOwners = owners.get(getOtherColor(captureColor));
        int pointsToRemove = usedPoints;
        while (pointsToRemove > 0) {
            // start removing from beginning of list (oldest owners)
            if (otherOwners.size() == 0) {
                Log.i("CAPTURE EVENT", "No one to take points from :(");
                break;
            }
            Owner otherOwner = otherOwners.get(0);
            // remove owner if all their points are removed, otherwise decrease their points
            Log.i("CAPTURE EVENT", userName + " took " + usedPoints + " points from " + otherOwner.getUserName());
            if (pointsToRemove >= otherOwner.getPoints()) {
                otherOwners.remove(0);
                pointsToRemove -= otherOwner.getPoints();
                Log.i("CAPTURE EVENT", otherOwner.getUserName() + " lost all points at " + name);
            } else {
                otherOwner.subtractPoints(pointsToRemove);
                pointsToRemove = 0;
            }
        }

        // add used capture points to the end of this team's list of owners
        if (usedPoints > 0) {
            owners.get(color).add(new Owner(userName, usedPoints));
        }

        return new CaptureResult(usedPoints, wasCaptured);
    }
}
