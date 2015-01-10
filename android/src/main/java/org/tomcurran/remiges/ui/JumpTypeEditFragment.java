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


public class JumpTypeEditFragment extends ItemEditFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        EditItemActivity.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeEditFragment.class);

    private Cursor mJumpTypeCursor;

    private EditText mJumpTypeName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (mState) {
            case STATE_INSERT:
                getActivity().setTitle(R.string.title_jumptype_insert);
                break;
            case STATE_EDIT:
                getLoaderManager().initLoader(0, null, this);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jumptype_edit, container, false);

        mJumpTypeName = (EditText) rootView.findViewById(R.id.edit_jumptype_name);

        return rootView;
    }

    @Override
    protected Uri getContentUri() {
        return RemigesContract.JumpTypes.CONTENT_URI;
    }

    @Override
    protected ContentValues getDefaultValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, "");
        return values;
    }

    @Override
    protected ContentValues getViewValues() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.JumpTypes.JUMPTPYE_NAME, mJumpTypeName.getText().toString());
        return values;
    }

    @Override
    protected void setViewValues(ContentValues values) {
        mJumpTypeName.setText(values.getAsString(RemigesContract.JumpTypes.JUMPTPYE_NAME));
    }

    @Override
    protected ContentValues passIntentValues(Bundle extras, ContentValues values) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mItemUri,
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
