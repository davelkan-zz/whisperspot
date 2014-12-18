package com.example.davelkan.mapv2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

import com.example.davelkan.mapv2.util.Node;
import com.example.davelkan.mapv2.util.NodeInfoFragment;
import com.example.davelkan.mapv2.util.NodeMap;
import com.example.davelkan.mapv2.util.RawNode;
import com.example.davelkan.mapv2.util.User;
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
import java.util.Set;

public class MapsActivity extends FragmentActivity {
    private static String TAG = "MapsActivity";
    private static LatLng OLIN = new LatLng(42.2929, -71.2615);
    private Menu menu;
    public String APP_NAME = "Whisperspot";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private BLEScanner scanner;
    private FirebaseUtils firebaseUtils;
    private NodeMap nodes = new NodeMap(this);
    private User user;
    private Node activeNode;
    private Intel intel;
    private Toast oldToast = null;
    private SharedPreferences preferences;
    private Set<String> visitedDevices;
    private boolean devMode = false;
    private boolean nodeInfo = false;
    //private boolean dontZoom = true;

    Button leave_intel;
    Button take_intel;
    Button leave_trap;
    Button decrypt_intel;
    TextView pop_up;
    TextView about;
    Spinner node_selector;
    TextView nodeStats;
    ProgressBar ownerBar;
    int mapState = 0;
    public Marker myLocation;
    int zoom = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.i("STARTUP", "====================================");
        Firebase.setAndroidContext(this);
        firebaseUtils = new FirebaseUtils(nodes);
        initPreferences();
        initUser();
        initButtons();
        initMap();

