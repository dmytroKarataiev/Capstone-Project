/*
 * MIT License
 *
 * Copyright (c) 2016. Dmytro Karataiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.adkdevelopment.earthquakesurvival.adapters;

/**
 * Simple RecyclerView adapter with OnClickListener on each element
 * Created by karataev on 3/15/16.
 */

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adkdevelopment.earthquakesurvival.DetailActivity;
import com.adkdevelopment.earthquakesurvival.R;
import com.adkdevelopment.earthquakesurvival.objects.earthquake.Feature;
import com.adkdevelopment.earthquakesurvival.provider.count.CountColumns;
import com.adkdevelopment.earthquakesurvival.provider.earthquake.EarthquakeColumns;
import com.adkdevelopment.earthquakesurvival.provider.news.NewsColumns;
import com.adkdevelopment.earthquakesurvival.ui.CursorRecyclerViewAdapter;
import com.adkdevelopment.earthquakesurvival.utils.LocationUtils;
import com.adkdevelopment.earthquakesurvival.utils.Utilities;
import com.google.android.gms.maps.model.LatLng;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Creates RecyclerView adapter which populates task items in MainFragment
 * Each element has an onClickListener
 */
public class NewsAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

    private final Activity mContext;

    // Provide a reference to the views for each data item
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @Bind(R.id.news_item_title) TextView mTitle;
        @Bind(R.id.news_item_date) TextView mDate;
        @Bind(R.id.news_item_description) TextView mDescription;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public class ViewHolderStats extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @Bind(R.id.stat_item_year) TextView mStatYear;
        @Bind(R.id.stat_item_month) TextView mStatMonth;
        @Bind(R.id.stat_item_week) TextView mStatWeek;
        @Bind(R.id.stat_item_day) TextView mStatDay;

        public ViewHolderStats(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public class ViewHolderLargest extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @Bind(R.id.stat_item_largest) TextView mStatLargest;
        @Bind(R.id.stat_item_magnitude) TextView mStatMagnitude;
        @Bind(R.id.stat_item_description) TextView mStatDescription;
        @Bind(R.id.stat_item_date) TextView mStatDate;
        @Bind(R.id.stat_item_distance) TextView mStatDistance;
        @Bind(R.id.earthquake_item_click) CardView mEarthquakeClick;

        public ViewHolderLargest(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NewsAdapter(Activity context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.news_statistics, parent, false);

                return new ViewHolderStats(v);
            case 1:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.news_largest, parent, false);

                return new ViewHolderLargest(v);
            case 2:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.news_largest, parent, false);

                return new ViewHolderLargest(v);
            default:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.news_item, parent, false);

                return new ViewHolder(v);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {

        switch (holder.getItemViewType()) {
            case 0:
                Cursor tempCursor = mContext.getContentResolver()
                        .query(CountColumns.CONTENT_URI,
                                null,
                                null,
                                null,
                                CountColumns._ID + " LIMIT 1");

                if (tempCursor != null) {

                    tempCursor.moveToFirst();

                    ((ViewHolderStats) holder).mStatYear.setText(mContext.getString(R.string.earthquake_statistics_year,
                            tempCursor.getInt(tempCursor.getColumnIndex(CountColumns.COUNT_YEAR))));
                    ((ViewHolderStats) holder).mStatMonth.setText(mContext.getString(R.string.earthquake_statistics_month,
                            tempCursor.getInt(tempCursor.getColumnIndex(CountColumns.COUNT_MONTH))));
                    ((ViewHolderStats) holder).mStatWeek.setText(mContext.getString(R.string.earthquake_statistics_week,
                            tempCursor.getInt(tempCursor.getColumnIndex(CountColumns.COUNT_WEEK))));
                    ((ViewHolderStats) holder).mStatDay.setText(mContext.getString(R.string.earthquake_statistics_day,
                            tempCursor.getInt(tempCursor.getColumnIndex(CountColumns.COUNT_DAY))));

                    tempCursor.close();
                }
                break;
            case 1:
                tempCursor = mContext.getContentResolver().query(EarthquakeColumns.CONTENT_URI,
                            null,
                            null,
                            null,
                            EarthquakeColumns.MAG + " DESC LIMIT 1");

                    if (tempCursor != null) {
                        populateView(holder, tempCursor, 0);
                    }
                break;
            case 2:
                tempCursor = mContext.getContentResolver().query(EarthquakeColumns.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

                if (tempCursor != null) {
                    int position = 0;
                    double distanceClosest = 99999.9;

                    while (tempCursor.moveToNext()) {
                        double latitude = tempCursor.getDouble(tempCursor.getColumnIndex(EarthquakeColumns.LATITUDE));
                        double longitude = tempCursor.getDouble(tempCursor.getColumnIndex(EarthquakeColumns.LONGITUDE));
                        LatLng latLng = new LatLng(latitude, longitude);

                        double currentDistance = LocationUtils.getDistance(latLng, LocationUtils.getLocation(mContext));

                        if (currentDistance < distanceClosest) {
                            distanceClosest = currentDistance;
                            position = tempCursor.getPosition();
                        }
                    }

                    ((ViewHolderLargest) holder).mStatLargest.setText(mContext.getString(R.string.earthquake_statistics_closest));

                    populateView(holder, tempCursor, position);

                }
                break;
            default:
                Long dateMillis = cursor.getLong(cursor.getColumnIndex(NewsColumns.DATE));
                ((ViewHolder) holder).mDate.setText(Utilities.getNiceDate(dateMillis));

                String title = cursor.getString(cursor.getColumnIndex(NewsColumns.TITLE));
                String link = cursor.getString(cursor.getColumnIndex(NewsColumns.URL));

                link = mContext.getString(R.string.earthquake_link, link);
                ((ViewHolder) holder).mDescription.setText(Html.fromHtml(link));
                ((ViewHolder) holder).mDescription.setMovementMethod(LinkMovementMethod.getInstance());

                ((ViewHolder) holder).mTitle.setText(title);

                ((ViewHolder) holder).mTitle.setOnClickListener(click -> Toast.makeText(mContext, title, Toast.LENGTH_SHORT).show());
                break;
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        if (getCursor() != null) {
            return getCursor().getCount();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void populateView(RecyclerView.ViewHolder holder, Cursor tempCursor, int position) {

        tempCursor.moveToPosition(position);

        double latitude = tempCursor.getDouble(tempCursor.getColumnIndex(EarthquakeColumns.LATITUDE));
        double longitude = tempCursor.getDouble(tempCursor.getColumnIndex(EarthquakeColumns.LONGITUDE));
        LatLng latLng = new LatLng(latitude, longitude);

        String desc = tempCursor.getString(tempCursor.getColumnIndex(EarthquakeColumns.PLACE));
        ((ViewHolderLargest) holder).mStatDescription.setText(desc);

        double magnitude = tempCursor.getDouble(tempCursor.getColumnIndex(EarthquakeColumns.MAG));
        ((ViewHolderLargest) holder).mStatMagnitude.setText(mContext.getString(R.string.earthquake_magnitude, magnitude));

        long dateMillis = tempCursor.getLong(tempCursor.getColumnIndex(EarthquakeColumns.TIME));
        ((ViewHolderLargest) holder).mStatDate.setText(Utilities.getNiceDate(dateMillis));

        String link = tempCursor.getString(tempCursor.getColumnIndex(EarthquakeColumns.URL));

        String distance = mContext.getString(R.string.earthquake_distance,
                LocationUtils.getDistance(latLng, LocationUtils.getLocation(mContext)));
        ((ViewHolderLargest) holder).mStatDistance.setText(distance);

        tempCursor.close();

        // TODO: 4/22/16 move to interface 
        ((ViewHolderLargest) holder).mEarthquakeClick.setOnClickListener(click -> {
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(Feature.MAGNITUDE, magnitude);
            intent.putExtra(Feature.PLACE, desc);
            intent.putExtra(Feature.DATE, Utilities.getNiceDate(dateMillis));
            intent.putExtra(Feature.LINK, link);
            intent.putExtra(Feature.LATLNG, latLng);
            intent.putExtra(Feature.DISTANCE, distance);

            // Check if a phone supports shared transitions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //noinspection unchecked always true
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(
                        mContext,
                        Pair.create(((ViewHolderLargest) holder).itemView.findViewById(R.id.earthquake_item_click),
                                ((ViewHolderLargest) holder).itemView.findViewById(R.id.earthquake_item_click).getTransitionName()))
                        .toBundle();
                mContext.startActivity(intent, bundle);
            } else {
                mContext.startActivity(intent);
            }

        });
    }
}