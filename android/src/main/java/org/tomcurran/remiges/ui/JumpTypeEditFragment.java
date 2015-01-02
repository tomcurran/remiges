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

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpTypeEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        EditItemActivity.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeEditFragment.class);

    private static final int STATE_INSERT = 0;
    private static final int STATE_EDIT = 1;

    private static final String SAVE_STATE_JUMPTYPE_URI = "jumptype_uri";
    private static final String SAVE_STATE_JUMPTYPE_STATE = "jumptype_state";

    private int mState;
    private Uri mJumpTypeUri;
    private Cursor mJumpTypeCursor;

    private EditText mJumpTypeName;

    public JumpTypeEditFragment() {
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
                mJumpTypeUri = null;
            } else if (Intent.ACTION_EDIT.equals(action)) {
                mState = STATE_EDIT;
                mJumpTypeUri = intent.getData();
                getLoaderManager().initLoader(0, null, this);
            } else {
                LOGE(TAG, "Unknown action");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            }
        } else {
            mJumpTypeUri = savedInstanceState.getParcelable(SAVE_STATE_JUMPTYPE_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_JUMPTYPE_STATE);
        }

        if (mState == STATE_INSERT) {
            activity.setTitle(R.string.title_jumptype_insert);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jumptype_edit, container, false);

        mJumpTypeName = (EditText) rootView.findViewById(R.id.edit_jumptype_name);

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
        outState.putParcelable(SAVE_STATE_JUMPTYPE_URI, mJumpTypeUri);
        outState.putInt(SAVE_STATE_JUMPTYPE_STATE, mState);
    }

    @Override
    public void onSaveItem() {
        switch (mState) {
            case STATE_INSERT:
                insertJumpType();
                break;
            case STATE_EDIT:
                updateJumpType();
                break;
        }
    }

    private ContentValues getDefaultValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, "");
        return values;
    }

    private ContentValues getViewValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, mJumpTypeName.getText().toString());
        return values;
    }

    private void setViewValues(ContentValues values) {
        mJumpTypeName.setText(values.getAsString(RemigesContract.JumpTypes.JUMPTPYE_NAME));
    }

    private ContentValues passIntentValues(Bundle extras, ContentValues values) {
        ContentValues newValues = new ContentValues(values);
        if (extras.containsKey(RemigesContract.JumpTypes.JUMPTPYE_NAME))
            newValues.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, extras.getString(RemigesContract.JumpTypes.JUMPTPYE_NAME));
        return newValues;
    }

    private void loadJumpType() {
        Cursor cursor = mJumpTypeCursor;
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, cursor.getString(JumpTypeQuery.NAME));
            setViewValues(values);
        }
    }

    private void insertJumpType() {
        FragmentActivity activity = getActivity();
        Uri jumpTypeUri = activity.getContentResolver().insert(RemigesContract.JumpTypes.CONTENT_URI, getViewValues());
        if (jumpTypeUri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSERT);
            intent.setData(jumpTypeUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
    }

    private void updateJumpType() {
        FragmentActivity activity = getActivity();
        if (activity.getContentResolver().update(mJumpTypeUri, getViewValues(), null, null) > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(mJumpTypeUri);
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

}
