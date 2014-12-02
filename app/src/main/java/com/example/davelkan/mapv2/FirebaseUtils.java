package com.example.davelkan.mapv2;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by mwismer on 11/6/14.
 */
public class FirebaseUtils {
    private String TAG = "FirebaseUtils";
    private String url = "https://mobproto-final.firebaseio.com/";
    private Firebase firebase = new Firebase(url);

    public void pushUUIDInfo(String deviceAddress, Map<String, byte[]> valueMap) {
        String timeStamp = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss").format(new Date());
        Firebase currentDevice = new Firebase(url).child("devices").child(deviceAddress).child(timeStamp);
        currentDevice.child("values").setValue(valueMap);
        currentDevice.child("UUIDs").setValue(valueMap.keySet());
    }

    public void getNodeList(ValueEventListener listener) {
        Firebase nodeList = (new Firebase(url));
        nodeList = nodeList.child("nodes");
        nodeList.addListenerForSingleValueEvent(listener);
    }

    public List<MapsActivity.Point> getBlueLatList() {
        Query queryRef = firebase.child("nodes").orderByChild("color").equalTo("blue");
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                MapsActivity.Node item = new MapsActivity.Node(snapshot.getValue(RawNode.class), snapshot.getKey());

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
        return new ArrayList<MapsActivity.Node> (Arrays.asList(items));
    }

//    pattern for Firebase objects
    public class RawNode {
        String color;
        double lat, lon;
        public RawNode() { }
    }
}