        // stuff for main activity
//        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.container, new MapsFragment())
//                    .commit();
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        scanner.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMap();
    }

    // this goes in main activity
    public void switchFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }




    // APP-WIDE -- should go in main Activity?  So MapsActivity should be a fragment?




    public FirebaseUtils getFirebaseUtils() {
        return firebaseUtils;
    }

    //    Make sure toasts don't stack (cancel previous toast before creating new one)
    public void toastify(String text) {
//        if (oldToast != null) oldToast.cancel();
        oldToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        oldToast.show();
    }

    public void setDevMode(boolean newValue) {
//        toastify("Dev mode set to " + (newValue?"ON":"OFF") + " from " + (devMode?"ON":"OFF"));
        devMode = newValue;
    }

    public boolean getDevMode() {
        return devMode;
    }

    private void initPreferences() {
        preferences = getSharedPreferences("whisperspot", Context.MODE_PRIVATE);
        visitedDevices = preferences.getStringSet("visitedNodes", new HashSet<String>());
    }

    private void initUser() {
        Intent intent = getIntent();
        String userName = intent.getStringExtra(InitialSetup.USERNAME);
        String color = intent.getStringExtra(InitialSetup.TEAM);

        Log.i(TAG, "Username: " + userName);
        Log.i(TAG, "Team Color: " +  color);

        user = new User(userName, color);
        intel = new Intel(user);
        firebaseUtils.retrieveUser(userName, user, preferences);
    }

    // goes in fragment to switch fragments
    public void goToFragmentForNode(Node node) {
//        ((MainActivity) getActivity()).switchFragment(new NodeInfoFragment());
    }

    // scans for a Bluetooth device
    public void runScanner(String device) {
        scanner = new BLEScanner(this);
        scanner.scanBLE(device);
    }



    // MAP -- Stays in this class, and should be some of the only stuff here



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK
     * on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void initMap() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMapLongClickListener(Listeners.getOnMapLongClickListener(this));

        LocationListener locationListener = Listeners.getLocationListener(this);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            locationListener.onLocationChanged(location);
            zoomTo(location, zoom);
        } else { // this is just so it zooms in on Olin if phone doesn't know where it is
            zoomTo(OLIN, zoom);
        }
    }

    //helper function to zoom to a specific location at a specific zoom using LatLng
    private void zoomTo(LatLng latLng, int zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    //helper function to zoom to a specific location at a specific zoom using our Node class
    private void zoomTo(Node node, int zoom) {
        zoomTo(node.getCenter(), zoom);
    }

    //helper function to zoom to a specific location at a specific zoom using our Location
    private void zoomTo(Location location, int zoom) {
        zoomTo(new LatLng(location.getLatitude(), location.getLongitude()), zoom);
    }

    //Zooms to and slightly below a node using Node class - used to put the node in frame while viewing nodeStats
    private void zoomBelow(Node node) {
        zoomTo(new LatLng(node.getLat() - 0.00025, node.getLon()), 19);
    }

    public void drawNode(Node node) {
        if (mMap != null && visitedDevices.contains(node.getDevice())) {
            mMap.addCircle(new CircleOptions()
                    .center(node.getCenter())
                    .radius(25)
                    .strokeColor(node.getAllyColor())
                    .fillColor(Color.TRANSPARENT));
        }
    }



    // LOCATION -- stays in this class, but maybe updateLocation should stay in its listener?
    // If so, how should we access all the MapsActivity variables it uses?



    public void updateLocation(LatLng latLng) {
        if (myLocation != null) {
            myLocation.remove();
        }
        myLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("My Position"));

        Node foundNode = checkAllyProximity(latLng);
        if (foundNode == null) foundNode = checkEnemyProximity(latLng);
        if (activeNode != null && activeNode != foundNode) {
            toastify("left " + activeNode.getName());
            mapState = 0;
            makeInvisible();
        }
        if (foundNode == null) { // didn't find a node
            activeNode = null;
            Log.i("LOCATION UPDATE", "NOT IN A NODE");
            mapState = 0;
            makeInvisible();
        } else { // found a node
            if (foundNode.getColor().equals(user.getColor())) {
                mapState = 1;
            } else {
                mapState = 2;
            }
            if (!foundNode.equals(activeNode)) {
                zoomTo(foundNode, 19);
                if (!visitedDevices.contains(foundNode.getDevice())) {
                    visitedDevices.add(foundNode.getDevice());
                    preferences.edit().remove("visitedNodes").apply();
                    preferences.edit().putStringSet("visitedNodes", visitedDevices).apply();
                    drawNode(foundNode);
                }
                toastify("entered " + foundNode.getName());
                firebaseUtils.pullNode(nodes, foundNode);
                //  activity.runScanner(foundNode.getDevice());
            }
            Log.i("LOCATION UPDATE", "IN NODE: " + foundNode.getDevice());
            activeNode = foundNode;
        }
        displayButtons(mapState);
    }

    //check to see if you're in a node
    public Node checkAllyProximity(LatLng latLng) {
        return nodes.checkProximity(user.getColor(), latLng);
    }

    //Check to see if you're in an enemy node
    public Node checkEnemyProximity(LatLng latLng) {
        return nodes.checkProximity(user.getEnemyColor(), latLng);
    }



    // BUTTON INTERFACE -- should all be in another fragment (putting buttons on top of map == bad)



    public void initButtons() {
        leave_intel = (Button) findViewById(R.id.lvmsg);
        take_intel = (Button) findViewById(R.id.tkmsg);
        leave_trap = (Button) findViewById(R.id.lvtrp);
        decrypt_intel = (Button) findViewById(R.id.dcptmsg);
        pop_up = (TextView) findViewById(R.id.popUp);
        node_selector = (Spinner) findViewById(R.id.node_selector);
        about = (TextView) findViewById(R.id.about);
        nodeStats = (TextView) findViewById(R.id.nodeStats);
        ownerBar = (ProgressBar) findViewById(R.id.ownerBar);
        ownerBar.setMax(100);
        about.setText("Username: " + user.getName() + "\nFaction: " + user.getColor());
        // Create an ArrayAdapter using the string array and a default spinner layout
        //TODO: Convert visited hashmap to Array usable by array adapter
        if(visitedDevices == null) {
            Log.i("pipe", "FUCK FUCK FUCK FUCK");
        }
        //TODO: identify node selected
        //TODO: modify nodeStats Textview based on identified node
        //TODO: show nodeStats TextView and zoom on selected node

        take_intel.setOnClickListener(Listeners.gatherIntel(this));
        leave_intel.setOnClickListener(Listeners.deliverIntel(this));
        decrypt_intel.setOnClickListener(Listeners.decryptIntel(this));
        pop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop_up.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void deliverIntel() {
        popUp(intel.deliverIntel(activeNode, nodes));
        getFirebaseUtils().pushNode(activeNode);
        getFirebaseUtils().pushUser(user);
        toastify("you: " + user.getPoints() + "; " + activeNode.getName() + ": " +
                activeNode.getColor() + " " + activeNode.getOwnership());
    }



    // DISPLAY -- should also be in button/node info display fragment


    public void setMapState(int state) {
        mapState = state;
    }

    public void displayButtons(int state) {
        makeInvisible(); //Start from scratch, only enable buttons
        if (state == 0) { //Not in a node, no buttons
        } else if (state == 1) { //In an ally Node
            leave_intel.setVisibility(View.VISIBLE);
            take_intel.setVisibility(View.VISIBLE);
        } else if (state == 2) { //In an enemy Node
            leave_intel.setVisibility(View.VISIBLE);
            decrypt_intel.setVisibility(View.VISIBLE);
        } else if (state == 3) { //Just left intel, took intel, or attempted to decrypt
        }
    }

    public void makeInvisible() {
        leave_trap.setVisibility(View.INVISIBLE);
        decrypt_intel.setVisibility(View.INVISIBLE);
        leave_intel.setVisibility(View.INVISIBLE);
        take_intel.setVisibility(View.INVISIBLE);
        pop_up.setVisibility(View.INVISIBLE);
    }

    public void popUp(String text) {
        pop_up.setText(text);
        pop_up.setVisibility(View.VISIBLE);
    }

    private void setNodeInfo(boolean value) {
        nodeInfo = value;
        int visibility = (value)?View.VISIBLE:View.INVISIBLE;
        about.setVisibility(visibility);
        node_selector.setVisibility(visibility);
        nodeStats.setVisibility(visibility);
        ownerBar.setVisibility(visibility);
        if (value) initNodeStates();
    }



    // ACTION BAR -- Listeners should be moved to Listeners class, but how do we access all the visibility stuff?



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    private void setOptionTitle(int id, String title) {
        MenuItem item = menu.findItem(id);
        item.setTitle(title);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_toggle_node_info:
                setOptionTitle(R.id.menu_toggle_node_info, "Turn Node Info " + (nodeInfo?"ON":"OFF"));
                setNodeInfo(!nodeInfo);
                return true;
            case R.id.menu_toggle_dev_mode:
                setOptionTitle(R.id.menu_toggle_dev_mode, "Turn Dev Mode " + (devMode?"ON":"OFF"));
                setDevMode(!devMode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Initiates nodeStats window and calls function to populate it
    private void initNodeStates(){
        ArrayAdapter<String> adapter = new ArrayAdapter<> (this,android.R.layout.simple_spinner_item,new ArrayList<>(visitedDevices));
        // Apply the adapter to the spinner(dropdown)
        node_selector.setAdapter(adapter);
        //used a listener to check if new item selected
        node_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFromList = node_selector.getItemAtPosition(position).toString();
                //Call the functions that actually populate the NodeStats Window
                if(!showNodeStats(selectedFromList,"red")) {
                    showNodeStats(selectedFromList, "blue");
                }
            }
            //nodeStats.setText(selectedFromList);
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //populates the nodeStats window to inform users about the nodes.
    private Boolean showNodeStats(String selectedFromList, String faction){
        for (Node node : nodes.get(faction)) {
            if (selectedFromList.equals(node.getDevice())) {
                zoomBelow(node);
                if (faction.equalsIgnoreCase("blue")) {
                    faction = "Blue Fedoras";
                    ownerBar.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    //the following attribute is lollipop-only.  Only enable if user is using 21 or later
                    if (Build.VERSION.SDK_INT >= 21) {
                        ownerBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    }

                } else if (faction.equalsIgnoreCase("red")) {
                    faction = "Red Bowlers";
                    ownerBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    //the following attribute is lollipop-only.  Only enable if user is using 21 or later
                    if (Build.VERSION.SDK_INT >= 21) {
                        ownerBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                    }
                }
                nodeStats.setText("\n \n \n" + node.getCenter().toString() + "\n" + "Controlling Faction: " + faction + " \n \n Controlling Influence: ");
                int percentage = (node.getOwnership());
                ownerBar.setProgress(percentage);
                return true;
            }
        }
        return false;
    }

    public Intel getIntel() {
        return intel;
    }

    public Node getActiveNode() {
        return activeNode;
    }
}