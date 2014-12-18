package com.example.davelkan.mapv2;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


public class InitialSetup extends FragmentActivity {
    private final static String TAG = "InitialSetup";
    public final static String USERNAME = "com.example.davelkan.mapv2.USERNAME";
    public final static String TEAM = "com.example.davelkan.mapv2.TEAM";
    private SharedPreferences preferences;
    public ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("SETUP", "====================================");

        preferences = getSharedPreferences("whisperspot", Context.MODE_PRIVATE);

        checkUserExists();
        
        actionBar = getActionBar();
        try {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            Log.i(TAG, e.toString());
        }

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
        if (userName != null && color != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(USERNAME, userName);
            intent.putExtra(TEAM, color);
            Log.i(TAG, "Create user: " + userName);
            startActivity(intent);
        }
    }

    public void joinBowler(View view) {
        joinTeam(view, "red");
    }

    public void joinFedora(View view) {
        joinTeam(view, "blue");
    }

    public void joinTeam(View view, String color) {
        String userName = checkUserName();
        if (!userName.equals("")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(USERNAME, userName);
            intent.putExtra(TEAM, color);

            preferences.edit().remove("username").apply();
            preferences.edit().remove("color").apply();
            preferences.edit().putString("username", userName).apply();
            preferences.edit().putString("color", color).apply();
            (new FirebaseUtils()).createUser(userName, color);

            if (color.equals("blue")) {
                Log.i(TAG, "fedora join");
            } else if (color.equals("red")) {
                Log.i(TAG, "bowler join");
            }
            startActivity(intent);
        }
    }

    public String checkUserName() {
        EditText nameEdit;
        String userName;

        nameEdit = (EditText)findViewById(R.id.username);
        userName = nameEdit.getText().toString();
        if (userName.equals("")) {
            Toast.makeText(this, "You did not enter a username", Toast.LENGTH_SHORT).show();
            return "";
        }
        else {
            Log.i("user", nameEdit.getText().toString());
            return userName;
        }
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
            return inflater.inflate(R.layout.fragment_initial_setup, container, false);
        }
    }
}
