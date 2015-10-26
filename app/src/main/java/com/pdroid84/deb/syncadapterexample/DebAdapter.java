package com.pdroid84.deb.syncadapterexample;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by debashis on 26/07/15.
 */
public class DebAdapter extends CursorAdapter {

    private static final int VIEW_COUNT_TOTAL = 2;
    private static final int VIEW_LAYOUT_TODAY = 0;
    private static final int VIEW_LAYOUT_FUTURE = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView locationView;

        public ViewHolder(View view) {
            Log.d("DEB","DebAdapter ---> ViewHolder is called");
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            locationView = (TextView) view.findViewById(R.id.list_item_location);
        }
    }

    public DebAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        Log.d("DEB","DebAdapter ---> Costructor is called");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        Log.d("DEB","DebAdapter ---> newView is called");
        //get the view type to be used (today or future)
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType)
        {
            case VIEW_LAYOUT_TODAY:
            {
                layoutId = R.layout.fragment_deb_list_today;
                break;
            }
            case  VIEW_LAYOUT_FUTURE:
            {
                layoutId = R.layout.fragment_deb_list;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return  view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d("DEB","DebAdapter ---> bindView is called");

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_LAYOUT_TODAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(DebListFragment.COL_WEATHER_ID)));
                //Set the Location to textView
                String locationCity = cursor.getString(DebListFragment.COL_CITY);
                viewHolder.locationView.setText(locationCity);

                break;
            }
            case VIEW_LAYOUT_FUTURE: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(DebListFragment.COL_WEATHER_ID)));
                break;
            }
        }

        // Read date from cursor
        long dateInMillis = cursor.getLong(DebListFragment.COL_WEATHER_DATE);
        Log.d("DEB", "DebAdapter ---> date value as in database: " + Long.toString(dateInMillis));

        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Read weather forecast from cursor
        String description = cursor.getString(DebListFragment.COL_WEATHER_DESC);


        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);
        //For accessibility add contentDescription
        viewHolder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast,description));

        // For accessibility, add a content description to the icon field
        //For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable. So commenting it out
        //viewHolder.iconView.setContentDescription(description);

        // Read user preference for metric or imperial temperature units
        //Found that we are not using this, so commenting it out
        //boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(DebListFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high));
        //For accessibility add contentDescription
        viewHolder.highTempView.setContentDescription(context.getString(R.string.a11y_high_temp,
                Utility.formatTemperature(context,high)));

        // Read low temperature from cursor
        double low = cursor.getDouble(DebListFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low));
        //For accessibility add contentDescription
        viewHolder.lowTempView.setContentDescription(context.getString(R.string.a11y_low_temp,
                Utility.formatTemperature(context,low)));
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("DEB","DebAdapter ---> getItemViewType is called");
        int viewType = -1;
        if (position == 0 && mUseTodayLayout)
        {
            viewType = VIEW_LAYOUT_TODAY;
        }
        else
        {
            viewType = VIEW_LAYOUT_FUTURE;
        }
        Log.d("DEB","DebAdapter ---> getItemViewType---> viewType= "+viewType);
        return viewType;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_COUNT_TOTAL;
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }
}
