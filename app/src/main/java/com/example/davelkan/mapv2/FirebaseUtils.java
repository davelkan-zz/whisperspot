package com.example.davelkan.mapv2;

/**
 * Created by mwismer on 11/6/14.
 */
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class FirebaseUtils {
    private String TAG = "FirebaseUtils";
    private String url = "https://mobproto-final.firebaseio.com/";
    private Firebase firebase;

    public FirebaseUtils() {
        firebase = new Firebase(url);
    }

    public void pushUUIDInfo(String deviceAddress, Map<String, byte[]> valueMap) {
        String timeStamp = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss").format(new Date());
        Firebase currentDevice = new Firebase(url).child("devices").child(deviceAddress).child(timeStamp);
        currentDevice.child("values").setValue(valueMap);
        currentDevice.child("UUIDs").setValue(valueMap.keySet());
    }

    public void populateNodes(final MapsActivity activity) {
        Firebase dataRef = firebase.child("nodes");
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Node node = new Node(child.getValue(RawNode.class), child.getKey());
                    activity.addNode(node);
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    //    pattern for Firebase objects
    public static class RawNode {
        String color;
        double lat, lon;
        public RawNode() { }
        public RawNode(String color, double lat, double lon) {
            this.color = color;
            this.lat = lat;
            this.lon = lon;
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
        public String getColor() {
            return color;
        }
        public double getLat() {
            return lat;
        }
        public double getLon() {
            return lon;
        }
    }
}