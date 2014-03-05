package edu.mit.mobile.android.maps;

/*
 * Copyright (C) 2012-2013 MIT Mobile Experience Lab
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import edu.mit.mobile.android.staticmap.R;

/**
 * <p>
 * A view that will generate a URL which can be used to display a map within it. This will not
 * display the map image out of the box; the display of the map is intentionally separated from the
 * loading of the image, so as to allow hosting fragments / activities to manage the network load
 * lifecycle themselves.
 * </p>
 *
 * <p>
 * A recommended solution for loading the map image is our own <a
 * href="https://github.com/mitmel/Android-Image-Cache">Android Image Cache</a>.
 * </p>
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 */
public class GoogleStaticMapView extends ImageView {

    private static final String TAG = GoogleStaticMapView.class.getSimpleName();
    private GoogleStaticMaps mStaticMapUtil;

    private String mMarker;
    private boolean mSensor;
    private float mLatitude;
    private float mLongitude;
    private int mZoom;
    private String mMapType;

    private boolean mHasReceivedSet = false;

    private OnMapUpdateListener mMapUpdateListener;
    private int mMapHeight;
    private int mMapWidth;

    public GoogleStaticMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GoogleStaticMapView,
                0, 0);
        try {
            mMarker = a.getString(R.styleable.GoogleStaticMapView_marker);
            if (mMarker == null || mMarker.equals("")) {
                mMarker = "size:mid|color:red";
            }
            mMapType = a.getString(R.styleable.GoogleStaticMapView_maptype);
            if (mMapType == null || mMapType.equals("")) {
                mMapType = "roadmap";
            }
            mSensor = a.getBoolean(R.styleable.GoogleStaticMapView_sensor, false);
            mZoom = a.getInteger(R.styleable.GoogleStaticMapView_zoom, 14);
            mLatitude = a.getFloat(R.styleable.GoogleStaticMapView_latitude, 0);
            mLongitude = a.getInteger(R.styleable.GoogleStaticMapView_longitude, 0);
        } finally {
            a.recycle();
        }
        init(context);
    }

    public GoogleStaticMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoogleStaticMapView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        final Map<String, String> mapArgs = new HashMap<String, String>();

        mapArgs.put(GoogleStaticMaps.PARAMETER_ZOOM, String.valueOf(mZoom));
        mapArgs.put(GoogleStaticMaps.PARAMETER_MAPTYPE, mMapType);

        mStaticMapUtil = new GoogleStaticMaps(context, mapArgs);
        setOnClickListener(mOnClickListener);
    }

    public void setMarker(String marker) {
        mMarker = marker;
    }

    /**
     * <p>
     * Sets the position of the marker on the map.
     * </p>
     *
     * <blockquote>Applications that determine the user's location via a sensor must pass
     * sensor=true within your Static Maps API request URL. If your application does not use a
     * sensor, pass sensor=false.</blockquote>
     *
     * @param latitude
     * @param longitude
     * @param sensor
     *            pass true if this coordinate represents the user's location as determined by a
     *            sensor (such as a GPS locator).
     */
    public void setMap(float latitude, float longitude, boolean sensor) {
        mSensor = sensor;
        mLatitude = latitude;
        mLongitude = longitude;
        mHasReceivedSet = true;

        updateMap();

        invalidate();
        requestLayout();
    }

    public void clearMap() {
        mHasReceivedSet = false;
        invalidate();
        requestLayout();
    }

    /**
     * Sets the zoom level.
     *
     * @param zoom
     */
    public void setZoom(int zoom) {
        if (zoom != mZoom) {
            mZoom = zoom;

            mStaticMapUtil.setExtraArg(GoogleStaticMaps.PARAMETER_ZOOM, String.valueOf(mZoom));

            invalidate();
            requestLayout();
        }
    }

    /**
     * Gets the zoom level.
     *
     * @return
     */
    public int getZoom() {
        return mZoom;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateMap();
            invalidate();
            requestLayout();
        }
    }

    public void setOnMapUpdateListener(OnMapUpdateListener l) {
        mMapUpdateListener = l;
    }

    public int getMapWidth() {
        return mMapWidth;
    }

    public int getMapHeight() {
        return mMapHeight;
    }

    private void updateMap() {
        if (!mHasReceivedSet) {
            return;
        }

        final int mapWidth = getWidth() - (getPaddingRight() + getPaddingLeft());
        final int mapHeight = getHeight() - (getPaddingTop() + getPaddingBottom());

        if (mapWidth <= 0 || mapHeight <= 0) {
            Log.e(TAG, "mapWidth or mapHeight were <=0. Not updating.");
            return;
        }

        mMapWidth = mapWidth;
        mMapHeight = mapHeight;

        final Uri staticMap = mStaticMapUtil.getMap(mLatitude, mLongitude, mapWidth, mapHeight,
                mSensor, mMarker);

        if (mMapUpdateListener != null) {
            mMapUpdateListener.onMapUpdate(this, staticMap);
        }
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + mLatitude + "," + mLongitude
                            + "?q=" + mLatitude + "," + mLongitude + "&z=" + mZoom)));
        }
    };
}
