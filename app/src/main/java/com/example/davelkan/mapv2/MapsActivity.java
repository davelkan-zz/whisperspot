package com.example.davelkan.mapv2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

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
    private HashMap<String, List<Node>> nodes = new HashMap<>();
    private String allyColor = "blue";
    private String enemyColor = "red";
    private String userName = "Ralph";
    private int userPoints = 0;
    private int captureBonus = 10;
    private Node activeNode;
    private Node intel = null;
    private Location sink;

    Button leave_message;
    Button take_message;
    Button leave_trap;
    Button decrypt_message;
    Button submit_message;
    Button cancel_message;
    TextView display_message;
    TextView pop_up;
    EditText message;
    int mapState = 0;
    Marker myLocation;
    int zoom = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Firebase.setAndroidContext(this);
        setupFirebase();
        initButtons();
        setUpMapIfNeeded();
//        runScanner("78:A5:04:8C:25:DF");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        scanner.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        updatePoints();
    }

    // create Firebase reference and pull node data from it
    private void setupFirebase() {
        firebaseUtils = new FirebaseUtils();
//        createNewNode("BC:6A:29:AE:DA:C1", "blue", new LatLng(42.293307, -71.263748));
//        createNewNode("78:A5:04:8C:25:DF", "red", new LatLng(42.29372, -71.264478));
//        createNewNode("device0", "blue", new LatLng(42.292671, -71.262174));
        firebaseUtils.populateNodes(this);
    }

    // scans for a Bluetooth device
    public void runScanner(String device) {
        scanner = new BLEScanner(this);
        scanner.scanBLE(device);
    }

    // adds a new device to Firebase, or updates current device's information
    private void createNewNode(String device, String color, LatLng center) {
        List<Owner> ownersList = new ArrayList<>();
        ownersList.add(new Owner("default", 200));

        HashMap<String, List<Owner>> owners = new HashMap<>();
        owners.put(color, ownersList);

        firebaseUtils.pushNode(new Node(device, color, 100, center, owners));
    }

    // adds a node to list of nodes and draws on map
    public void addNode(Node node) {
        if(nodes.get(node.getColor()) == null) {
            nodes.put(node.getColor(), new ArrayList<Node>());
        }

        // TODO: be able to update node color
        nodes.get(node.getColor()).add(node);
        mMap.addCircle(new CircleOptions()
                .center(node.getCenter())
                .radius(25)
                .strokeColor(node.getEnemyColor())
                .fillColor(node.getAllyColor()));
    }

    public void captureNodeByPoints(Node node, int points) {
        CaptureResult result = node.captureByPoints(userName, allyColor, points);
        userPoints += result.getUsedPoints();
        if (result.getWasCaptured()) {
//            updateNodeColor(node, allyColor);
            nodes.get(node.getColor()).remove(node);
            nodes.get(allyColor).add(node);
            firebaseUtils.pushNode(node);
            userPoints += captureBonus;
        }
    }

    public FirebaseUtils getFirebaseUtils() {
        return firebaseUtils;
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

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (location != null) {
                if (myLocation != null) {
                    myLocation.remove();
                }
                //TODO:Check if near undiscovered node only display those known
                myLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("My Position"));
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            locationListener.onLocationChanged(location);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        } else { //this is just so it zooms in on Olin even if it finds nothing
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.2929, -71.2615), zoom));
        }
    }


    //check to see if you're in a node
    private Node checkAllyProximity(Location location){
        Node activeNode = checkProximity(nodes.get(allyColor), location);
        if (activeNode != null) {
            leave_message.setVisibility(View.VISIBLE);
            take_message.setVisibility(View.VISIBLE);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(activeNode.getCenter(), 19));
            mapState = 1;
        }
        return activeNode;
    }

    private Node checkEnemyProximity(Location location){
        Node activeNode = checkProximity(nodes.get(enemyColor), location);
        if (activeNode != null) {
            leave_trap.setVisibility(View.VISIBLE);
            decrypt_message.setVisibility(View.VISIBLE);
            mapState = 1;
        }
        return activeNode;
    }

    private Node checkProximity(List<Node> nodes, Location location) {
        if (nodes == null) { return null; }
        if (location == null) { return null; }
        for(Node activeNode : nodes) {
            if (isClose(activeNode, location) && mapState < 2) {
                System.out.print("in Range");
                return activeNode;
            }
        }
        return null;
    }

    private void zoomBelowNode() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(activeNode.getLat() - 0.00025, activeNode.getLon()), 19));
    }

    public void initButtons(){
        leave_message = (Button) findViewById(R.id.lvmsg);
        take_message = (Button) findViewById(R.id.tkmsg);
        leave_trap = (Button) findViewById(R.id.lvtrp);
        decrypt_message = (Button) findViewById(R.id.dcptmsg);
        message = (EditText) findViewById(R.id.message);
        display_message = (TextView) findViewById(R.id.message_display);
        submit_message = (Button) findViewById(R.id.sbmtmsg);
        cancel_message = (Button) findViewById(R.id.cnclmsg);
        pop_up = (TextView) findViewById(R.id.popUp);

        leave_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel_message.setVisibility(View.VISIBLE);
                //submit_message.setVisibility(View.VISIBLE);
                //message.setVisibility(View.VISIBLE);
                take_message.setVisibility(View.INVISIBLE);
                leave_message.setVisibility(View.INVISIBLE);
                mapState = 2;
                returnIntel();

                //zoomBelowNode();
            }
        });
        take_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_message.setVisibility(View.INVISIBLE);
                leave_message.setVisibility(View.INVISIBLE);
                mapState = 2;
                //display_message.setText("");
                //display_message.setVisibility(View.VISIBLE);
                //(new FirebaseUtils()).displayMostRecentMessage("78:A5:04:8C:25:DF", display_message);
                gatherIntel();
            }
        });
        decrypt_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        leave_trap.setOnClickListener(new View.OnClickListener() {
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
                cancel_message.setVisibility(View.INVISIBLE);
                take_message.setVisibility(View.VISIBLE);
                leave_message.setVisibility(View.VISIBLE);
                mapState = 1;
            }
        });
        cancel_message.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                message.setVisibility(View.INVISIBLE);
                submit_message.setVisibility(View.INVISIBLE);
                cancel_message.setVisibility(View.INVISIBLE);
                take_message.setVisibility(View.VISIBLE);
                leave_message.setVisibility(View.VISIBLE);
                mapState = 1;
            }
        });
        pop_up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                pop_up.setVisibility(View.INVISIBLE);
                leave_message.setVisibility(View.VISIBLE);
            }
        });
    }

    public void makeInvisible(){
        leave_trap.setVisibility(View.INVISIBLE);
        decrypt_message.setVisibility(View.INVISIBLE);
        message.setVisibility(View.INVISIBLE);
        submit_message.setVisibility(View.INVISIBLE);
        cancel_message.setVisibility(View.INVISIBLE);
    }

    private void updatePoints(){
        System.out.print("words");
    }

    public boolean isClose(Node start, Location location) {
        Location startLoc = new Location(location);
        startLoc.setLatitude(start.getLat());
        startLoc.setLongitude(start.getLon());

        return (startLoc.distanceTo(location) < 25);
        //return(true);
    }

    private void gatherIntel() { //gathering intel at allied node... checking node color may be unnecessary
        if(activeNode.getColor().equalsIgnoreCase(allyColor)) {
            if (intel == null) {
                pop_up.setText("Intel Gathered!  Deliver it to another WhisperSpot!");
                pop_up.setVisibility(View.VISIBLE);
                //TODO: Fanciness - add fancy fake number generator
                intel = activeNode;
            } else {
                pop_up.setText("You may only carry 1 intel at a time.");
                pop_up.setVisibility(View.VISIBLE);
            }
        }
        else{
            pop_up.setText("This is enemy territory! You have to decrypt intel here!");
            pop_up.setVisibility(View.VISIBLE);
        }
    }

    //decrypt intel at enemy node
    private void decryptIntel() {
        if(activeNode.getColor().equalsIgnoreCase(enemyColor)) {
            if (intel == null && decryptCounter()) {
                //TODO: Functionality - Report that Intel gathered
                //TODO: factor in influence for probability
                Random rand = new Random();
                int odds = rand.nextInt(10);
                if (odds > 5) {
                    intel = activeNode;
                    pop_up.setText("Intel Decrytped!  Deliver it to another WhisperSpot!");
                    pop_up.setVisibility(View.VISIBLE);
                }
                else{
                    pop_up.setText("Your cover was blown while decrypting message! You need to lay low for a bit!");
                    pop_up.setVisibility(View.VISIBLE);
                }
            }
            else if (!decryptCounter()) {
                pop_up.setText("Your cover is blown here! You need to lay low for a bit!");
                pop_up.setVisibility(View.VISIBLE);
            } else if (intel != null) {
                pop_up.setText("You may only carry 1 intel at a time.");
                pop_up.setVisibility(View.VISIBLE);
            }
        }

        else{
            pop_up.setText("You don't need to decrypt intel at allied WhisperSpots");
            pop_up.setVisibility(View.VISIBLE);
        }
    }

    private void returnIntel(){
        if(intel != null){
            //TODO: Check distance from intel source
            float[] res = new float[]{0};
            Location.distanceBetween(intel.getLat(), intel.getLon(), activeNode.getLat(), activeNode.getLon(), res);
            int distance = (int) res[0];
            int influence = 5 + ((int)distance/200);
            captureNodeByPoints(activeNode, influence);
            //TODO: Tell Searing about influence
            intel = null;
            pop_up.setText("Nice Work Agent! You gained"+influence+"Influence over this WhisperSpot");
            pop_up.setVisibility(View.VISIBLE);
        }
        else{
            pop_up.setText("You poor ignorant fool. You have no Intel to offer.");
            pop_up.setVisibility(View.VISIBLE);
        }
    }



    private boolean decryptCounter(){  //counter to stop users trying to decrypt too frequently
        return true;
    }
}