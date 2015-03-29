package org.tomcurran.remiges.ui;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.EditItemActivity;
import org.tomcurran.remiges.util.GeoUtil;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class PlaceEditFragment extends ItemEditFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        EditItemActivity.Callbacks {
    private static final String TAG = makeLogTag(PlaceEditFragment.class);

    private static final int ACTIVITY_PLACE_PICKER = 0;

    private Cursor mPlaceCursor;

    private EditText mPlaceName;
    private EditText mPlaceLatitude;
    private EditText mPlaceLongitude;
    private TextView mPlacePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (mState) {
            case STATE_INSERT:
                getActivity().setTitle(R.string.title_place_insert);
                break;
            case STATE_EDIT:
                getLoaderManager().initLoader(0, null, this);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_edit, container, false);

        mPlaceName = (EditText) rootView.findViewById(R.id.edit_place_name);
        mPlaceLatitude = (EditText) rootView.findViewById(R.id.edit_place_latitude);
        mPlaceLongitude = (EditText) rootView.findViewById(R.id.edit_place_longitude);
        mPlacePicker = (TextView) rootView.findViewById(R.id.edit_place_picker);

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPlacePicker();
            }
        });

        return rootView;
    }

    @Override
    protected Uri getContentUri() {
        return RemigesContract.Places.CONTENT_URI;
    }

    @Override
    protected ContentValues getDefaultValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Places.PLACE_NAME, "");
        values.put(RemigesContract.Places.PLACE_LATITUDE, 0.0);
        values.put(RemigesContract.Places.PLACE_LONGITUDE, 0.0);
        return values;
    }

    @Override
    protected ContentValues getViewValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Places.PLACE_NAME, mPlaceName.getText().toString());
        values.put(RemigesContract.Places.PLACE_LATITUDE, UIUtils.parseTextViewDouble(mPlaceLatitude));
        values.put(RemigesContract.Places.PLACE_LONGITUDE, UIUtils.parseTextViewDouble(mPlaceLongitude));
        return values;
    }

    @Override
    protected void setViewValues(ContentValues values) {
        mPlaceName.setText(values.getAsString(RemigesContract.Places.PLACE_NAME));
        UIUtils.setTextViewDouble(mPlaceLatitude, values.getAsDouble(RemigesContract.Places.PLACE_LATITUDE));
        UIUtils.setTextViewDouble(mPlaceLongitude, values.getAsDouble(RemigesContract.Places.PLACE_LONGITUDE));
    }

    @Override
    protected ContentValues passIntentValues(Bundle extras, ContentValues values) {
        ContentValues newValues = new ContentValues(values);
        if (extras.containsKey(RemigesContract.Places.PLACE_NAME))
            newValues.put(RemigesContract.Places.PLACE_NAME, extras.getString(RemigesContract.Places.PLACE_NAME));
        if (extras.containsKey(RemigesContract.Places.PLACE_LATITUDE))
            newValues.put(RemigesContract.Places.PLACE_LATITUDE, extras.getDouble(RemigesContract.Places.PLACE_LATITUDE));
        if (extras.containsKey(RemigesContract.Places.PLACE_LONGITUDE))
            newValues.put(RemigesContract.Places.PLACE_LONGITUDE, extras.getDouble(RemigesContract.Places.PLACE_LONGITUDE));
        return newValues;
    }

    private void loadPlace() {
        Cursor cursor = mPlaceCursor;
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(RemigesContract.Places.PLACE_NAME, cursor.getString(PlaceQuery.NAME));
            values.put(RemigesContract.Places.PLACE_LATITUDE, cursor.getDouble(PlaceQuery.LATITUDE));
            values.put(RemigesContract.Places.PLACE_LONGITUDE, cursor.getDouble(PlaceQuery.LONGITUDE));
            setViewValues(values);
        }
    }

    private void OpenPlacePicker() {
        try {
            mPlacePicker.setEnabled(false);
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            ContentValues values = getViewValues();
            LatLng place = new LatLng(values.getAsDouble(RemigesContract.Places.PLACE_LATITUDE), values.getAsDouble(RemigesContract.Places.PLACE_LONGITUDE));
            if (place.latitude != 0.0 && place.longitude != 0.0) {
                intentBuilder.setLatLngBounds(GeoUtil.LatLngBoundary(place, 100000));
            }
            startActivityForResult(intentBuilder.build(getActivity()), ACTIVITY_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Google Play Services is not available.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_PLACE_PICKER:
                mPlacePicker.setEnabled(true);
                if (resultCode == FragmentActivity.RESULT_OK) {
                    final Place place = PlacePicker.getPlace(data, getActivity());
                    final LatLng latLng = place.getLatLng();
                    ContentValues values = getViewValues();
                    values.put(RemigesContract.Places.PLACE_LATITUDE, latLng.latitude);
                    values.put(RemigesContract.Places.PLACE_LONGITUDE, latLng.longitude);
                    setViewValues(values);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mItemUri,
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
