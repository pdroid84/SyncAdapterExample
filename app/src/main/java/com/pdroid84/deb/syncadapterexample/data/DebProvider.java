package com.pdroid84.deb.syncadapterexample.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by pdroid84 on 27/06/15.
 */
public class DebProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DebDbHelper mDebDbHelper;

    static final int WEATHER = 1;
    static final int WEATHER_WITH_CITY = 2;
    static final int WEATHER_WITH_CITY_AND_DATE = 3;

    private static final SQLiteQueryBuilder sWeatherByCityQueryBuilder;

    static {
        Log.d("DEB",DebProvider.class.getName() + "->static part is called");
        sWeatherByCityQueryBuilder = new SQLiteQueryBuilder();

        sWeatherByCityQueryBuilder.setTables(
                DebContract.DebWeatherFields.TABLE_NAME);
    }

    //prepare the city = ?
    private static final String sCitySelection =
            DebContract.DebWeatherFields.COLUMN_CITY + " = ? ";

    //prepare the city = ? AND date >= ?
    private static final String sCityWithStartDateSelection =
            DebContract.DebWeatherFields.COLUMN_CITY + " = ? AND " +
                    DebContract.DebWeatherFields.COLUMN_DATE + " >= ? ";

    //prepare the city = ? AND date = ?
    private static final String sCityAndDateSelection =
            DebContract.DebWeatherFields.COLUMN_CITY + " = ? AND " +
                    DebContract.DebWeatherFields.COLUMN_DATE + " = ? ";

    private Cursor getWeatherByCity(Uri uri, String[] projection, String sortOrder) {
        Log.d("DEB",DebProvider.class.getName() + "->getWeatherByCity is called");
        String city = DebContract.DebWeatherFields.getCityFromUri(uri);
        long startDate = DebContract.DebWeatherFields.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sCitySelection;
            selectionArgs = new String[]{city};
        } else {
            selection = sCityWithStartDateSelection;
            selectionArgs = new String[]{city, Long.toString(startDate)};
        }

        return sWeatherByCityQueryBuilder.query(mDebDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByCityAndDate(Uri uri, String[] projection, String sortOrder) {
        Log.d("DEB",DebProvider.class.getName() + "->getWeatherCityAndDate is called");
        String city = DebContract.DebWeatherFields.getCityFromUri(uri);
        long date = DebContract.DebWeatherFields.getDateFromUri(uri);

        return sWeatherByCityQueryBuilder.query(mDebDbHelper.getReadableDatabase(),
                projection,
                sCityAndDateSelection,
                new String[]{city, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    //  This UriMatcher will match each URI to the WEATHER, WEATHER_WITH_CITY, WEATHER_WITH_CITY_AND_DATE,
    static UriMatcher buildUriMatcher() {
        Log.d("DEB",DebProvider.class.getName() + "->buildUriMatcher (static part) is called");
        // Add all the possible Uri to matcher along with corresponding code

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DebContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DebContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, DebContract.PATH_WEATHER + "/*", WEATHER_WITH_CITY);
        matcher.addURI(authority, DebContract.PATH_WEATHER + "/*/#", WEATHER_WITH_CITY_AND_DATE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        Log.d("DEB",DebProvider.class.getName() + "->onCreate is called");
        mDebDbHelper = new DebDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        Log.d("DEB",DebProvider.class.getName() + "->getType is called");
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case WEATHER_WITH_CITY_AND_DATE:
                return DebContract.DebWeatherFields.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_CITY:
                return DebContract.DebWeatherFields.CONTENT_TYPE;
            case WEATHER:
                return DebContract.DebWeatherFields.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.d("DEB",DebProvider.class.getName() + "->query is called");
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/#"
            case WEATHER_WITH_CITY_AND_DATE:
            {
                retCursor = getWeatherByCityAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_CITY: {
                retCursor = getWeatherByCity(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mDebDbHelper.getReadableDatabase().query(
                        DebContract.DebWeatherFields.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("DEB",DebProvider.class.getName() + "->insert is called");
        final SQLiteDatabase db = mDebDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(DebContract.DebWeatherFields.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DebContract.DebWeatherFields.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d("DEB",DebProvider.class.getName() + "->delete is called");
        final SQLiteDatabase db = mDebDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case WEATHER:
                rowsDeleted = db.delete(
                        DebContract.DebWeatherFields.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        Log.d("DEB",DebProvider.class.getName() + "->normalizeDate is called");
        // normalize the date value
        if (values.containsKey(DebContract.DebWeatherFields.COLUMN_DATE)) {
            long dateValue = values.getAsLong(DebContract.DebWeatherFields.COLUMN_DATE);
            values.put(DebContract.DebWeatherFields.COLUMN_DATE, DebContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d("DEB",DebProvider.class.getName() + "->update is called");
        final SQLiteDatabase db = mDebDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WEATHER:
                normalizeDate(values);
                rowsUpdated = db.update(DebContract.DebWeatherFields.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.d("DEB",DebProvider.class.getName() + "->bulkInsert is called");
        final SQLiteDatabase db = mDebDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(DebContract.DebWeatherFields.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

}