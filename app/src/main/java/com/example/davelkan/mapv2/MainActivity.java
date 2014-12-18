package com.example.davelkan.mapv2;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.davelkan.mapv2.util.NodeMap;
import com.example.davelkan.mapv2.util.User;
import com.firebase.client.Firebase;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity {
    private static String TAG = "MapsActivity";
    public BLEScanner scanner;
    public FirebaseUtils firebaseUtils;
    public NodeMap nodes;
    private Menu menu;
    private Toast oldToast = null;
    private boolean devMode = false;
    public SharedPreferences preferences;
    public Set<String> visitedDevices;
    public Intel intel;
    public User user;
    public boolean nodeInfo = false;
    public MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("STARTUP", "====================================");
        Firebase.setAndroidContext(this);

        mapsFragment = new MapsFragment();
        nodes = new NodeMap(mapsFragment);
        firebaseUtils = new FirebaseUtils(nodes);

        initPreferences();
        initUser();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mapsFragment)
                    .commit();
        }
    }

    public FirebaseUtils getFirebaseUtils() {
        return firebaseUtils;
    }

    public void setDevMode(boolean newValue) {
        devMode = newValue;
    }

    // scans for a Bluetooth device
    public void runScanner(String device) {
        scanner = new BLEScanner(this);
        scanner.scanBLE(device);
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

    //    Make sure toasts don't stack (cancel previous toast before creating new one)
    public void toastify(String text) {
        if (oldToast != null) oldToast.cancel();
        oldToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        oldToast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
//            case R.id.menu_toggle_node_info:
//                setOptionTitle(R.id.menu_toggle_node_info, "Turn Node Info " + (nodeInfo?"ON":"OFF"));
//                mapsFragment.setNodeInfo(!nodeInfo);
//                return true;
            case R.id.menu_toggle_dev_mode:
                setOptionTitle(R.id.menu_toggle_dev_mode, "Turn Dev Mode " + (devMode?"ON":"OFF"));
                setDevMode(!devMode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
