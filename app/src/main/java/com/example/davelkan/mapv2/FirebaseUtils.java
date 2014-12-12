package com.example.davelkan.mapv2;

/**
 * Created by mwismer on 11/6/14.
 */

import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
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

    public void populateNodes(final MapsActivity activity) {
        Firebase dataRef = firebase.child("nodes");
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Node node = child.getValue(Node.class);
                    node.setDevice(child.getKey());
                    activity.addNode(node);
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
                //TODO: Display username and timestamp, pick which message better
                text.setText(messages.values().iterator().next().get("text"));
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