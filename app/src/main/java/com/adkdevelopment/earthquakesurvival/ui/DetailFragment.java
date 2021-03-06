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
 *
 */

package com.adkdevelopment.earthquakesurvival.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adkdevelopment.earthquakesurvival.R;
import com.adkdevelopment.earthquakesurvival.data.objects.earthquake.Feature;
import com.adkdevelopment.earthquakesurvival.data.provider.earthquake.EarthquakeColumns;
import com.adkdevelopment.earthquakesurvival.ui.behaviour.ScrollableMapView;
import com.adkdevelopment.earthquakesurvival.ui.settings.SettingsActivity;
import com.adkdevelopment.earthquakesurvival.utils.LocationUtils;
import com.adkdevelopment.earthquakesurvival.utils.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = DetailFragment.class.getSimpleName();

    @BindView(R.id.map)
    ScrollableMapView mMapView;
    @BindView(R.id.earthquake_place) TextView mEarthquakePlace;
    @BindView(R.id.earthquake_magnitude) TextView mEarthquakeMagnitude;
    @BindView(R.id.earthquake_date) TextView mEarthquakeDate;
    @BindView(R.id.earthquake_depth) TextView mEarthquakeDepth;
    @BindView(R.id.earthquake_link) TextView mEarthquakeLink;
    @BindDrawable(R.drawable.marker) Drawable mOval;
    @BindView(R.id.earthquake_distance) TextView mEarthquakeDistance;
    private Unbinder mUnbinder;

    private GoogleMap mGoogleMap;
    private CameraPosition mCameraPosition;
    public static final String CAMERA_POSITION = "camera";
    public static final String MAP_STATE = "mapViewSaveState";

    // Earthquake event
    private LatLng mPosition;
    private double mMagnitude;
    private String mLink;
    private String mDescription;
    private String mDate;
    private String mDistance;
    private double mDepth;

    // ShareActionProvider - sharing info with others
    ShareActionProvider mShareActionProvider;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detailed_fragment, container, false);
        setHasOptionsMenu(true);

        mUnbinder = ButterKnife.bind(this, rootView);

        if (getActivity().getIntent().hasExtra(Feature.GEOFENCE)) {

            ArrayList<String> place = getActivity().getIntent().getStringArrayListExtra(Feature.GEOFENCE);

            Cursor cursor = null;
            if (place != null && place.size() > 1) {
                cursor = getActivity().getContentResolver()
                        .query(EarthquakeColumns.CONTENT_URI,
                                null,
                                EarthquakeColumns.URL + "= ?",
                                new String[]{place.get(1)},
                                null);
            }

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                long dateMillis = cursor.getLong(cursor.getColumnIndex(EarthquakeColumns.TIME));
                double latitude = cursor.getDouble(cursor.getColumnIndex(EarthquakeColumns.LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(EarthquakeColumns.LONGITUDE));

                LatLng latLng = new LatLng(latitude, longitude);

                mCameraPosition = CameraPosition.builder()
                        .target(latLng)
                        .zoom(LocationUtils.CAMERA_DEFAULT_ZOOM)
                        .build();

                mDistance = getString(R.string.earthquake_distance,
                        LocationUtils.getDistance(latLng, LocationUtils.getLocation(getContext())));
                mDate = Utilities.getRelativeDate(dateMillis);
                mDescription = Utilities.formatEarthquakePlace(cursor
                        .getString(cursor.getColumnIndex(EarthquakeColumns.PLACE)));
                mLink = cursor.getString(cursor.getColumnIndex(EarthquakeColumns.URL));
                mMagnitude = cursor.getDouble(cursor.getColumnIndex(EarthquakeColumns.MAG));
                mDepth = cursor.getDouble(cursor.getColumnIndex(EarthquakeColumns.DEPTH)) / 1.6;
                cursor.close();

                setDataToViews();
            }

        } else if (getActivity().getIntent() != null){
            Intent input = getActivity().getIntent();
            mDate = input.getStringExtra(Feature.DATE);
            mDescription = input.getStringExtra(Feature.PLACE);
            mLink = input.getStringExtra(Feature.LINK);
            mMagnitude = input.getDoubleExtra(Feature.MAGNITUDE, 0.0);
            mPosition = input.getParcelableExtra(Feature.LATLNG);
            mDistance = input.getStringExtra(Feature.DISTANCE);
            mDepth = input.getDoubleExtra(Feature.DEPTH, 0.0);

            setDataToViews();
        }

        if (mPosition == null) {
            mPosition = LocationUtils.getLocation(getContext());
        }

        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle(MAP_STATE) : null;

        if (Utilities.checkPlayServices(getActivity())) {
            mMapView.onCreate(mapViewSavedInstanceState);
            mMapView.getMapAsync(this);
        }

        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
        } else {
            mCameraPosition = CameraPosition.builder()
                    .target(mPosition)
                    .zoom(LocationUtils.CAMERA_DEFAULT_ZOOM)
                    .build();
        }

        return rootView;
    }

    /**
     * Sets data to views
     */
    public void setDataToViews() {
        mEarthquakeLink.setText(Utilities.getHtmlText(getString(R.string.earthquake_link, mLink)));
        mEarthquakeLink.setMovementMethod(LinkMovementMethod.getInstance());

        mEarthquakeDistance.setText(mDistance);
        mEarthquakeDate.setText(mDate);
        mEarthquakePlace.setText(mDescription);
        mEarthquakeMagnitude.setText(getString(R.string.earthquake_magnitude, mMagnitude));
        mEarthquakeDepth.setText(getString(R.string.earthquake_depth, mDepth));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mCameraPosition != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }
        addMarkers();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final Bundle mapViewSaveState = new Bundle(outState);

        if (mMapView != null) {
            mMapView.onSaveInstanceState(mapViewSaveState);
            outState.putBundle(MAP_STATE, mapViewSaveState);
        }

        if (mGoogleMap != null) {
            CameraPosition save = mGoogleMap.getCameraPosition();
            outState.putParcelable(CAMERA_POSITION, save);
        }
    }

    /**
     * Adds markers to the map from the database
     */
    private void addMarkers() {
        if (mGoogleMap != null) {
            mGoogleMap.clear();

            LatLng latLng = LocationUtils.getLocation(getContext());
            if (mPosition != null) {
                latLng = mPosition;
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(Utilities.getEarthquakeMarker(getContext(), mMagnitude)));

            mGoogleMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detailed, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        setShareIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets a share intent on ShareActionProvider
     */
    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.earthquake_magnitude, mMagnitude) + "\n"
                            + mDate + "\n"
                            + mDescription + "\n"
                            + mDistance + "\n"
                            + getString(R.string.earthquake_depth, mDepth) + "\n"
                            + mLink);
            mShareActionProvider.setShareIntent(intent);
        } else {
            Log.e(TAG, "ShareActionProvider is null");
        }
    }
}
