package com.pdroid84.deb.syncadapterexample;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import com.pdroid84.deb.syncadapterexample.data.DebContract;
import java.util.Map;
import java.util.Set;

/*
    This module is copied from learning project Sunshine and customised accordingly
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_LOCATION = "london";
    static final String TEST_CITY = "London";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }


    static ContentValues createWeatherValues() {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_USER_LOCATION,TEST_LOCATION);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_CITY,TEST_CITY);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_DATE,TEST_DATE);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_DEGREES,1.1);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_HUMIDITY,2.2);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_PRESSURE, 3.3);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_MAX_TEMP, 85);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_MIN_TEMP, 45);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_SHORT_DESC, "Sunny");
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_WEATHER_ID, 321);
        weatherValues.put(DebContract.DebWeatherFields.COLUMN_WIND_SPEED, 6.6);

        return weatherValues;
    }



    /*
        The functions provided inside of TestDebProvider uses this utility class to test
        the ContentObserver callbacks using the PollingCheck class which is grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
