package com.pdroid84.deb.syncadapterexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pdroid84.deb.syncadapterexample.gcm.DebRegistrationIntentService;
import com.pdroid84.deb.syncadapterexample.sync.DebSyncAdapter;


public class MainActivity extends AppCompatActivity implements DebListFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTablet;
    private String mLocation;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging mGcm;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final String TAG = "MainActivity-->";
    private static final String LOG_TAG = "DEB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);
        Log.d(LOG_TAG, TAG+"onCreate is called");
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTablet = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTablet = false;
            // Set elevation to ZERO ensures that the Action Bar belends properly with body of the activity
            getSupportActionBar().setElevation(0f);
        }

        DebListFragment debListFragment =  ((DebListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_debList));
        debListFragment.setUseTodayLayout(!mTablet);
        Log.d(LOG_TAG, TAG+"about to start the SyncAdapter...");
        DebSyncAdapter.initializeSyncAdapter(this);

        //Start the Broadcast Receiver for GCM service
        startBoradcastReceiver();
        // If Google Play Services is not available, some features, such as GCM-powered weather
        // alerts, will not be available.
        if (checkPlayServices()) {
            Intent intent = new Intent(this,DebRegistrationIntentService.class);
            startService(intent);
        } else {
            Log.i(LOG_TAG, TAG+"No valid Google Play Services APK. Weather alerts will be disabled.");
            // Store regID as null
            storeRegistrationId(this, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, TAG+"onOptionsItemSelected is called");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(LOG_TAG, TAG+"Setting is clicked");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If Google Play Services is not available, some features, such as GCM-powered weather
        // alerts, will not be available.
        //It is advisable by Google to check this both in onCreate() and onResume()
        if(!checkPlayServices()) {
            //set the regID as null
            storeRegistrationId(this, null);
        }
        //Register the broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(DebRegistrationIntentService.REGISTRATION_COMPLETE));

        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            DebListFragment dlf = (DebListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_debList);
            if ( dlf != null ) {
                dlf.onLocationChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    protected void onPause() {
        //Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        Log.d(LOG_TAG, TAG+"onItemSelected is called");
        if (mTablet) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, dateUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(LOG_TAG, TAG+"This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Register the Google Colud Messaging Service using BroadcastReceiver
     */
    private void startBoradcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String token = intent.getStringExtra(PROPERTY_REG_ID);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(DebRegistrationIntentService.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(LOG_TAG,TAG+"Token received: "+token);
                    storeRegistrationId(context, token);
                } else {
                    Log.d(LOG_TAG,TAG+"Token not received successfully, going to setup null...");
                    storeRegistrationId(context, null);
                }
            }
        };
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(LOG_TAG, TAG+"GCM Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOG_TAG, TAG+"App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(LOG_TAG, TAG + "Saving regId " + regId + " on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // Here the registration ID in shared preferences, but
        // how it is to be stored depends on developer. Just make sure
        // that it is private!
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            int retPkg = packageInfo.versionCode;
            Log.d(LOG_TAG,TAG+"The app version is "+retPkg);
            return retPkg;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen. WHAT DID YOU DO?!?!
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
