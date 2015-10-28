package com.pdroid84.deb.syncadapterexample;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.pdroid84.deb.syncadapterexample.data.DebContract;
import com.pdroid84.deb.syncadapterexample.sync.DebSyncAdapter;

/**

 */
public class DebListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private DebAdapter mDebAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;
    private static final int DEB_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            // List of columns we want to retrieve
            DebContract.DebWeatherFields._ID,
            DebContract.DebWeatherFields.COLUMN_CITY,
            DebContract.DebWeatherFields.COLUMN_DATE,
            DebContract.DebWeatherFields.COLUMN_WEATHER_ID,
            DebContract.DebWeatherFields.COLUMN_SHORT_DESC,
            DebContract.DebWeatherFields.COLUMN_MAX_TEMP,
            DebContract.DebWeatherFields.COLUMN_MIN_TEMP
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_CITY = 1;
    static final int COL_WEATHER_DATE = 2;
    static final int COL_WEATHER_ID = 3;
    static final int COL_WEATHER_DESC = 4;
    static final int COL_WEATHER_MAX_TEMP = 5;
    static final int COL_WEATHER_MIN_TEMP = 6;

    public DebListFragment (){
        Log.d("DEB", "DebListFragment ---> Constructor is called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("DEB", "DebListFragment ---> onCreateView is called");
        // Get the data from the source Adapter
        mDebAdapter = new DebAdapter(getActivity(),null,0);

        //Inflate the layout of the fragment
        View parentView = inflater.inflate(R.layout.deb_fragment_main, container, false);

        //Get a reference to the ListView
        mListView = (ListView) parentView.findViewById(R.id.listview_forecast);
        //Set the empty view with list view so that it's data appears only when the list is empty
        mListView.setEmptyView(parentView.findViewById(R.id.empty_list_text_view));

        //Attach the adapter to the ListView
        mListView.setAdapter(mDebAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Cursor mCursor = (Cursor) adapterView.getItemAtPosition(pos);
                //This part is to do the needful for detail display
                if (mCursor != null) {
                    String locationCity = Utility.getPreferredLocation(getActivity());
                    //Commenting the Toast
                    //Toast.makeText(getActivity(), "Item selected is " + pos + " & Location = " + locationCity, Toast.LENGTH_LONG).show();
                    ((Callback) getActivity())
                            .onItemSelected(DebContract.DebWeatherFields.buildWeatherLocationWithDate(
                                    locationCity, mCursor.getLong(COL_WEATHER_DATE)
                            ));
                }
            }
        });
        return parentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("DEB", "DebListFragment ---> onActivityCreated is called");
        getLoaderManager().initLoader(DEB_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        //Register the SharedPreference Listner
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        //Unregister the SharedPreference Listener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("DEB", "DebListFragment ---> onCreateLoader is called");
        // Sort order:  Ascending, by date.
        String sortOrder = DebContract.DebWeatherFields.COLUMN_DATE + " ASC";

        //Location is now read from sharedpreference
        //String locationCity = "Newcastle";
        String locationCity = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = DebContract.DebWeatherFields.buildWeatherLocationWithStartDate(
                locationCity, System.currentTimeMillis());
        Log.d("DEB", "DebListFragment ---> weatherForLocationUri: " + weatherForLocationUri.toString());
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("DEB", "DebListFragment ---> onLoadFinished is called");
        mDebAdapter.swapCursor(data);
        if(mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
        updateNoDataMessage();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("DEB", "DebListFragment ---> onLoaderReset is called");
        mDebAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mDebAdapter != null) {
            mDebAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        Log.d("DEB", "DebListFragment ---> onLocationChanged is called");
        updateWeather();
        getLoaderManager().restartLoader(DEB_LOADER, null, this);
    }

    private void updateWeather() {
        Log.d("DEB", "DebListFragment ---> updateWeather is called");
        DebSyncAdapter.syncImmediately(getActivity());
    }

    /**
     * Update the empty list view with user friendly message when there is no network available and
     * the application cannot fetch the data or the network is available but no data returned from the server
     */
    private void updateNoDataMessage() {
        Log.d("DEB", "DebListFragment ---> updateNoDataMessage is called");
        if(mDebAdapter.getCount() == 0) {
            TextView mTextView = (TextView) getView().findViewById(R.id.empty_list_text_view);
            if (mTextView != null) {
                //the cursor is empty, no data? Invalid location
                int msg = R.string.empty_weather_data;
                @DebSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case DebSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        msg = R.string.empty_server_down;
                        break;
                    case DebSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        msg = R.string.empty_server_error;
                        break;
                    case DebSyncAdapter.LOCATION_STATUS_INVALID:
                        msg = R.string.empty_invalid_location;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            msg = R.string.empty_no_connection_msg;
                        }
                }
                mTextView.setText(msg);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("DEB", "DebListFragment ---> onSharedPreferenceChanged is called");
        if(key.equals(getString(R.string.pref_location_status_key))) {
            updateNoDataMessage();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
