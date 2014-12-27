package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMapReadyCallback {
    private static final String TAG = makeLogTag(JumpDetailFragment.class);

    private static final String SAVE_STATE_JUMP_URI = "jump_uri";

    private static final int DIALOG_FRAGMENT = 0;

    private Uri mJumpUri;
    private Cursor mJumpCursor;

    private TextView mJumpTitle;
    private TextView mJumpDate;
    private LinearLayout mJumpDescriptionLayout;
    private TextView mJumpDescription;
    private TextView mJumpExitAltitude;
    private TextView mJumpDeploymentAltitude;
    private TextView mJumpDelay;
    private LinearLayout mPlaceContainer;
    private GoogleMap mMap;
    private LatLng mLocation;
    private FrameLayout mPlaceMap;
    private TextView mPlaceName;
    private Typeface mRoboto;

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
        public void onEditJump(Uri uri);
        public void onDeleteJump(Uri uri);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onEditJump(Uri uri) {
        }
        @Override
        public void onDeleteJump(Uri uri) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            mJumpUri = intent.getData();
        } else {
            mJumpUri = savedInstanceState.getParcelable(SAVE_STATE_JUMP_URI);
        }

        mRoboto = UIUtils.loadFont(getActivity(), UIUtils.FONT_ROBOTO_THIN);

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jump_detail, container, false);

        mJumpTitle = (TextView) rootView.findViewById(R.id.detail_jump_title);
        mJumpDate = (TextView) rootView.findViewById(R.id.detail_jump_date);
        mJumpDescription = (TextView) rootView.findViewById(R.id.detail_jump_description);
        mJumpDescriptionLayout = (LinearLayout) rootView.findViewById(R.id.detail_jump_layout_description);
        mPlaceContainer = (LinearLayout) rootView.findViewById(R.id.detail_jump_place_container);
        mPlaceName = (TextView) rootView.findViewById(R.id.detail_jump_place_name);
        mPlaceMap = (FrameLayout) rootView.findViewById(R.id.detail_jump_map);
        mJumpExitAltitude = (TextView) rootView.findViewById(R.id.detail_jump_exit_altitude);
        mJumpDeploymentAltitude = (TextView) rootView.findViewById(R.id.detail_jump_deployment_altitude);
        mJumpDelay = (TextView) rootView.findViewById(R.id.detail_jump_delay);

        mJumpTitle.setTypeface(mRoboto);

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
        inflater.inflate(R.menu.jump_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_jump_detail_edit:
                editJump();
                return true;
            case R.id.menu_jump_detail_delete:
                DialogFragment dialog = DeleteItemDialogFragment.newInstance(R.string.dialog_jump_delete_message);
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
                    deleteJump();
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_JUMP_URI, mJumpUri);
    }

    private void editJump() {
        mCallbacks.onEditJump(mJumpUri);
    }

    private void deleteJump() {
        int rowsDeleted = getActivity().getContentResolver().delete(mJumpUri, null, null);
        if (rowsDeleted > 0) {
            mCallbacks.onDeleteJump(mJumpUri);
        }
    }

    private void loadJump() {
        Cursor jumpCursor = mJumpCursor;
        if (jumpCursor.moveToFirst()) {
            mJumpDate.setText(DateFormat.format(getString(R.string.format_detail_jump_date), jumpCursor.getLong(JumpQuery.DATE)));
            String description = jumpCursor.getString(JumpQuery.DESCRIPTION);
            mJumpDescription.setText(description);
            mJumpDescriptionLayout.setVisibility(description.isEmpty() ? View.GONE : View.VISIBLE);
            String title;
            int way =  jumpCursor.getInt(JumpQuery.WAY);
            if (way > 1) {
                title = getString(R.string.detail_jump_way, way);
            } else {
                title = getString(R.string.detail_jump_solo);
            }
            String jumptype = jumpCursor.getString(JumpQuery.TYPE);
            if (jumptype != null) {
                title += " " + jumptype;
            }
            mJumpTitle.setText(title);
            String place = jumpCursor.getString(JumpQuery.PLACE);
            if (place != null) {
                mPlaceContainer.setVisibility(View.VISIBLE);
                mPlaceName.setText(place);
                double latitude = jumpCursor.getFloat(JumpQuery.LATITUDE);
                double longitude = jumpCursor.getFloat(JumpQuery.LONGITUDE);
                if (latitude != 0 && longitude != 0) {
                    mLocation = new LatLng(latitude, longitude);
                    SupportMapFragment mapFragment = SupportMapFragment.newInstance(
                            sDefaultMapOptions.camera(CameraPosition.fromLatLngZoom(mLocation, sDefaultMapZoom)));
                    mapFragment.getMapAsync(this);
                    getChildFragmentManager().beginTransaction().replace(R.id.detail_jump_map, mapFragment).commit();
                    addMarker();
                    mPlaceMap.setVisibility(View.VISIBLE);
                } else {
                    mLocation = null;
                    mPlaceMap.setVisibility(View.GONE);
                }
            } else {
                mPlaceContainer.setVisibility(View.GONE);
            }
            mJumpExitAltitude.setText(jumpCursor.getString(JumpQuery.EXIT_ALTITUDE));
            mJumpDeploymentAltitude.setText(jumpCursor.getString(JumpQuery.DEPLOYMENT_ALTITUDE));
            mJumpDelay.setText(jumpCursor.getString(JumpQuery.DELAY));
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                mJumpUri,
                JumpQuery.PROJECTION,
                null,
                null,
                RemigesContract.Jumps.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mJumpCursor = cursor;
        loadJump();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mJumpCursor = null;
    }

    private interface JumpQuery {

        String[] PROJECTION = {
                RemigesContract.Jumps.JUMP_NUMBER,
                RemigesContract.Jumps.JUMP_DATE,
                RemigesContract.Jumps.JUMP_DESCRIPTION,
                RemigesContract.Jumps.JUMP_WAY,
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.Places.PLACE_NAME,
                RemigesContract.Places.PLACE_LONGITUDE,
                RemigesContract.Places.PLACE_LATITUDE,
                RemigesContract.Jumps.JUMP_EXIT_ALTITUDE,
                RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE,
                RemigesContract.Jumps.JUMP_DELAY,
                RemigesContract.Jumps._ID
        };

        int NUMBER = 0;
        int DATE = 1;
        int DESCRIPTION = 2;
        int WAY = 3;
        int TYPE = 4;
        int PLACE = 5;
        int LONGITUDE = 6;
        int LATITUDE = 7;
        int EXIT_ALTITUDE = 8;
        int DEPLOYMENT_ALTITUDE = 9;
        int DELAY = 10;

    }

}
