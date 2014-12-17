package com.example.davelkan.mapv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
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

    public static void setDevModeListener(final MapsActivity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Set Developer Mode")
                .setMessage("Select whether you would like dev mode 'on' or 'off'.")
                .setNegativeButton("Off", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.setDevMode(false);
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("On", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.setDevMode(true);
                        dialogInterface.dismiss();
                    }
                }).show();
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
}
