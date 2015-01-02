package org.tomcurran.remiges.ui;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.tomcurran.remiges.ui.singlepane.EditItemActivity;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class PlaceEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        EditItemActivity.Callbacks {
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
            if (Intent.ACTION_INSERT.equals(action)) {
                mState = STATE_INSERT;
                mPlaceUri = null;
            } else if (Intent.ACTION_EDIT.equals(action)) {
                mState = STATE_EDIT;
                mPlaceUri = intent.getData();
                getLoaderManager().initLoader(0, null, this);
            } else {
                LOGE(TAG, "Unknown action");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            }
        } else {
            mPlaceUri = savedInstanceState.getParcelable(SAVE_STATE_PLACE_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_PLACE_STATE);
        }

        if (mState == STATE_INSERT) {
            activity.setTitle(R.string.title_place_insert);
        }
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            ContentValues values = getDefaultValues();
            if (mState == STATE_INSERT) {
                Bundle extras = BaseActivity.fragmentArgumentsToIntent(getArguments()).getExtras();
                if (extras != null) {
                    values = passIntentValues(extras, values);
                }
            }
            setViewValues(values);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_PLACE_URI, mPlaceUri);
        outState.putInt(SAVE_STATE_PLACE_STATE, mState);
    }

    @Override
    public void onSaveItem() {
        switch (mState) {
            case STATE_INSERT:
                insertPlace();
                break;
            case STATE_EDIT:
                updatePlace();
                break;
        }
    }

    private ContentValues getDefaultValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Places.PLACE_NAME, "");
        values.put(RemigesContract.Places.PLACE_LATITUDE, 0.0);
        values.put(RemigesContract.Places.PLACE_LONGITUDE, 0.0);
        return values;
    }

    private ContentValues getViewValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Places.PLACE_NAME, mPlaceName.getText().toString());
        values.put(RemigesContract.Places.PLACE_LATITUDE, UIUtils.parseTextViewDouble(mPlaceLatitude));
        values.put(RemigesContract.Places.PLACE_LONGITUDE, UIUtils.parseTextViewDouble(mPlaceLongitude));
        return values;
    }

    private void setViewValues(ContentValues values) {
        mPlaceName.setText(values.getAsString(RemigesContract.Places.PLACE_NAME));
        UIUtils.setTextViewDouble(mPlaceLatitude, values.getAsDouble(RemigesContract.Places.PLACE_LATITUDE));
        UIUtils.setTextViewDouble(mPlaceLongitude, values.getAsDouble(RemigesContract.Places.PLACE_LONGITUDE));
    }

    private ContentValues passIntentValues(Bundle extras, ContentValues values) {
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

    private void insertPlace() {
        FragmentActivity activity = getActivity();
        Uri placeUri = activity.getContentResolver().insert(RemigesContract.Places.CONTENT_URI, getViewValues());
        if (placeUri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSERT);
            intent.setData(placeUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
    }

    private void updatePlace() {
        FragmentActivity activity = getActivity();
        if (activity.getContentResolver().update(mPlaceUri, getViewValues(), null, null) > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(mPlaceUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
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
