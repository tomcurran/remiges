package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;

import java.io.IOException;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCacheException;
import edu.mit.mobile.android.maps.GoogleStaticMapView;
import edu.mit.mobile.android.maps.OnMapUpdateListener;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class PlaceDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(PlaceDetailFragment.class);

    private static final String SAVE_STATE_PLACE_URI = "place_uri";

    private Uri mPlaceUri;
    private Cursor mPlaceCursor;

    private TextView mPlaceName;
    private TextView mPlaceLatitude;
    private TextView mPlaceLongitude;

    private ImageCache mCache;
    private GoogleStaticMapView mPlaceStaticMap;

    private OnMapUpdateListener mOnMapUpdateListener = new OnMapUpdateListener() {
        @Override
        public void onMapUpdate(GoogleStaticMapView view, Uri mapUrl) {
            StaticMap staticMap = new StaticMap();
            staticMap.uri = mapUrl;
            staticMap.width = view.getWidth();
            staticMap.height = view.getHeight();
            new FetchStaticMapTask().execute(staticMap);
        }
    };

    static class StaticMap {
        Uri uri;
        int width;
        int height;
    }

    private class FetchStaticMapTask extends AsyncTask<StaticMap, Integer, Drawable> {

        protected Drawable doInBackground(StaticMap... maps) {
            Drawable drawable = null;
            try {
                drawable = mCache.getImage(maps[0].uri, maps[0].width, maps[0].height);
            } catch (IOException e) {
                LOGE(TAG, String.format("I/O error: %s", e.getMessage()));
            } catch (ImageCacheException e) {
                LOGE(TAG, String.format("Image cache error: %s", e.getMessage()));
            }
            return drawable;
        }

        protected void onPostExecute(Drawable result) {
            if (result != null) {
                mPlaceStaticMap.setImageDrawable(result);
            }
        }

    }

    public interface Callbacks {
        public void onEditPlace(Uri uri);
        public void onDeletePlace(Uri uri);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onEditPlace(Uri uri) {
        }
        @Override
        public void onDeletePlace(Uri uri) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public PlaceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            mPlaceUri = intent.getData();
        } else {
            mPlaceUri = savedInstanceState.getParcelable(SAVE_STATE_PLACE_URI);
        }

        mCache = ImageCache.getInstance(getActivity());
        mCache.setCacheMaxSize(1024 * 1024);

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_detail, container, false);

        mPlaceName = (TextView) rootView.findViewById(R.id.detail_place_name);
        mPlaceLatitude = (TextView) rootView.findViewById(R.id.detail_place_latitude);
        mPlaceLongitude = (TextView) rootView.findViewById(R.id.detail_place_longitude);
        mPlaceStaticMap = (GoogleStaticMapView) rootView.findViewById(R.id.detail_place_staticmap);

        mPlaceStaticMap.setZoom(7);
        mPlaceStaticMap.setOnMapUpdateListener(mOnMapUpdateListener);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
        if (mCallbacks == null) {
            throw new IllegalStateException("Parent must implement fragment's callbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.place_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_place_detail_edit:
                editPlace();
                return true;
            case R.id.menu_place_detail_delete:
                deletePlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_PLACE_URI, mPlaceUri);
    }

    private void editPlace() {
        mCallbacks.onEditPlace(mPlaceUri);
    }

    private void deletePlace() {
        int rowsDeleted = getActivity().getContentResolver().delete(mPlaceUri, null, null);
        if (rowsDeleted > 0) {
            mCallbacks.onDeletePlace(mPlaceUri);
        }
    }

    private void loadPlace() {
        Cursor placeCursor = mPlaceCursor;
        if (placeCursor.moveToFirst()) {
            mPlaceName.setText(placeCursor.getString(PlaceQuery.NAME));
            mPlaceLatitude.setText(placeCursor.getString(PlaceQuery.LATITUDE));
            mPlaceLongitude.setText(placeCursor.getString(PlaceQuery.LONGITUDE));
            mPlaceStaticMap.setMap(
                    placeCursor.getFloat(PlaceQuery.LATITUDE),
                    placeCursor.getFloat(PlaceQuery.LONGITUDE),
                    true
            );
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                mPlaceUri,
                PlaceQuery.PROJECTION,
                null,
                null,
                RemigesContract.Places.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mPlaceCursor = cursor;
        loadPlace();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mPlaceCursor = null;
    }

    private interface PlaceQuery {

        String[] PROJECTION = {
                RemigesContract.Places.PLACE_NAME,
                RemigesContract.Places.PLACE_LATITUDE,
                RemigesContract.Places.PLACE_LONGITUDE,
                RemigesContract.Places._ID
        };

        int NAME = 0;
        int LATITUDE = 1;
        int LONGITUDE = 2;
        int _ID = 3;

    }

}