package com.pdroid84.deb.syncadapterexample;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import com.pdroid84.deb.syncadapterexample.data.DebContract;
import com.pdroid84.deb.syncadapterexample.data.DebDbHelper;
import com.pdroid84.deb.syncadapterexample.data.DebProvider;

/**
 * Created on 04/11/2015.
 */
public class TestDebProvider extends AndroidTestCase {

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                DebContract.DebWeatherFields.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                DebContract.DebWeatherFields.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a fresh start, so run deleteAllRecordsFromProvider
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // DebProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                DebProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + DebContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DebContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        // Test that all the types are returned properly
        String type = mContext.getContentResolver().getType(DebContract.DebWeatherFields.CONTENT_URI);
        assertEquals("Error: without parameter should return correct content type",
                DebContract.DebWeatherFields.CONTENT_TYPE, type);

        String testLocation = "London";
        type = mContext.getContentResolver().getType(
                DebContract.DebWeatherFields.buildWeatherLocationWithCity(testLocation));
        assertEquals("Error: with location parameter should return correct content type",
                DebContract.DebWeatherFields.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        type = mContext.getContentResolver().getType(
                DebContract.DebWeatherFields.buildWeatherLocationWithDate(testLocation,testDate));
        assertEquals("Error: with location and date should return correct content type",
                DebContract.DebWeatherFields.CONTENT_ITEM_TYPE, type);
    }

    public void testBasicQuery (){
        DebDbHelper debDbHelper = new DebDbHelper(mContext);
        SQLiteDatabase sdb = debDbHelper.getWritableDatabase();
        ContentValues contentValues = TestUtilities.createWeatherValues();
        long rowCount = sdb.insert(DebContract.DebWeatherFields.TABLE_NAME, null, contentValues);
        assertTrue("Error in inserting data to the table", rowCount != -1);
        sdb.close();

        Cursor cur = mContext.getContentResolver().query(
                DebContract.DebWeatherFields.CONTENT_URI,
                null,null,null,null);

        //Test the cursor
        TestUtilities.validateCursor("testBasicQuery", cur, contentValues);
    }

    public void testInsertReadProvider() {

        ContentValues weatherValues = TestUtilities.createWeatherValues();
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco;
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(DebContract.DebWeatherFields.CONTENT_URI, true, tco);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(DebContract.DebWeatherFields.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        // Did our content observer get called? If this fails, insert weather
        // in ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                DebContract.DebWeatherFields.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
                weatherCursor, weatherValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DebContract.DebWeatherFields.CONTENT_URI,
                true, weatherObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, most-likely not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        weatherObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertWeatherValues() {
        long currentTestDate = TestUtilities.TEST_DATE;
        long millisecondsInADay = 1000*60*60*24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate += millisecondsInADay ) {
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_USER_LOCATION,TestUtilities.TEST_LOCATION);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_CITY, TestUtilities.TEST_CITY);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_DATE, currentTestDate);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_DEGREES, 1.1);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_HUMIDITY, 1.2 + 0.01 * (float) i);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_MAX_TEMP, 75 + i);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_MIN_TEMP, 65 - i);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
            weatherValues.put(DebContract.DebWeatherFields.COLUMN_WEATHER_ID, 321);
            returnContentValues[i] = weatherValues;
        }
        return returnContentValues;
    }


    public void testBulkInsert() {
        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertWeatherValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DebContract.DebWeatherFields.CONTENT_URI,
                true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(DebContract.DebWeatherFields.CONTENT_URI, bulkInsertContentValues);

        // If this fails, it means that most-likely not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                DebContract.DebWeatherFields.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                DebContract.DebWeatherFields.COLUMN_DATE + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
