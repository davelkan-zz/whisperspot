package com.example.davelkan.mapv2.util;

import com.example.davelkan.mapv2.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by mwismer on 12/17/14.
 */
public class NodeMap {
    private HashMap<String, List<Node>> nodeMap;
    private MapsActivity activity;

    public NodeMap(MapsActivity activity) {
        this.activity = activity;
        this.nodeMap = new HashMap<>();
    }

    public List<Node> get(String key) {
        return nodeMap.get(key);
    }

    public void put(String key, List<Node> node) {
        nodeMap.put(key, node);
    }

    public void addNode(Node node) {
        if (nodeMap.get(node.getColor()) == null) {
            nodeMap.put(node.getColor(), new ArrayList<Node>());
        }
        nodeMap.get(node.getColor()).add(node);

        activity.drawNode(node);
    }

    // updates a node's information with new data
    public void updateNode(Node node, RawNode data) {
        if (!node.getColor().equalsIgnoreCase(data.getColor())) { // new color
            updateNodeColor(node, node.getColor());
        }
        node.update(data);
    }

    public void updateNodeColor(Node node, String newColor) {
        if (nodeMap.get(newColor) == null) { // add new color to nodes information
            nodeMap.put(newColor, new ArrayList<Node>());
        }
        nodeMap.get(node.getColor()).remove(node);
        nodeMap.get(newColor).add(node);
        // TODO: reflect color change on map
    }


    public Node getNodeFromDevice(String device) {
        for (List<Node> nodeList : nodeMap.values()) {
            for (Node node : nodeList) {
                if (device.equals(node.getDevice())) return node;
            }
        }
        return null;
    }

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

    //Check distance from given nodes, used for discovering unknown nodes
    public Node checkProximity(String color, LatLng latLng) {
        List<Node> nodes = nodeMap.get(color);
        if (nodes == null || latLng == null) {
            return null;
        }
        for (Node activeNode : nodes) {
            if (activeNode.getDistance(latLng) < 25) {
                System.out.print("in Range");
                return activeNode;
            }
        }
        return null;
    }
}
