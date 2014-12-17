package com.example.davelkan.mapv2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
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
import java.util.Set;

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
    private int userPoints = 0; // TODO: store this in Firebase
    private int captureBonus = 10;
    private Node activeNode;
    private Node intel = null;
    private Toast oldToast = null;
    private LatLng olin = new LatLng(42.2929, -71.2615);
    private SharedPreferences preferences;
    private Set<String> visitedNodes;
    private boolean devMode = true;

    Button leave_intel;
    Button take_intel;
    Button leave_trap;
    Button decrypt_intel;
    TextView pop_up;
    int mapState = 0;
    public Marker myLocation;
    int zoom = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.i("STARTUP", "====================================");
        Firebase.setAndroidContext(this);
        setupFirebase();
        initButtons();
        preferences = getSharedPreferences("whisperspot", Context.MODE_PRIVATE);
//        preferences.edit().remove("visitedNodes").apply();
        visitedNodes = preferences.getStringSet("visitedNodes", new HashSet<String>());
        setUpMapIfNeeded();
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
        resetFirebase(false);
        firebaseUtils.populateNodes(this);
    }

    // wipes all nodestats from the Firebase
    private void resetFirebase(boolean confirm) {
        if (confirm) {
            createNewNode("BC:6A:29:AE:DA:C1", "blue", new LatLng(42.293307, -71.263748));
            createNewNode("78:A5:04:8C:25:DF", "red", new LatLng(42.29372, -71.264478));
            createNewNode("D4:0E:28:2D:C5:B2", "blue", new LatLng(42.292671, -71.262174));
            createNewNode("CD:19:99:D7:B5:8E", "red", new LatLng(42.292728, -71.263475));
            createNewNode("device0", "blue", new LatLng(42.292333, -71.262797));
            createNewNode("device1", "red", new LatLng(42.293091, -71.2626));
        }
    }

    // scans for a Bluetooth device
    public void runScanner(String device) {
        scanner = new BLEScanner(this);
        scanner.scanBLE(device);
    }

    // adds a new device to Firebase, or updates current device's information
    private void createNewNode(String device, String color, LatLng center) {
        List<Owner> thisOwnersList = new ArrayList<>();
        thisOwnersList.add(new Owner("initialAlly", 150));
        List<Owner> otherOwnersList = new ArrayList<>();
        otherOwnersList.add(new Owner("initialEnemy", 50));

        HashMap<String, List<Owner>> owners = new HashMap<>();
        owners.put(color, thisOwnersList);
        owners.put(Node.getOtherColor(color), otherOwnersList);

        firebaseUtils.pushNode(new Node(device, color, 50, center, owners));
    }

    // adds a node to list of nodes and draws on map
    public void addNode(Node node) {
        if (nodes.get(node.getColor()) == null) {
            nodes.put(node.getColor(), new ArrayList<Node>());
        }
        nodes.get(node.getColor()).add(node);

        if (visitedNodes.contains(node.getDevice())) {
            drawNode(node);
        }
    }

    public void drawNode(Node node) {
        mMap.addCircle(new CircleOptions()
                .center(node.getCenter())
                .radius(25)
//                .strokeColor(Color.TRANSPARENT)
                .strokeColor(node.getAllyColor())
//                .strokeColor(node.getEnemyColor())
//                .fillColor(node.getAllyColor()));
                .fillColor(Color.TRANSPARENT));
    }

    // updates a node's information with new data
    public void updateNode(Node node, RawNode data) {
        if (!node.getColor().equalsIgnoreCase(data.getColor())) { // new color
            updateNodeColor(node, node.getColor());
        }
        node.update(data);
    }

    // tells node that this user is trying to capture it
    public void captureNodeByPoints(Node node, int points) {
        CaptureResult result = node.captureByPoints(userName, allyColor, points);
        userPoints += result.getUsedPoints();
        toastify("you: " + userPoints + "; node: " + node.getColor() + " " + node.getOwnership());
        if (result.getWasCaptured()) {
            updateNodeColor(node, allyColor);
            userPoints += captureBonus;
        }
        firebaseUtils.pushNode(node);
    }

    public void updateNodeColor(Node node, String newColor) {
        if (nodes.get(newColor) == null) { // add new color to nodes information
            nodes.put(newColor, new ArrayList<Node>());
        }
        nodes.get(node.getColor()).remove(node);
        nodes.get(newColor).add(node);
        // TODO: reflect color change on map
    }

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
        devMode = newValue;
    }

    public boolean getDevMode() {
        return devMode;
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
    private void setUpMapIfNeeded() {
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
        } else { // this is just so it zooms in on Olin even if it finds nothing
            zoomTo(olin, zoom);
        }
    }

    public void updateLocation(LatLng latLng) {
        if (myLocation != null) {
            myLocation.remove();
        }
        // TODO: Check if near undiscovered node only display those known
        myLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("My Position"));

        Node foundNode = checkAllyProximity(latLng);
        if (foundNode == null) foundNode = checkEnemyProximity(latLng);
        if (activeNode != null && activeNode != foundNode) {
            toastify("left " + activeNode.getDevice());
        }
        if (foundNode == null) { // didn't find a node
            activeNode = null;
            Log.i("LOCATION UPDATE", "NOT IN A NODE");
            mapState = 0;
            makeInvisible();
        } else { // found a node
            if (!foundNode.equals(activeNode)) {
                if (!visitedNodes.contains(foundNode.getDevice())) {
                    visitedNodes.add(foundNode.getDevice());
                    preferences.edit().remove("visitedNodes").apply();
                    preferences.edit().putStringSet("visitedNodes", visitedNodes).apply();
                    drawNode(foundNode);
                }
                toastify("entered " + foundNode.getDevice());
                firebaseUtils.pullNode(this, foundNode);
                //  activity.runScanner(foundNode.getDevice());
            }
            Log.i("LOCATION UPDATE", "IN NODE: " + foundNode.getDevice());
            activeNode = foundNode;
        }
    }

    private void zoomTo(LatLng latLng, int zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void zoomTo(Node node, int zoom) {
        zoomTo(node.getCenter(), zoom);
    }

    private void zoomTo(Location location, int zoom) {
        zoomTo(new LatLng(location.getLatitude(), location.getLongitude()), zoom);
    }

    private void zoomBelow(Node node) {
        zoomTo(new LatLng(node.getLat() - 0.00025, node.getLon()), 19);
    }

    //check to see if you're in a node
    public Node checkAllyProximity(LatLng latLng) {
        Node activeNode = checkProximity(nodes.get(allyColor), latLng);
        if (activeNode != null) {
            leave_intel.setVisibility(View.VISIBLE);
            take_intel.setVisibility(View.VISIBLE);
            zoomTo(activeNode, 19);
            mapState = 1;
        }
        return activeNode;
    }

    public Node checkEnemyProximity(LatLng latLng) {
        Node activeNode = checkProximity(nodes.get(enemyColor), latLng);
        if (activeNode != null) {
            //leave_trap.setVisibility(View.VISIBLE);
            decrypt_intel.setVisibility(View.VISIBLE);
            mapState = 1;
        }
        return activeNode;
    }

    private Node checkProximity(List<Node> nodes, LatLng latLng) {
        if (nodes == null) {
            return null;
        }
        if (latLng == null) {
            return null;
        }
        for (Node activeNode : nodes) {
            if (getDistance(activeNode.getCenter(), latLng) < 25 && mapState < 2) {
                System.out.print("in Range");
                return activeNode;
            }
        }
        return null;
    }

    private float getDistance(LatLng start, LatLng end) {
        float[] res = new float[]{0};
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, res);
        return res[0];
    }

    public void initButtons() {
        leave_intel = (Button) findViewById(R.id.lvmsg);
        take_intel = (Button) findViewById(R.id.tkmsg);
        leave_trap = (Button) findViewById(R.id.lvtrp);
        decrypt_intel = (Button) findViewById(R.id.dcptmsg);
        pop_up = (TextView) findViewById(R.id.popUp);

        leave_intel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel_message.setVisibility(View.VISIBLE);
                //submit_message.setVisibility(View.VISIBLE);
                //message.setVisibility(View.VISIBLE);
                take_intel.setVisibility(View.INVISIBLE);
                leave_intel.setVisibility(View.INVISIBLE);
                mapState = 2;
                returnIntel();

                //zoomBelowNode();
            }
        });
       take_intel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               take_intel.setVisibility(View.INVISIBLE);
               leave_intel.setVisibility(View.INVISIBLE);
               mapState = 2;
               //display_message.setText("");
               //display_message.setVisibility(View.VISIBLE);
               //(new FirebaseUtils()).displayMostRecentMessage("78:A5:04:8C:25:DF", display_message);
               gatherIntel();
           }
       });
        decrypt_intel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decryptIntel();
            }
        });
        leave_trap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        pop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop_up.setVisibility(View.INVISIBLE);
                //leave_message.setVisibility(View.VISIBLE);
            }
        });
    }

    public void makeInvisible() {
        leave_trap.setVisibility(View.INVISIBLE);
        decrypt_intel.setVisibility(View.INVISIBLE);
        leave_intel.setVisibility(View.INVISIBLE);
        take_intel.setVisibility(View.INVISIBLE);
        pop_up.setVisibility(View.INVISIBLE);
    }

    private void updatePoints() {
        System.out.print("words");
    }

    private void gatherIntel() { // gathering intel at allied node... checking node color may be unnecessary
        if (activeNode == null) {
            pop_up.setText("Return to the node to gather intel!");
            pop_up.setVisibility(View.VISIBLE);
        } else if (activeNode.getColor().equalsIgnoreCase(allyColor)) {
            if (intel == null) {
                pop_up.setText("Intel Gathered!  Deliver it to another WhisperSpot!");
                pop_up.setVisibility(View.VISIBLE);
                //TODO: Fanciness - add fancy fake number generator
                intel = activeNode;
            } else {
                pop_up.setText("You may only carry 1 intel at a time.");
                pop_up.setVisibility(View.VISIBLE);
            }
        } else {
            pop_up.setText("This is enemy territory! You have to decrypt intel here!");
            pop_up.setVisibility(View.VISIBLE);
        }
    }

    //decrypt intel at enemy node
    private void decryptIntel() {
        if (activeNode == null) {
            pop_up.setText("Return to the node to decrypt intel!");
            pop_up.setVisibility(View.VISIBLE);
        } else if (activeNode.getColor().equalsIgnoreCase(enemyColor)) {
            if (intel == null && decryptCounter()) {
                Random rand = new Random();
                int odds = rand.nextInt(100);
                int ownership = activeNode.getOwnership();
                if (odds > ownership - 5) {
                    intel = activeNode;
                    pop_up.setText("Intel Decrypted!  Deliver it to another WhisperSpot!");
                    pop_up.setVisibility(View.VISIBLE);
                } else {
                    pop_up.setText("Your cover was blown while decrypting message! You need to lay low for a bit!");
                    pop_up.setVisibility(View.VISIBLE);
                }
            } else if (!decryptCounter()) {
                pop_up.setText("Your cover is blown here! You need to lay low for a bit!");
                pop_up.setVisibility(View.VISIBLE);
            } else if (intel != null) {
                pop_up.setText("You may only carry 1 intel at a time.");
                pop_up.setVisibility(View.VISIBLE);
            }
        } else {
            pop_up.setText("You don't need to decrypt intel at allied WhisperSpots");
            pop_up.setVisibility(View.VISIBLE);
        }
    }

    private void returnIntel() {
        if (intel == null) {
            pop_up.setText("You poor ignorant fool. You have no Intel to offer.");
            pop_up.setVisibility(View.VISIBLE);
        } else if (activeNode == null) {
            pop_up.setText("Return to the node to return intel!");
            pop_up.setVisibility(View.VISIBLE);
        } else {
            int distance = (int) getDistance(intel.getCenter(), activeNode.getCenter());

            int influence = 5 + distance / 200;
            captureNodeByPoints(activeNode, influence);
            intel = null;
            pop_up.setText("Nice Work Agent! You gained " + influence + " Influence over this WhisperSpot");
            pop_up.setVisibility(View.VISIBLE);
        }
    }


    private boolean decryptCounter() {  //counter to stop users trying to decrypt too frequently
        return true;
    }
}