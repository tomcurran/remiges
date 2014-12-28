package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class PlaceDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMapReadyCallback {
    private static final String TAG = makeLogTag(PlaceDetailFragment.class);

    private static final String SAVE_STATE_PLACE_URI = "place_uri";

    private static final int DIALOG_FRAGMENT = 0;

    private static final int LOADER_PLACE_DETAIL = 0;

    private Uri mPlaceUri;
    private Cursor mPlaceCursor;
    private GoogleMap mMap;
    private LatLng mLocation;
    private FrameLayout mPlaceMap;

    private static GoogleMapOptions sDefaultMapOptions = new GoogleMapOptions()
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .liteMode(false)
            .compassEnabled(false)
            .rotateGesturesEnabled(false)
            .scrollGesturesEnabled(false)
            .tiltGesturesEnabled(false)
            .zoomGesturesEnabled(false)
            .zoomControlsEnabled(false);
    private static final float sDefaultMapZoom = 7;

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

        getLoaderManager().initLoader(LOADER_PLACE_DETAIL, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_detail, container, false);

        mPlaceMap = (FrameLayout) rootView.findViewById(R.id.detail_place_map);

        if (savedInstanceState == null) {
            StatisticsFragment statisticsFragment = StatisticsFragment.newInstance(
                    RemigesContract.Places.getPlaceId(mPlaceUri), RemigesContract.Jumps.PLACE_ID);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.detail_place_statistics, statisticsFragment).commit();
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        addMarker();
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
                DialogFragment dialog = DeleteItemDialogFragment.newInstance(R.string.dialog_place_delete_message);
                dialog.setTargetFragment(this, DIALOG_FRAGMENT);
                dialog.show(getFragmentManager().beginTransaction(), "dialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    deletePlace();
                }
                break;
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
            double latitude = placeCursor.getDouble(PlaceQuery.LATITUDE);
            double longitude = placeCursor.getDouble(PlaceQuery.LONGITUDE);
            if (latitude != 0 && longitude != 0) {
                mLocation = new LatLng(latitude, longitude);
                SupportMapFragment mapFragment = SupportMapFragment.newInstance(
                        sDefaultMapOptions.camera(CameraPosition.fromLatLngZoom(mLocation, sDefaultMapZoom)));
                mapFragment.getMapAsync(this);
                getChildFragmentManager().beginTransaction().replace(R.id.detail_place_map, mapFragment).commit();
                addMarker();
                mPlaceMap.setVisibility(View.VISIBLE);
            } else {
                mLocation = null;
                mPlaceMap.setVisibility(View.GONE);
            }
        }
    }

    private void addMarker() {
        if (mLocation != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocation));
            mMap.addMarker(new MarkerOptions()
                            .position(mLocation)
            );
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_PLACE_DETAIL:
                return new CursorLoader(
                        getActivity(),
                        mPlaceUri,
                        PlaceQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.Places.DEFAULT_SORT
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_PLACE_DETAIL:
                mPlaceCursor = cursor;
                loadPlace();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_PLACE_DETAIL:
                mPlaceCursor = null;
                break;
        }
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
