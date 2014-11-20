package com.example.davelkan.mapv2;

import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by mwismer on 11/6/14.
 */
public class FirebaseUtils {
    private String TAG = "FirebaseUtils";
    private String url = "https://mobproto-final.firebaseio.com/";

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
}