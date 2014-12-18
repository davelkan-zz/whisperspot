package com.example.davelkan.mapv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class Listeners {
    public static GoogleMap.OnMapLongClickListener getOnMapLongClickListener(final MapsActivity activity) {
        return new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                if (point != null && activity.getDevMode()) {
                    activity.updateLocation(point);
                }
            }
        };
    }

    public static LocationListener getLocationListener(final MapsActivity activity) {
        return new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location != null && !activity.getDevMode()) {
                    activity.updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
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


    public static View.OnClickListener gatherIntel(final MapsActivity activity) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setMapState(4);
                activity.displayButtons(4);
                activity.popUp(activity.getIntel().gatherIntel(activity.getActiveNode()));
            }
        });
    }

    public static View.OnClickListener deliverIntel(final MapsActivity activity) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setMapState(4);
                activity.displayButtons(4);
                activity.deliverIntel();
            }
        });

    }

    public static View.OnClickListener decryptIntel(final MapsActivity activity) {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setMapState(4);
                activity.displayButtons(4);
                activity.popUp(activity.getIntel().decryptIntel(activity.getActiveNode()));
            }
        });
    }
}