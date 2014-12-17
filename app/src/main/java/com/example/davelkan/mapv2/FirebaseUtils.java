package com.example.davelkan.mapv2;

import android.util.Log;
import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {
    private String TAG = "FirebaseUtils";
    private String url = "https://mobproto-final.firebaseio.com/";
    private Firebase firebase;

    public FirebaseUtils() {
        firebase = new Firebase(url);
    }

    public void pushUUIDInfo(String deviceAddress, Map<String, byte[]> valueMap) {
        Firebase currentDevice = new Firebase(url).child("devices")
                                                  .child(deviceAddress)
                                                  .child(timestamp());
        currentDevice.child("values").setValue(valueMap);
        currentDevice.child("UUIDs").setValue(valueMap.keySet());
    }

    public void sendMessage(String messageText, String username, String deviceID) {
        Firebase deviceMessages = (new Firebase(url)).child("messages")
                                                     .child(deviceID);
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("username", username);
        message.put("text", messageText);
        message.put("timestamp", timestamp());
        deviceMessages.push().setValue(message);
    }

    // pushes node data to Firebase list of node data
    public void pushNode(Node node) {
        firebase.child("nodes").child(node.getDevice()).setValue(node.getRawNode());
    }

    // requests update in information of a single node
    public void pullNode(final MapsActivity activity, final Node node) {
        Firebase dataRef = firebase.child("nodes").child(node.getDevice());
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    activity.updateNode(node, snapshot.getValue(RawNode.class));
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
        firebase.child("users").child(username).setValue(user);
    }

    // pulls list of node data from firebase and creates Node objects for each
    public void populateNodes(final MapsActivity activity) {
        Firebase dataRef = firebase.child("nodes");
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Node node = new Node(child.getValue(RawNode.class), child.getKey());
                        activity.addNode(node);
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

    public void displayMostRecentMessage(String deviceID, final TextView text) {
        Firebase deviceMessages = (new Firebase(url)).child("messages").child(deviceID);
        deviceMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, String>> messages;
                messages = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                String maxKey = Collections.max(messages.keySet());
                //TODO: Display username and timestamp
                text.setText(messages.get(maxKey).get("text"));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss").format(new Date());

    }
}