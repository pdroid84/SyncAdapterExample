package com.pdroid84.deb.syncadapterexample;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;
import com.pdroid84.deb.syncadapterexample.data.DebContract;
/**

 */
public class DebListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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

        //Attach the adapter to the ListView
        mListView.setAdapter(mDebAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Cursor mCursor = (Cursor) adapterView.getItemAtPosition(pos);
                // code to be impleted later for detail fragment
                Toast.makeText(getActivity(),"Item selected is "+pos,Toast.LENGTH_LONG).show();
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("DEB", "DebListFragment ---> onCreateLoader is called");
        // Sort order:  Ascending, by date.
        String sortOrder = DebContract.DebWeatherFields.COLUMN_DATE + " ASC";

        String locationSetting = "Newcastle";
        Uri weatherForLocationUri = DebContract.DebWeatherFields.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("DEB", "DebListFragment ---> onLoaderReset is called");
        mDebAdapter.swapCursor(null);
    }
}
