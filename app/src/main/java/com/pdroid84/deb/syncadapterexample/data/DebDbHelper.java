package com.pdroid84.deb.syncadapterexample.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pdroid84.deb.syncadapterexample.data.DebContract.DebWeatherFields;
/**
 * Created by pdroid84 on 27/06/15.
 * Define the Database schema and upgrade (drop) strategy
 */
public class DebDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    public static final String DATABASE_NAME = "weather.db";

    public DebDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DEB", DebDbHelper.class.getName() + "-> Constructor is called");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.d("DEB",DebDbHelper.class.getName() + "-> onCreate is called");
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + DebWeatherFields.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                DebWeatherFields._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DebWeatherFields.COLUMN_USER_LOCATION + " TEXT NOT NULL, " +
                DebWeatherFields.COLUMN_CITY + " TEXT NOT NULL, " +
                DebWeatherFields.COLUMN_DATE + " INTEGER NOT NULL, " +
                DebWeatherFields.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
                DebWeatherFields.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                DebWeatherFields.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                DebWeatherFields.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                DebWeatherFields.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                DebWeatherFields.COLUMN_PRESSURE + " REAL NOT NULL, " +
                DebWeatherFields.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                DebWeatherFields.COLUMN_DEGREES + " REAL NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d("DEB",DebDbHelper.class.getName() + "-> onUpgrade is called");
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DebWeatherFields.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}