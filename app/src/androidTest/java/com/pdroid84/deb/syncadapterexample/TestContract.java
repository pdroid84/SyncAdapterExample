package com.pdroid84.deb.syncadapterexample;

import android.net.Uri;
import android.test.AndroidTestCase;
import com.pdroid84.deb.syncadapterexample.data.DebContract;



/**
 * Created on 04/11/2015.
 */
public class TestContract extends AndroidTestCase {
    private static final String loc = "London";

    public void testBuildWeatherLocationWithCity() {
        Uri uri = DebContract.DebWeatherFields.buildWeatherLocationWithCity("London");
        assertEquals("Error:The uri should match",
                "content://com.pdroid84.deb.syncadapterexample.provider/weather/London",
                uri.toString());
    }

    public void testGetCityFromUri () {
        Uri uri = DebContract.DebWeatherFields.buildWeatherLocationWithCity("Newcastle");
        assertEquals("Error:The location should match",
                "Newcastle",
                DebContract.DebWeatherFields.getCityFromUri(uri));
    }

}
