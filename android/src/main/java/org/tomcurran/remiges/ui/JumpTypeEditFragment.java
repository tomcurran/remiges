package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpTypeEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpTypeEditFragment.class);

    private static final int STATE_INSERT = 0;
    private static final int STATE_EDIT = 1;

    private static final String SAVE_STATE_JUMPTYPE_URI = "jumptype_uri";
    private static final String SAVE_STATE_JUMPTYPE_STATE = "jumptype_state";

    private int mState;
    private Uri mJumpTypeUri;
    private Cursor mJumpTypeCursor;

    private EditText mJumpTypeName;

    public interface Callbacks {
        public void onJumpTypeEdited(Uri uri);
        public void onDeleteJumpType(Uri uri);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onJumpTypeEdited(Uri uri) {
        }
        @Override
        public void onDeleteJumpType(Uri uri) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpTypeEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        FragmentActivity activity = (FragmentActivity) getActivity();

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_INSERT)) {
                mState = STATE_INSERT;
                // TODO: incorporate values passed in ?
                ContentValues values = new ContentValues();
                values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, "");
                mJumpTypeUri = activity.getContentResolver().insert(intent.getData(), values);
                if (mJumpTypeUri == null) {
                    LOGE(TAG, "Failed to insert new jump type into " + intent.getData());
                    activity.setResult(FragmentActivity.RESULT_CANCELED);
                    activity.finish();
                    return;
                }
            } else if (action.equals(Intent.ACTION_EDIT)) {
                mState = STATE_EDIT;
                mJumpTypeUri = intent.getData();
            } else {
                LOGE(TAG, "Unknown action. Exiting");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            }
        } else {
            mJumpTypeUri = savedInstanceState.getParcelable(SAVE_STATE_JUMPTYPE_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_JUMPTYPE_STATE);
        }
        activity.setResult(FragmentActivity.RESULT_OK, (new Intent()).setAction(mJumpTypeUri.toString()));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jumptype_edit, container, false);

        mJumpTypeName = (EditText) rootView.findViewById(R.id.edit_jumptype_name);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateJump();
        outState.putParcelable(SAVE_STATE_JUMPTYPE_URI, mJumpTypeUri);
        outState.putInt(SAVE_STATE_JUMPTYPE_STATE, mState);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateJump();
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
        inflater.inflate(R.menu.jumptype_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_jumptype_edit_delete:
                deleteJump();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadJumpType() {
        Cursor jumpTypeCursor = mJumpTypeCursor;
        if (jumpTypeCursor.moveToFirst()) {
            mJumpTypeName.setText(mJumpTypeCursor.getString(JumpTypeQuery.NAME));
        }
    }

    private void updateJump() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, mJumpTypeName.getText().toString());
        int rowsUpdate = getActivity().getContentResolver().update(mJumpTypeUri, values, null, null);
        if (rowsUpdate > 0) {
            mCallbacks.onJumpTypeEdited(mJumpTypeUri);
        }
    }

    private void deleteJump() {
        int rowsDeleted = getActivity().getContentResolver().delete(mJumpTypeUri, null, null);
        if (rowsDeleted > 0) {
            mCallbacks.onDeleteJumpType(mJumpTypeUri);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mJumpTypeUri,
                JumpTypeQuery.PROJECTION,
                null,
                null,
                RemigesContract.JumpTypes.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mJumpTypeCursor = cursor;
        loadJumpType();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mJumpTypeCursor = null;
    }

    private interface JumpTypeQuery {

        String[] PROJECTION = {
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.JumpTypes._ID
        };

        int NAME = 0;
        int _ID = 1;

    }

    public static class JumpTypeAdapter extends SimpleCursorAdapter {

        private final static int[] TO = {
                android.R.id.text1
        };

        public JumpTypeAdapter(Context context, final String[] projection) {
            super(context, android.R.layout.simple_spinner_item, null, projection, JumpTypeAdapter.TO, 0);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

    }

}
