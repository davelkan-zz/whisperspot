package com.example.davelkan.mapv2;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.davelkan.mapv2.util.Node;
import com.example.davelkan.mapv2.util.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment {
    private static LatLng OLIN = new LatLng(42.2929, -71.2615);
    public String APP_NAME = "Whisperspot";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapView mapView; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private Node activeNode;

    View rootView;
    MainActivity mainActivity;

    Button leave_intel;
    Button take_intel;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initButtons();
        MapsInitializer.initialize(mainActivity);
        initMap();
    }

    @Override
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        if (mapView != null) {
            mapView.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) { //Sometimes this is null returning to the app from intent
            //Log.i(GoogleMapFragment.class.getSimpleName(), "mapView is not null on Destroy()");
            mapView.onDestroy();
        }
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mainActivity.scanner.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        mainActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            Log.i("DebugDebug", "debug");
            mapView = (MapView) rootView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mMap = mapView.getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                Log.i("DebugDebug", "chris");
                initMap();
                setUpMap();
            }
        }

        return rootView;
    }


    public void goToFragmentForNode(Node node) {
//        ((MainActivity) getActivity()).switchFragment(new NodeInfoFragment());
    }


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
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mainActivity.toastify("Please enable GPS for " + APP_NAME + " to work properly.");
            startActivity(myIntent);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near my bedroom.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        this.mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.mMap.getUiSettings().setAllGesturesEnabled(true);
        this.mMap.setOnMapLongClickListener(Listeners.getOnMapLongClickListener(mainActivity, this));
        Log.i("DebugDebug", "hahaha");
        LocationListener locationListener = Listeners.getLocationListener(mainActivity, this);

        // Get the location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
        if (mMap != null && mainActivity.visitedDevices.contains(node.getDevice())) {
            mMap.addCircle(new CircleOptions()
                    .center(node.getCenter())
                    .radius(25)
                    .strokeColor(node.getAllyColor())
                    .fillColor(Color.TRANSPARENT));
        }
    }



    // LOCATION -- stays in this class, but maybe updateLocation should stay in its listener?
    // If so, how should we access all the MapsActivity variables it uses?

    public void updateMarker(LatLng latLng) {
        if (myLocation != null) {
            myLocation.remove();
        }
        myLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("My Position"));
    }

    public void enterNewNode(Node foundNode) {
        zoomTo(foundNode, 19);
        if (!mainActivity.visitedDevices.contains(foundNode.getDevice())) {
            mainActivity.visitedDevices.add(foundNode.getDevice());
            mainActivity.preferences.edit().remove("visitedNodes").apply();
            mainActivity.preferences.edit().putStringSet("visitedNodes", mainActivity.visitedDevices).apply();
            drawNode(foundNode);
        }
        mainActivity.toastify("entered " + foundNode.getName());
        mainActivity.firebaseUtils.pullNode(mainActivity.nodes, foundNode);
    }

    //check to see if you're in a node
    public Node checkAllyProximity(LatLng latLng) {
        return mainActivity.nodes.checkProximity(mainActivity.user.getColor(), latLng);
    }

    //Check to see if you're in an enemy node
    public Node checkEnemyProximity(LatLng latLng) {
        return mainActivity.nodes.checkProximity(mainActivity.user.getEnemyColor(), latLng);
    }



    // BUTTON INTERFACE -- should all be in another fragment (putting buttons on top of map == bad)



