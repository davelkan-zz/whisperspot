package com.example.davelkan.mapv2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.davelkan.mapv2.util.Node;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class Listeners {
    // listener to check if user is clicking on a node (requesting information about it)
    public static GoogleMap.OnMapClickListener getOnMapClickListener(final MainActivity activity, final MapsFragment fragment) {
        return new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                if (point != null) {
                    Listeners.onNodeClick(activity, fragment, point);
                }
            }
        };
    }

    // listener to check if user would like to request a manual location update (if in dev mode)
    public static GoogleMap.OnMapLongClickListener getOnMapLongClickListener(final MainActivity activity, final MapsFragment fragment) {
        return new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                if (point != null && activity.getDevMode()) {
                    Listeners.onLocationUpdate(activity, fragment, point);
                }
            }
        };
    }

    // location listener from location manager to receive GPS location information
    public static LocationListener getLocationListener(final MainActivity activity, final MapsFragment fragment) {
        return new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location != null && !activity.getDevMode()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Listeners.onLocationUpdate(activity, fragment, latLng);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
//                Log.i("LOCATION UPDATE", status + " " + extras.get("satellites"));
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    // called when a user short clicks a node, requesting information without traveling to it
    public static void onNodeClick(MainActivity activity, MapsFragment fragment, LatLng latLng) {
        Node activeNode = fragment.getActiveNode();
        Node foundNode = activity.nodes.getNodeFromLatLng(latLng);

        if (activeNode != null && activeNode != foundNode) {
            fragment.setMapState(0);
        }
        if (foundNode == null) { // didn't find a node
            Log.i("LOCATION UPDATE", "NOT IN A NODE");
            fragment.setMapState(0);
        } else { // found a node
            if (foundNode.getColor().equals(fragment.getUser().getColor())) {
                fragment.setMapState(5);
            } else {
                fragment.setMapState(6);
            }
            if (!foundNode.equals(fragment.getActiveNode())) {
                fragment.enterNewNode(foundNode);
            }
            Log.i("LOCATION UPDATE", "IN NODE: " + foundNode.getDevice());
        }
        fragment.displayButtons();
    }

    // called when a user moves to a new location, either using devMode or GPS updates
    public static void onLocationUpdate(MainActivity activity, MapsFragment fragment, LatLng latLng) {
        Node activeNode = fragment.getActiveNode();

        fragment.updateMarker(latLng);

        Node foundNode = activity.nodes.getNodeFromLatLng(latLng);

        if (activeNode != null && activeNode != foundNode) {
            fragment.mainActivity.toastify("left " + activeNode.getName());
            fragment.setMapState(0);
        }
        if (foundNode == null) { // didn't find a node
            fragment.setActiveNode(null);
            Log.i("LOCATION UPDATE", "NOT IN A NODE");
            fragment.setMapState(0);
        } else { // found a node
            if (foundNode.getColor().equals(fragment.getUser().getColor())) {
                fragment.setMapState(1);
            } else {
                fragment.setMapState(2);
            }
            if (!foundNode.equals(fragment.getActiveNode())) {
                fragment.enterNewNode(foundNode);
                activity.toastify("entered " + foundNode.getName());
            }
            Log.i("LOCATION UPDATE", "IN NODE: " + foundNode.getDevice());
            fragment.setActiveNode(foundNode);
        }
        fragment.displayButtons();
    }

    public static View.OnClickListener gatherIntel(final MapsFragment activity) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setMapState(4);
                activity.displayButtons();
                activity.popUp(activity.getIntel().gatherIntel(activity.getActiveNode()));
            }
        });
    }

    public static View.OnClickListener deliverIntel(final MapsFragment activity) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setMapState(4);
                activity.displayButtons();
                activity.deliverIntel();
            }
        });

    }

    public static View.OnClickListener decryptIntel(final MapsFragment activity) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setMapState(4);
                activity.displayButtons();
                activity.popUp(activity.getIntel().decryptIntel(activity.getActiveNode()));
            }
        });
    }
}