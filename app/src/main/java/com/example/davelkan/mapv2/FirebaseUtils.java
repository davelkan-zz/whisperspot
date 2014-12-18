package com.example.davelkan.mapv2;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.davelkan.mapv2.util.Node;
import com.example.davelkan.mapv2.util.NodeMap;
import com.example.davelkan.mapv2.util.Owner;
import com.example.davelkan.mapv2.util.RawNode;
import com.example.davelkan.mapv2.util.RawUser;
import com.example.davelkan.mapv2.util.User;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUtils {
    private String TAG = "FirebaseUtils";
    private String url = "https://mobproto-final.firebaseio.com/";
    private Firebase firebase;

    public FirebaseUtils() {
        firebase = new Firebase(url);
    }

    public FirebaseUtils(NodeMap nodes) {
        firebase = new Firebase(url);
        resetFirebase(false);
        populateNodes(nodes);
    }

    // wipes all nodestats from the Firebase
    private void resetFirebase(boolean confirm) {
        if (confirm) {
            createNewNode("BC:6A:29:AE:DA:C1", "Campus Center", "blue", new LatLng(42.293307, -71.263748));
            createNewNode("78:A5:04:8C:25:DF", "Academic Center", "red", new LatLng(42.29372, -71.264478));
            createNewNode("D4:0E:28:2D:C5:B2", "East Hall", "blue", new LatLng(42.292671, -71.262174));
            createNewNode("CD:19:99:D7:B5:8E", "Upper Lawn", "red", new LatLng(42.292728, -71.263475));
            createNewNode("device0", "Lower Lawn", "blue", new LatLng(42.292333, -71.262797));
            createNewNode("device1", "West Hall", "red", new LatLng(42.293091, -71.2626));
        }
    }

    // adds a new device to Firebase, or updates current device's information
    public void createNewNode(String device, String name, String color, LatLng center) {
        List<Owner> thisOwnersList = new ArrayList<>();
        thisOwnersList.add(new Owner("initialAlly", 150));
        List<Owner> otherOwnersList = new ArrayList<>();
        otherOwnersList.add(new Owner("initialEnemy", 50));

        HashMap<String, List<Owner>> owners = new HashMap<>();
        owners.put(color, thisOwnersList);
        owners.put(Node.getOtherColor(color), otherOwnersList);

        pushNode(new Node(device, name, color, 50, center, owners));
    }

    public void pushUUIDInfo(String deviceAddress, Map<String, byte[]> valueMap) {
        Firebase currentDevice = new Firebase(url).child("devices")
                                                  .child(deviceAddress)
                                                  .child(timestamp());
        currentDevice.child("values").setValue(valueMap);
        currentDevice.child("UUIDs").setValue(valueMap.keySet());
    }

    // pushes node data to Firebase list of node data
    public void pushNode(Node node) {
        firebase.child("nodes").child(node.getDevice()).setValue(node.getRawNode());
    }

    // requests update in information of a single node
    public void pullNode(final NodeMap nodes, final Node node) {
        Firebase dataRef = firebase.child("nodes").child(node.getDevice());
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    nodes.updateNode(node, snapshot.getValue(RawNode.class));
                } catch (FirebaseException e) {
                    Log.i("FIREBASE PULL", e.toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public void createUser(String username, String color) {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put("created", timestamp());
        user.put("color", color);
        user.put("points", "0");
        firebase.child("users").child(username).setValue(user);
    }

    public void retrieveUser(final String username, final User userToUpdate, final SharedPreferences preferences) {
        firebase.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.i("DebugDebug", dataSnapshot.getValue(RawUser.class).toString());
                    userToUpdate.updateWith(dataSnapshot.getValue(RawUser.class));
                    preferences.edit().putString("color", userToUpdate.getColor()).apply();
                } catch (FirebaseException e) {
                    Log.i("FIREBASE POPULATE", e.toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void pushUser(User user) {
        firebase.child("users").child(user.getName()).setValue(user.getRawUser());
    }

    // pulls list of node data from firebase and creates Node objects for each
    public void populateNodes(final NodeMap nodes) {
        Firebase dataRef = firebase.child("nodes");
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Node node = new Node(child.getValue(RawNode.class), child.getKey());
                        nodes.addNode(node);
                    } catch (FirebaseException e) {
                        Log.i("FIREBASE POPULATE", e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss").format(new Date());

    }
}