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

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    public DebAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        Log.d("DEB","DebAdapter ---> Costructor is called");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
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

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_LAYOUT_TODAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(DebListFragment.COL_WEATHER_ID)));
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
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Read weather forecast from cursor
        String description = cursor.getString(DebListFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        // For accessibility, add a content description to the icon field
        viewHolder.iconView.setContentDescription(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(DebListFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high));

        // Read low temperature from cursor
        double low = cursor.getDouble(DebListFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low));
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

    /*
    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }  */
}
