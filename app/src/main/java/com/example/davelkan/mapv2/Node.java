package com.example.davelkan.mapv2;

public class Node {
    String device;
    String color;
    Point center;

    public Node() {
    }

    public Node(String device, String color, Point center) {
        this.device = device;
        this.color = color;
        this.center = center;
    }

    public Node(FirebaseUtils.RawNode rawNode, String device) {
        this.device = device;
        this.color = rawNode.color;
        this.center = new Point(rawNode.lat, rawNode.lon);
    }
}
