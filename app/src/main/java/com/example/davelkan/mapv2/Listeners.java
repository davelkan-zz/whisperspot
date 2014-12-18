package com.example.davelkan.mapv2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.davelkan.mapv2.util.Node;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Listeners {
    public static GoogleMap.OnMapLongClickListener getOnMapLongClickListener(final MapsFragment activity) {
        return new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                if (point != null && activity.getDevMode()) {
                    Listeners.onLocationUpdate(activity, point);
                }
            }
        };
    }

    public static LocationListener getLocationListener(final MapsFragment activity) {
        return new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location != null && !activity.getDevMode()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Listeners.onLocationUpdate(activity, latLng);
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

    public static void onLocationUpdate(MapsFragment activity, LatLng latLng) {
        Node activeNode = activity.getActiveNode();

        activity.updateMarker(latLng);

        Node foundNode = activity.checkAllyProximity(latLng);
        if (foundNode == null) {
            foundNode = activity.checkEnemyProximity(latLng);
        }

        if (activeNode != null && activeNode != foundNode) {
            activity.toastify("left " + activeNode.getName());
            activity.setMapState(0);
        }
        if (foundNode == null) { // didn't find a node
            activity.setActiveNode(null);
            Log.i("LOCATION UPDATE", "NOT IN A NODE");
            activity.setMapState(0);
        } else { // found a node
            if (foundNode.getColor().equals(activity.getUser().getColor())) {
                activity.setMapState(1);
            } else {
                activity.setMapState(2);
            }
            if (!foundNode.equals(activity.getActiveNode())) {
                activity.enterNewNode(foundNode);
            }
            Log.i("LOCATION UPDATE", "IN NODE: " + foundNode.getDevice());
            activity.setActiveNode(foundNode);
        }
        activity.displayButtons();
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