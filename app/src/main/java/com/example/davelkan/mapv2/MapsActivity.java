package com.example.davelkan.mapv2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {
    private String TAG = "MapsActivity";
    public String APP_NAME = "Whisperspot";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private BLEScanner scanner;
    private FirebaseUtils firebaseUtils;
    private HashMap<String, ArrayList<Node>> nodes = new HashMap<String, ArrayList<Node>>();
    private String allyColor = "Blue";
    private String enemyColor = "Red";
    private Node activeNode;

    Button leave_message;
    Button take_message;
    Button lvtrp;
    Button dcptmsg;
    Button submit_message;
    Button cnclmsg;
    TextView display_message;
    EditText message;
    int mapState = 0;
    Marker newLocation;
    Marker myLocation;
    int zoom = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Firebase.setAndroidContext(this);
        pullFromFirebase();
        setUpMapIfNeeded();
        initButtons();
        runScanner();
    }

    private void pullFromFirebase() {
        firebaseUtils = new FirebaseUtils();
        firebaseUtils.populateNodes(this);
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER )) {
            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
            Toast.makeText(this, "Please enable GPS for " + APP_NAME + " to work properly.", Toast.LENGTH_LONG).show();
            startActivity(myIntent);
            return;
        }

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
                    if (newLocation != null) {
                        newLocation.remove();
                        if (myLocation != null) {myLocation.remove();}
                    }
                    newLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("New Position"));
                    Node foundNode = checkAllyProximity(location);
                    if (foundNode == null) { foundNode = checkEnemyProximity(location); }
                    if (foundNode == null) { // didn't find a node
                        mapState = 0;
                        makeInvisible();
                    } else { // found a node
                        activeNode = foundNode;
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            Marker myLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("My Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        } else { //this is just so it zooms in on Olin even if it finds nothing
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.2929, -71.2615), zoom));
        }
    }


    //check to see if you're in a node
    private Node checkAllyProximity(Location location){
        if (nodes == null) { return null; }
        for(Node activeNode : nodes.get(allyColor)) {
            boolean isClose = getIsClose(activeNode.center, location);
            if (isClose) { // found a node
                if (mapState < 2) { //issue commands based on mapState
                    leave_message.setVisibility(View.VISIBLE);
                    take_message.setVisibility(View.VISIBLE);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(activeNode.center, 19));
                    mapState = 1;
                }
                return activeNode;
            }
        }
        return null;
    }

    private Node checkEnemyProximity(Location location){
        if (nodes == null) { return null; }
        for(Node activeNode : nodes.get(enemyColor)) {
            boolean isClose = getIsClose(activeNode.center, location);

            if (isClose && mapState < 2) {
                System.out.print("in Range");
                lvtrp.setVisibility(View.VISIBLE);
                dcptmsg.setVisibility(View.VISIBLE);
                mapState = 1;
                return activeNode;
            }
        }
        return null;
    }

    private void zoomBelowNode() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(activeNode.center.latitude - 0.00025, activeNode.center.longitude), 19));
    }

    public void initButtons(){
        leave_message = (Button) findViewById(R.id.lvmsg);
        take_message = (Button) findViewById(R.id.tkmsg);
        lvtrp = (Button) findViewById(R.id.lvtrp);
        dcptmsg = (Button) findViewById(R.id.dcptmsg);
        message = (EditText) findViewById(R.id.message);
        display_message = (TextView) findViewById(R.id.message_display);
        submit_message = (Button) findViewById(R.id.sbmtmsg);
        cnclmsg = (Button) findViewById(R.id.cnclmsg);
        leave_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnclmsg.setVisibility(View.VISIBLE);
                submit_message.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
                take_message.setVisibility(View.INVISIBLE);
                leave_message.setVisibility(View.INVISIBLE);
                mapState = 2;
                zoomBelowNode();
            }
        });
        take_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_message.setVisibility(View.INVISIBLE);
                leave_message.setVisibility(View.INVISIBLE);
                mapState = 2;
                display_message.setText("");
                display_message.setVisibility(View.VISIBLE);
                (new FirebaseUtils()).displayMostRecentMessage("78:A5:04:8C:25:DF", display_message);
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
        submit_message.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String submit = message.getText().toString();
                //TODO: change username and deviceID to real values
                (new FirebaseUtils()).sendMessage(submit, "example_user", "78:A5:04:8C:25:DF");
                message.setVisibility(View.INVISIBLE);
                submit_message.setVisibility(View.INVISIBLE);
                cnclmsg.setVisibility(View.INVISIBLE);
                mapState = 1;
            }
        });
        cnclmsg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                message.setVisibility(View.INVISIBLE);
                submit_message.setVisibility(View.INVISIBLE);
                cnclmsg.setVisibility(View.INVISIBLE);

            }
        });
    }
    public void makeInvisible(){
        lvtrp.setVisibility(View.INVISIBLE);
        dcptmsg.setVisibility(View.INVISIBLE);
        message.setVisibility(View.INVISIBLE);
        submit_message.setVisibility(View.INVISIBLE);
        cnclmsg.setVisibility(View.INVISIBLE);
    }

    private void updatepoints(){
        System.out.print("words");
    }

    public void addNode(Node node) {
        if(nodes.get(node.color) == null) {
            nodes.put(node.color, new ArrayList<Node>());
        }
        Log.i("yo", node.color + "");
        nodes.get(node.color).add(node);
        mMap.addCircle(new CircleOptions()
                .center(node.center)
                .radius(25)
                .strokeColor(getOtherColor(node.color))
                .fillColor(getThisColor(node.color)));
    }

    public List<Node> getNodesByColor(String color) {
        return nodes.get(color);
    }

    private int getThisColor(String team) {
        if (team.equals("Red") || team.equals("red")) return Color.RED;
        if (team.equals("Blue") || team.equals("blue")) return Color.BLUE;
        return Color.BLACK; // wat should be here?
    }

    private int getOtherColor(String team) {
        if (team.equals("Red") || team.equals("red")) return Color.BLUE;
        if (team.equals("Blue") || team.equals("blue")) return Color.RED;
        return Color.BLACK; // wat should be here?
    }

    public boolean getIsClose(LatLng start, Location location) {
        double radius = 6778.137;//radius of earth in km
        double distLat = (start.latitude - location.getLatitude())*Math.PI/180;
        double distLong = (start.longitude - location.getLongitude())*Math.PI/180;
        double step_a = Math.sin(distLat/2) * Math.sin(distLat/2) +
                Math.cos(location.getLatitude() * Math.PI / 180) * Math.cos(start.latitude * Math.PI / 180) *
                        Math.sin(distLong/2) * Math.sin(distLong/2);
        double step_b = 2* Math.atan2(Math.sqrt(step_a),Math.sqrt(1-step_a));
        double step_c = radius * step_b * 1000; //need to scale it to meters*/
        if (step_c < 25) {
            return true;
        } else{
            return false;
        }
    }

    public FirebaseUtils getFirebaseUtils() {
        return firebaseUtils;
    }
}

