package org.tomcurran.remiges.ui;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class PlaceEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(PlaceEditFragment.class);

    private static final int STATE_INSERT = 0;
    private static final int STATE_EDIT = 1;

    private static final String SAVE_STATE_PLACE_URI = "place_uri";
    private static final String SAVE_STATE_PLACE_STATE = "place_state";

    private int mState;
    private Uri mPlaceUri;
    private Cursor mPlaceCursor;

    private EditText mPlaceName;
    private EditText mPlaceLatitude;
    private EditText mPlaceLongitude;

    public PlaceEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            final String action = intent.getAction();
            if (action == null) {
                LOGE(TAG, "No action provided for jump type");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            } else if (action.equals(Intent.ACTION_INSERT)) {
                mState = STATE_INSERT;
                ContentValues values = getDefaultValues();
                if (intent.getExtras() != null) {
                    passInExtras(intent.getExtras(), values);
                }
                mPlaceUri = activity.getContentResolver().insert(RemigesContract.Places.CONTENT_URI, values);
                if (mPlaceUri == null) {
                    LOGE(TAG, "Failed to insert new place into " + intent.getData());
                    activity.setResult(FragmentActivity.RESULT_CANCELED);
                    activity.finish();
                    return;
                }
            } else if (action.equals(Intent.ACTION_EDIT)) {
                mState = STATE_EDIT;
                mPlaceUri = intent.getData();
            } else {
                LOGE(TAG, "Unknown action. Exiting");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            }
        } else {
            mPlaceUri = savedInstanceState.getParcelable(SAVE_STATE_PLACE_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_PLACE_STATE);
        }

        Intent intent = new Intent();
        switch (mState) {
            case STATE_INSERT: intent.setAction(Intent.ACTION_INSERT); break;
            case STATE_EDIT:   intent.setAction(Intent.ACTION_EDIT);   break;
        }
        intent.setData(mPlaceUri);
        activity.setResult(FragmentActivity.RESULT_OK, intent);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_edit, container, false);

        mPlaceName = (EditText) rootView.findViewById(R.id.edit_place_name);
        mPlaceLatitude = (EditText) rootView.findViewById(R.id.edit_place_latitude);
        mPlaceLongitude = (EditText) rootView.findViewById(R.id.edit_place_longitude);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_PLACE_URI, mPlaceUri);
        outState.putInt(SAVE_STATE_PLACE_STATE, mState);
    }

    public String barDone() {
        updatePlace();
        return RemigesContract.Places.getPlaceId(mPlaceUri);
    }

    public void barCancel() {
        if (mState == STATE_INSERT) {
            deletePlace();
        }
    }

    private ContentValues getDefaultValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Places.PLACE_NAME, "");
        values.put(RemigesContract.Places.PLACE_LATITUDE, 0.0);
        values.put(RemigesContract.Places.PLACE_LONGITUDE, 0.0);
        return values;
    }

    private void passInExtras(Bundle extras, ContentValues values) {
        if (extras.containsKey(RemigesContract.Places.PLACE_NAME))
            values.put(RemigesContract.Places.PLACE_NAME, extras.getString(RemigesContract.Places.PLACE_NAME));
        if (extras.containsKey(RemigesContract.Places.PLACE_LATITUDE))
            values.put(RemigesContract.Places.PLACE_LATITUDE, extras.getDouble(RemigesContract.Places.PLACE_LATITUDE));
        if (extras.containsKey(RemigesContract.Places.PLACE_LONGITUDE))
            values.put(RemigesContract.Places.PLACE_LONGITUDE, extras.getDouble(RemigesContract.Places.PLACE_LONGITUDE));
    }

    private void loadPlace() {
        Cursor cursor = mPlaceCursor;
        if (cursor.moveToFirst()) {
            mPlaceName.setText(cursor.getString(PlaceQuery.NAME));
            UIUtils.setTextViewDouble(mPlaceLatitude, cursor.getDouble(PlaceQuery.LATITUDE));
            UIUtils.setTextViewDouble(mPlaceLongitude, cursor.getDouble(PlaceQuery.LONGITUDE));
        }
    }

    private boolean updatePlace() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Places.PLACE_NAME, mPlaceName.getText().toString());
        values.put(RemigesContract.Places.PLACE_LATITUDE, UIUtils.parseTextViewDouble(mPlaceLatitude));
        values.put(RemigesContract.Places.PLACE_LONGITUDE, UIUtils.parseTextViewDouble(mPlaceLongitude));
        return getActivity().getContentResolver().update(mPlaceUri, values, null, null) > 0;
    }

    private boolean deletePlace() {
        return getActivity().getContentResolver().delete(mPlaceUri, null, null) > 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
