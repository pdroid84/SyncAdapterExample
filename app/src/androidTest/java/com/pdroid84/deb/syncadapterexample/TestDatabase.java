package com.pdroid84.deb.syncadapterexample;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import com.pdroid84.deb.syncadapterexample.data.DebContract;
import com.pdroid84.deb.syncadapterexample.data.DebDbHelper;


import java.util.HashSet;

public class TestDatabase extends AndroidTestCase {

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(DebDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DebContract.DebWeatherFields.TABLE_NAME);

        mContext.deleteDatabase(DebDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DebDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DebContract.DebWeatherFields.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> weatherColumnSet = new HashSet<String>();
        weatherColumnSet.add(DebContract.DebWeatherFields._ID);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_USER_LOCATION);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_CITY);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_DATE);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_DEGREES);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_HUMIDITY);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_DEGREES);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_MAX_TEMP);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_MIN_TEMP);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_PRESSURE);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_SHORT_DESC);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_WEATHER_ID);
        weatherColumnSet.add(DebContract.DebWeatherFields.COLUMN_WIND_SPEED);

        int columnNameIndex = c.getColumnIndex("name");
        Log.d("DEB", "index = " + columnNameIndex);
        do {
            String columnName = c.getString(columnNameIndex);
            weatherColumnSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that database doesn't contain all of the required columns
        assertTrue("Error: The database doesn't contain all of the required weather entry columns",
                weatherColumnSet.isEmpty());
        db.close();
    }


    public void testWeatherTable() {
        DebDbHelper dbHelper = new DebDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues weatherValues = TestUtilities.createWeatherValues();

        // Insert ContentValues into database and get a row ID back
        long weatherRowId = db.insert(DebContract.DebWeatherFields.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // Query the database and receive a Cursor back
        // A cursor is the primary interface to the query results.
        Cursor weatherCursor = db.query(
                DebContract.DebWeatherFields.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from location query", weatherCursor.moveToFirst() );

        // Validate the Query results
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                weatherCursor, weatherValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from weather query",
                weatherCursor.moveToNext() );

        // Close cursor and database
        weatherCursor.close();
        dbHelper.close();
    }
}