//    public void initButtons() {
//        leave_intel = (Button) rootView.findViewById(R.id.lvmsg);
//        take_intel = (Button) rootView.findViewById(R.id.tkmsg);
//        leave_trap = (Button) rootView.findViewById(R.id.lvtrp);
//        decrypt_intel = (Button) rootView.findViewById(R.id.dcptmsg);
//        pop_up = (TextView) rootView.findViewById(R.id.popUp);
//        node_selector = (Spinner) rootView.findViewById(R.id.node_selector);
//        about = (TextView) rootView.findViewById(R.id.about);
//        nodeStats = (TextView) rootView.findViewById(R.id.nodeStats);
//        ownerBar = (ProgressBar) rootView.findViewById(R.id.ownerBar);
//        ownerBar.setMax(100);
//        about.setText("Username: " + mainActivity.user.getName() + "\nFaction: " + mainActivity.user.getColor());
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        //TODO: Convert visited hashmap to Array usable by array adapter
//        if(mainActivity.visitedDevices == null) {
//            Log.i("pipe", "FUCK FUCK FUCK FUCK");
//        }
//        //TODO: identify node selected
//        //TODO: modify nodeStats Textview based on identified node
//        //TODO: show nodeStats TextView and zoom on selected node
//
//        take_intel.setOnClickListener(Listeners.gatherIntel(this));
//        leave_intel.setOnClickListener(Listeners.deliverIntel(this));
//        decrypt_intel.setOnClickListener(Listeners.decryptIntel(this));
//        pop_up.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pop_up.setVisibility(View.INVISIBLE);
//            }
//        });
//    }

    public void deliverIntel() {
        popUp(mainActivity.intel.deliverIntel(activeNode, mainActivity.nodes));
        if (mainActivity.intel.getNode() != activeNode) {
            mainActivity.getFirebaseUtils().pushNode(activeNode);
            mainActivity.getFirebaseUtils().pushUser(mainActivity.user);
            mainActivity.toastify("you: " + mainActivity.user.getPoints() + "; " + activeNode.getName() + ": " +
                    activeNode.getColor() + " " + activeNode.getOwnership());
        }
    }



    // DISPLAY -- should also be in button/node info display fragment


    public void setMapState(int state) {
        mapState = state;
    }

    public void displayButtons() {
//        makeInvisible(); //Start from scratch, only enable buttons
//        if (mapState == 0) { //Not in a node, no buttons
//        } else if (mapState == 1) { //In an ally Node
//            leave_intel.setVisibility(View.VISIBLE);
//            take_intel.setVisibility(View.VISIBLE);
//        } else if (mapState == 2) { //In an enemy Node
//            leave_intel.setVisibility(View.VISIBLE);
//            decrypt_intel.setVisibility(View.VISIBLE);
//        } else if (mapState == 3) { //Just left intel, took intel, or attempted to decrypt
//        }
    }

    public void makeInvisible() {
        decrypt_intel.setVisibility(View.INVISIBLE);
        leave_intel.setVisibility(View.INVISIBLE);
        take_intel.setVisibility(View.INVISIBLE);
        pop_up.setVisibility(View.INVISIBLE);
    }

    public void popUp(String text) {
        pop_up.setText(text);
        pop_up.setVisibility(View.VISIBLE);
    }

    public void setNodeInfo(boolean value) {
        mainActivity.nodeInfo = value;
        int visibility = (value)?View.VISIBLE:View.INVISIBLE;
//        about.setVisibility(visibility);
//        node_selector.setVisibility(visibility);
//        nodeStats.setVisibility(visibility);
//        ownerBar.setVisibility(visibility);
        if (value) initNodeStates();
    }



    //Initiates nodeStats window and calls function to populate it
    private void initNodeStates(){
        List<String> visitedTitles = new ArrayList<>();
        for(String id : mainActivity.visitedDevices) {
            visitedTitles.add(mainActivity.nodes.getNodeFromDevice(id).getName());
        }

        ArrayAdapter adapter = new ArrayAdapter<> (mainActivity, android.R.layout.simple_spinner_item, visitedTitles);
        // Apply the adapter to the spinner(dropdown)
//        node_selector.setAdapter(adapter);
        //used a listener to check if new item selected
//        node_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedFromList = node_selector.getItemAtPosition(position).toString();
//                //Call the functions that actually populate the NodeStats Window
//                if(!showNodeStats(selectedFromList,"red")) {
//                    showNodeStats(selectedFromList, "blue");
//                }
//            }
//            //nodeStats.setText(selectedFromList);
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }

    //populates the nodeStats window to inform users about the nodes.
    private Boolean showNodeStats(String selectedFromList, String faction){

        for (Node element : mainActivity.nodes.get(faction)) {
            if (selectedFromList.equals(element.getName())) {
                LatLng elementCenter = element.getCenter();
                zoomBelow(element);

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
                nodeStats.setText("\n \n \n" + elementCenter.toString() + "\n" + "Controlling Faction: " + faction + " \n \n Controlling Influence: ");
                int percentage = (element.getOwnership());
                ownerBar.setProgress(percentage);
                return true;
            }
        }
        return false;
    }

    public User getUser() {
        return mainActivity.user;
    }

    public Intel getIntel() {
        return mainActivity.intel;
    }

    public Node getActiveNode() {
        return activeNode;
    }

    public void setActiveNode(Node activeNode) {
        this.activeNode = activeNode;
    }
}