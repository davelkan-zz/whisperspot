package com.example.davelkan.mapv2.util;

import com.example.davelkan.mapv2.MapsFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NodeMap {
    private HashMap<String, List<Node>> nodeMap; // all node data, discovered or not
    private MapsFragment fragment;

    public NodeMap(MapsFragment fragment) {
        this.fragment = fragment;
        this.nodeMap = new HashMap<>();
    }

    public List<Node> get(String key) {
        return nodeMap.get(key);
    }

    public void put(String key, List<Node> node) {
        nodeMap.put(key, node);
    }

    // called each time Firebase gets a new node
    public void addNode(Node node) {
        if (nodeMap.get(node.getColor()) == null) {
            nodeMap.put(node.getColor(), new ArrayList<Node>());
        }
        nodeMap.get(node.getColor()).add(node);
        fragment.drawNode(node);
    }

    // updates a node's information with new data
    public void updateNode(Node node, RawNode data) {
        if (!node.getColor().equalsIgnoreCase(data.getColor())) { // new color
            updateNodeColor(node, node.getColor());
        }
        node.update(data);
    }

    // when a node changes color, it must be moved around in data storage, and also re-drawn on the map
    public void updateNodeColor(Node node, String newColor) {
        if (nodeMap.get(newColor) == null) { // add new color to nodes information
            nodeMap.put(newColor, new ArrayList<Node>());
        }
        nodeMap.get(node.getColor()).remove(node);
        nodeMap.get(newColor).add(node);
        fragment.drawNode(node);
    }

    // find node from unique identifier (device name)
    public Node getNodeFromDevice(String device) {
        for (List<Node> nodeList : nodeMap.values()) {
            for (Node node : nodeList) {
                if (device.equals(node.getDevice())) return node;
            }
        }
        return null;
    }

    // find node from point on map (within 25 meters-range of Bluetooth)
    public Node getNodeFromLatLng(LatLng point) {
        if (nodeMap == null || point == null) {
            return null;
        }
        for (List<Node> nodeList : nodeMap.values()) {
            for (Node node : nodeList) {
                if (node.getDistance(point) < 25) {
                    return node;
                }
            }
        }
        return null;
    }
}
