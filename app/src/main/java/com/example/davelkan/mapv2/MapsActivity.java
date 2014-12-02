package com.example.davelkan.mapv2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity{
    private String TAG = "MapsActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private BLEScanner scanner;

    Button lvmsg;
    Button tkmsg;
    Button lvtrp;
    Button dcptmsg;
    Button sbmtmsg;
    Button cnclmsg;
    EditText message;
    int mapState = 0;
    Marker newLocation;
    Marker myLocation;
    int zoom = 17;

    //public Marker mylocation = mMap.addMarker(new MarkerOptions());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        initButtons();

        Firebase.setAndroidContext(this);
        runScanner();
    }

    public void runScanner() {
        scanner = new BLEScanner(this);
        scanner.scanBLE("78:A5:04:8C:25:DF");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        scanner.onActivityResult(requestCode, resultCode, data);
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
     * install/update the Google Play services APK
     * on their device.
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
        final List<Point> BlueLatList = new ArrayList<Point>(Arrays.asList(new Point(42.2929,-71.2615),new Point(42.292,-71.2638),new Point(42.29293,-71.262125), new Point(42.293627,-71.264513)));
        final List<Point> RedLatList = new ArrayList<Point>(Arrays.asList(new Point(42.292,-71.2624),new Point(42.2926, -71.2635),new Point(42.2930325, -71.2619909)));
        // Get the location manager

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //final List<Point> shocked = new ArrayList<Point>(Arrays.asList(new Point(42.29362,-71.263992),new Point(42.293572,-71.263729),new Point(42.293302,-71.263863),new Point(42.293389,-71.263965),new Point(42.293405,-71.264121),new Point(42.293338,-71.264255)));
                if(location != null){
                    if (newLocation != null) {
                        newLocation.remove();
                        if(myLocation != null){myLocation.remove();}
                    }
                    newLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("New Position"));
                    checkEnemyNode(RedLatList, location);
                    checkAllyNode(BlueLatList, location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

        if (location != null) {
            Marker myLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("My Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.2929, -71.2615), zoom));
        }

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
                    .radius(10)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.RED));
        }
    }


    //check to see if you're in a node
    private int checkAllyNode(List<Point> nodeList,Location location){
        for(int i = 0; i < nodeList.size(); i++){
            Point activeNode = nodeList.get(i);
            double radius = 6778.137;//radius of earth in km
            double distLat = (activeNode.x - location.getLatitude())*Math.PI/180;
            double distLong = (activeNode.y - location.getLongitude())*Math.PI/180;
            double step_a = Math.sin(distLat/2) * Math.sin(distLat/2) +
                    Math.cos(location.getLatitude() * Math.PI / 180) * Math.cos(activeNode.x * Math.PI / 180) *
                            Math.sin(distLong/2) * Math.sin(distLong/2);
            double step_b = 2* Math.atan2(Math.sqrt(step_a),Math.sqrt(1-step_a));
            double step_c = radius * step_b * 1000; //need to scale it to meters*/

            if (step_c < 25  && sbmtmsg.getVisibility() == View.INVISIBLE) {
                System.out.print("in Range");
                lvmsg.setVisibility(View.VISIBLE);
                tkmsg.setVisibility(View.VISIBLE);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(activeNode.x, activeNode.y), 19));
                mapState = 1;
                return i;
            }
            else if(step_c < 25 && lvmsg.getVisibility() == View.INVISIBLE) {
                lvmsg.setVisibility(View.INVISIBLE);
                sbmtmsg.setVisibility(View.INVISIBLE);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(activeNode.x - 0.00025, activeNode.y), 19));
            }
            else{
                System.out.print("not in range");
                mapState = 0;
                makeInvisible();

            }

        }
        return -1;
    }

    private int checkEnemyNode(List<Point> nodeList, Location location){
        if(!nodeList.isEmpty()) for (int i = 0; i < nodeList.size(); i++) {
            Point activeNode = nodeList.get(i);
            double radius = 6778.137;//radius of earth in km
            double distLat = (activeNode.x - location.getLatitude()) * Math.PI / 180;
            double distLong = (activeNode.y - location.getLongitude()) * Math.PI / 180;
            double step_a = Math.sin(distLat / 2) * Math.sin(distLat / 2) +
            Math.cos(location.getLatitude() * Math.PI / 180) * Math.cos(activeNode.x * Math.PI / 180) *
            Math.sin(distLong / 2) * Math.sin(distLong / 2);
            double step_b = 2 * Math.atan2(Math.sqrt(step_a), Math.sqrt(1 - step_a));
            double step_c = radius * step_b * 1000; //need to scale it to meters
            System.out.print(step_c);

            if (step_c < 25 && mapState < 2) {
                System.out.print("in Range");
                lvtrp.setVisibility(View.VISIBLE);
                dcptmsg.setVisibility(View.VISIBLE);
                mapState = 1;
                return i;
            }
            else{
                System.out.print("not in range");
                mapState = 0;
                makeInvisible();
            }
        }
        return -1;
    }


    public class Point{
        double x;
        double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Node {
        int tag;
        String device;
        String colour;
        Point center;
        public Node(int tag, String device, String colour, Point center) {
            this.tag = tag;
            this.device = device;
            this.colour = colour;
            this.center = center;
        }
        public Node(FirebaseUtils.RawNode rawNode, String device) {
            this.device = device;
            this.colour = rawNode.color;
            this.center = new Point(rawNode.lat, rawNode.lon);
        }
        public Node() { }
    }

    public void initButtons(){
        lvmsg = (Button) findViewById(R.id.lvmsg);
        tkmsg = (Button) findViewById(R.id.tkmsg);
        lvtrp = (Button) findViewById(R.id.lvtrp);
        dcptmsg = (Button) findViewById(R.id.dcptmsg);
        message = (EditText) findViewById(R.id.message);
        sbmtmsg = (Button) findViewById(R.id.sbmtmsg);
        cnclmsg = (Button) findViewById(R.id.cnclmsg);
        lvmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnclmsg.setVisibility(View.VISIBLE);
                sbmtmsg.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
                tkmsg.setVisibility(View.INVISIBLE);
                lvmsg.setVisibility(View.INVISIBLE);
                mapState = 2;
            }
        });
        tkmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dcptmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        lvtrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        sbmtmsg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                message.setVisibility(View.INVISIBLE);
                sbmtmsg.setVisibility(View.INVISIBLE);
                cnclmsg.setVisibility(View.INVISIBLE);
                mapState = 1;
            }
        });
        cnclmsg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                message.setVisibility(View.INVISIBLE);
                sbmtmsg.setVisibility(View.INVISIBLE);
                cnclmsg.setVisibility(View.INVISIBLE);

            }
        });
    }
    public void makeInvisible(){
        lvtrp.setVisibility(View.INVISIBLE);
        dcptmsg.setVisibility(View.INVISIBLE);
        message.setVisibility(View.INVISIBLE);
        sbmtmsg.setVisibility(View.INVISIBLE);
        cnclmsg.setVisibility(View.INVISIBLE);
    }

    private void updatepoints(){
        System.out.print("words");
    }

}

