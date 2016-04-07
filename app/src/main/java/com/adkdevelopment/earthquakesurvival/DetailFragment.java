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

package com.adkdevelopment.earthquakesurvival;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adkdevelopment.earthquakesurvival.earthquake_objects.Feature;
import com.adkdevelopment.earthquakesurvival.utils.LocationUtils;
import com.adkdevelopment.earthquakesurvival.utils.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements OnMapReadyCallback {

    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.map) MapView mMapView;
    @Bind(R.id.earthquake_place) TextView mEarthquakePlace;
    @Bind(R.id.earthquake_magnitude) TextView mEarthquakeMagnitude;
    @Bind(R.id.earthquake_date) TextView mEarthquakeDate;
    @Bind(R.id.earthquake_link) TextView mEarthquakeLink;
    @BindDrawable(R.drawable.marker) Drawable mOval;

    private GoogleMap mGoogleMap;
    private CameraPosition mCameraPosition;
    private LatLng mPosition;
    private Double mMagnitude;
    private String mLink;

    public static final String CAMERA_POSITION = "camera";

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detailed_fragment, container, false);

        ButterKnife.bind(this, rootView);

        if (getActivity().getIntent() != null) {
            Intent input = getActivity().getIntent();
            String date = input.getStringExtra(Feature.DATE);
            String place = input.getStringExtra(Feature.PLACE);
            mLink = input.getStringExtra(Feature.LINK);
            mMagnitude = input.getDoubleExtra(Feature.MAGNITUDE, 1.0);
            mPosition = input.getParcelableExtra(Feature.LATLNG);

            mEarthquakeLink.setText(getString(R.string.earthquake_link, mLink));

            // awesome method to make all links clickable!
            Linkify.addLinks(mEarthquakeLink, Linkify.ALL);

            mEarthquakeDate.setText(date);
            mEarthquakePlace.setText(place);
            mEarthquakeMagnitude.setText(getString(R.string.earthquake_magnitude, mMagnitude));

            if (mPosition == null) {
                mPosition = LocationUtils.getLocation(getContext());
            }
        }

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
        } else {
            mCameraPosition = CameraPosition.builder()
                    .target(mPosition)
                    .zoom(LocationUtils.CAMERA_DEFAULT_ZOOM)
                    .build();
        }

        mFab.setOnClickListener(v -> Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        return rootView;
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
        ButterKnife.unbind(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CameraPosition save = mGoogleMap.getCameraPosition();
        outState.putParcelable(CAMERA_POSITION, save);
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

            Double magnitude = 1.0;
            if (mMagnitude != null) {
                magnitude = mMagnitude;
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(Utilities.getEarthquakeMarker(getContext(), magnitude)));

            mGoogleMap.addMarker(markerOptions);

        }
    }
}
