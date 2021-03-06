package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.PlaceDetailFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class PlaceDetailActivity extends SimpleSinglePaneActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        PlaceDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(PlaceDetailActivity.class);

    private static final int ACTIVITY_EDIT = 0;
    private static final String SAVE_STATE_PLACE_URI = "place_uri";

    private Uri mPlaceUri;
    private Cursor mPlaceCursor;

    @Override
    protected Fragment onCreatePane() {
        return new PlaceDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mPlaceUri = getIntent().getData();
        } else {
            mPlaceUri = savedInstanceState.getParcelable(SAVE_STATE_PLACE_URI);
        }

        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_PLACE_URI, mPlaceUri);
    }

    @Override
    public void onEditPlace(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(this, PlaceEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    @Override
    public void onDeletePlace(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

    private void loadPlace() {
        if (mPlaceCursor.moveToFirst()) {
            String name = mPlaceCursor.getString(PlaceQuery.NAME);
            if (!TextUtils.isEmpty(name)) {
                setTitle(name);
            } else {
                setTitle(R.string.title_place_detail);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                mPlaceUri,
                PlaceQuery.PROJECTION,
                null,
                null,
                RemigesContract.Places.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPlaceCursor = data;
        loadPlace();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPlaceCursor = null;
    }

    private interface PlaceQuery {

        String[] PROJECTION = {
                RemigesContract.Places.PLACE_NAME,
                RemigesContract.Places._ID
        };

        int NAME = 0;

    }

}
