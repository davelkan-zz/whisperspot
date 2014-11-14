package com.example.davelkan.mapv2;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity{
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private TextView latituteField;
    private TextView longitudeField;
    private Location location;
    private LocationManager locationManager;
    //public Marker mylocation = mMap.addMarker(new MarkerOptions());




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        updatepoints();
    }





    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near my bedroom.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */



    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if(location != null){
                    Marker newLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("New Position"));
                    //mylocation.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                    //mylocation.setTitle("IT MOVED!!");
                    double pointx = location.getLatitude();
                    double pointy = location.getLongitude();
                    System.out.println("word");
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(pointx, pointy))
                            .radius(25)
                            .strokeColor(Color.RED)
                            .fillColor(Color.BLUE));
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1, locationListener);
        Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

        Marker myLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("My Position"));
        List<Point> BlueLatList = new ArrayList<Point>();
            BlueLatList.add(new Point(42.2929,-71.2615));
            BlueLatList.add(new Point(42.2935,-71.2635));
            BlueLatList.add(new Point(42.292,-71.2638));

        List<Point> RedLatList = new ArrayList<Point>();
            RedLatList.add(new Point(42.292,-71.2624));
            RedLatList.add(new Point(42.2926, -71.263));
            RedLatList.add(new Point(42.2927, -71.2622));

        for (Point aBlueLatList : BlueLatList) {
            double pointx = aBlueLatList.x;
            double pointy = aBlueLatList.y;
            System.out.println("word");
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(pointx, pointy))
                    .radius(25)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE));
        }

        for (Point aRedLatList : RedLatList) {
            double pointx = aRedLatList.x;
            double pointy = aRedLatList.y;
            System.out.println("word");
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(pointx, pointy))
                    .radius(25)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.RED));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
    }

    private void inAllyNode(List<Point> nodeList){
        for(int i =0; i< nodeList.size(); i++){
            Point activeNode = nodeList.get(i);
            double radius = 6778.137;//radius of earth in km
            double distLat = (activeNode.x - location.getLatitude())*Math.PI/180;
            double distLong = (activeNode.y - location.getLongitude())*Math.PI/180;
            double step_a = Math.sin(distLat/2) * Math.sin(distLat/2) +
                    Math.cos(location.getLatitude() * Math.PI / 180) * Math.cos(activeNode.x * Math.PI / 180) *
                            Math.sin(distLong/2) * Math.sin(distLong/2);
            double step_b = 2* Math.atan2(Math.sqrt(step_a),Math.sqrt(1-step_a));
            double step_c = radius * step_b * 1000; //need to scale it to meters

            if(step_c < 25){
                //you're in the node
            }
        }
    }

    private void inEnemyNode(List<Point> nodeList){

    }

    public class Point{
        double x;
        double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    private void updatepoints(){
        System.out.print("words");
    }

}

