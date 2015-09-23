package com.pdroid84.deb.syncadapterexample;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.pdroid84.deb.syncadapterexample.sync.DebSyncAdapter;


public class MainActivity extends AppCompatActivity implements DebListFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DEB", "MainActivity->onCreate is called");
        setContentView(R.layout.activity_main);
        // Set elevation to ZERO ensures that the Action Bar belends properly with body of the activity
        getSupportActionBar().setElevation(0f);
        //Load the list fragment
        DebListFragment mDebListFragment =  ((DebListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        Log.d("DEB", "MainActivity->about to start the SyncAdapter...");
        DebSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("DEB", "MainActivity->onOptionsItemSelected is called");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("DEB", "MainActivity->Setting is clicked");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        Log.d("DEB", "MainActivity->onItemSelected is called");
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(dateUri);
        startActivity(intent);
    }
}
