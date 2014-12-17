package com.example.davelkan.mapv2;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;


public class InitialSetup extends FragmentActivity {
    public final static String USERNAME = "com.example.davelkan.mapv2.USERNAME";
    public final static String TEAM = "com.example.davelkan.mapv2.TEAM";
    private SharedPreferences preferences;
    private FirebaseUtils firebaseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("SETUP", "====================================");

        Firebase.setAndroidContext(this);
        firebaseUtils = new FirebaseUtils();
        preferences = getSharedPreferences("whisperspot", Context.MODE_PRIVATE);

        checkUserExists();

        setContentView(R.layout.activity_initial_setup);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public void checkUserExists() {
        String userName = preferences.getString("username", null);
        String color = preferences.getString("color", null);
        Log.i("NOOOOOOTTTTTHEEEEEEEYYYY", "bleh");
        if (userName != null && color != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra(USERNAME, userName);
            intent.putExtra(TEAM, color);
            Log.i("HEEEEEEEEEEEEYYYYYY", userName);
            startActivity(intent);
        }
    }

    public void joinBowler(View view) {
        String userName = checkUserName();
        if (userName != "") {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra(USERNAME, userName);
            intent.putExtra(TEAM, "red");

            preferences.edit().remove("username").apply();
            preferences.edit().remove("color").apply();
            preferences.edit().putString("username", userName).apply();
            preferences.edit().putString("color", "red").apply();
            firebaseUtils.createUser(userName, "red");

            Log.i("bowler join", "bowler!!!");
            startActivity(intent);
        };
    }

    public void joinFedora(View view) {
        String userName = checkUserName();
        if (userName != "") {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra(USERNAME, userName);
            intent.putExtra(TEAM, "blue");

            preferences.edit().remove("username").apply();
            preferences.edit().remove("color").apply();
            preferences.edit().putString("username", userName).apply();
            preferences.edit().putString("color", "blue").apply();
            firebaseUtils.createUser(userName, "blue");

            Log.i("fedora join", "fedora!!!");
            startActivity(intent);
        };
    }

    public String checkUserName() {
        EditText nameEdit;
        String userName;

        nameEdit = (EditText)findViewById(R.id.username);
        userName = nameEdit.getText().toString();
        if (userName.matches("")) {
            Toast.makeText(this, "You did not enter a username", Toast.LENGTH_SHORT).show();
            return "";
        }
        else {
            Log.i("user", nameEdit.getText().toString());
            return userName;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initial_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_initial_setup, container, false);
            return rootView;
        }
    }
}
