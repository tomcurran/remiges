package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.JumpDetailFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpDetailActivity extends SimpleSinglePaneActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpDetailActivity.class);

    private static final int ACTIVITY_EDIT = 0;
    private static final String SAVE_STATE_JUMP_URI = "jump_uri";

    private Uri mJumpUri;
    private Cursor mJumpCursor;

    @Override
    protected Fragment onCreatePane() {
        return new JumpDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mJumpUri = getIntent().getData();
        } else {
            mJumpUri = savedInstanceState.getParcelable(SAVE_STATE_JUMP_URI);
        }

        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_JUMP_URI, mJumpUri);
    }

    @Override
    public void onEditJump(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(this, JumpEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    @Override
    public void onDeleteJump(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

    private void loadJump() {
        if (mJumpCursor.moveToFirst()) {
            setTitle(getString(R.string.title_jump_detail_number, mJumpCursor.getInt(JumpQuery.NUMBER)));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                this,
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
                RemigesContract.Jumps._ID
        };

        int NUMBER = 0;

    }

}
