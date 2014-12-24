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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;
import org.tomcurran.remiges.util.TimeUtils;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpTypeDetailFragment.class);

    private static final String SAVE_STATE_JUMPTYPE_URI = "jumptype_uri";

    private static final int DIALOG_FRAGMENT = 0;

    private static final int LOADER_JUMPTYPE_DETAIL = 0;
    private static final int LOADER_JUMPTYPE_STAT_JUMP_COUNT = 1;
    private static final int LOADER_JUMPTYPE_STAT_LAST_JUMP = 2;

    private Uri mJumpTypeUri;
    private Cursor mJumpTypeCursor;
    private Cursor mJumpTypeJumpCountCursor;
    private Cursor mJumpTypeLastJumpCursor;

    private TextView mJumpTypeName;
    private TextView mJumpTypeJumpCount;
    private TextView mJumpTypeLastJump;
    private Typeface mRoboto;

    public interface Callbacks {
        public void onEditJumpType(String jumpTypeId);
        public void onDeleteJumpType(String jumpTypeId);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onEditJumpType(String jumpTypeId) {
        }
        @Override
        public void onDeleteJumpType(String jumpTypeId) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpTypeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            mJumpTypeUri = intent.getData();
        } else {
            mJumpTypeUri = savedInstanceState.getParcelable(SAVE_STATE_JUMPTYPE_URI);
        }

        mRoboto = UIUtils.loadFont(getActivity(), UIUtils.FONT_ROBOTO_THIN);

        getLoaderManager().initLoader(LOADER_JUMPTYPE_DETAIL, null, this);
        getLoaderManager().initLoader(LOADER_JUMPTYPE_STAT_JUMP_COUNT, null, this);
        getLoaderManager().initLoader(LOADER_JUMPTYPE_STAT_LAST_JUMP, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jumptype_detail, container, false);

        mJumpTypeName = (TextView) rootView.findViewById(R.id.detail_jumptype_name);
        mJumpTypeJumpCount = (TextView) rootView.findViewById(R.id.detail_jumptype_jump_count);
        mJumpTypeLastJump = (TextView) rootView.findViewById(R.id.detail_jumptype_jump_last);

        mJumpTypeName.setTypeface(mRoboto);

        return rootView;
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
        inflater.inflate(R.menu.jumptype_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_jumptype_detail_edit:
                editJumpType();
                return true;
            case R.id.menu_jumptype_detail_delete:
                DialogFragment dialog = DeleteItemDialogFragment.newInstance(R.string.dialog_jumptype_delete_message);
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
                    deleteJumpType();
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_JUMPTYPE_URI, mJumpTypeUri);
    }

    private void editJumpType() {
        mCallbacks.onEditJumpType(RemigesContract.JumpTypes.getJumpTypeId(mJumpTypeUri));
    }

    private void deleteJumpType() {
        int rowsDeleted = getActivity().getContentResolver().delete(mJumpTypeUri, null, null);
        if (rowsDeleted > 0) {
            mCallbacks.onDeleteJumpType(RemigesContract.JumpTypes.getJumpTypeId(mJumpTypeUri));
        }
    }

    private void loadJumpType() {
        Cursor cursor = mJumpTypeCursor;
        if (cursor.moveToFirst()) {
            mJumpTypeName.setText(cursor.getString(JumpTypeQuery.NAME));
        }
    }

    private void loadJumpCount() {
        Cursor cursor = mJumpTypeJumpCountCursor;
        if (cursor.moveToFirst()) {
            mJumpTypeJumpCount.setText(cursor.getString(JumpTypeCountQuery.COUNT));
        }
    }

    private void loadLastJump() {
        Cursor cursor = mJumpTypeLastJumpCursor;
        if (cursor.moveToFirst()) {
            long date = cursor.getLong(JumpTypeLastJumpQuery.DATE);
            if (date == 0) {
                mJumpTypeLastJump.setText(R.string.detail_place_last_jump_none);
            } else {
                mJumpTypeLastJump.setText(TimeUtils.getTimeAgo(getActivity(), date));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_JUMPTYPE_DETAIL:
                return new CursorLoader(
                        getActivity(),
                        mJumpTypeUri,
                        JumpTypeQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.JumpTypes.DEFAULT_SORT
                );
            case LOADER_JUMPTYPE_STAT_JUMP_COUNT:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.Jumps.CONTENT_URI,
                        JumpTypeCountQuery.PROJECTION,
                        JumpTypeCountQuery.SELECTION,
                        new String[] { RemigesContract.JumpTypes.getJumpTypeId(mJumpTypeUri) },
                        JumpTypeCountQuery.SORT
                );
            case LOADER_JUMPTYPE_STAT_LAST_JUMP:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.Jumps.CONTENT_URI,
                        JumpTypeLastJumpQuery.PROJECTION,
                        JumpTypeLastJumpQuery.SELECTION,
                        new String[] { RemigesContract.JumpTypes.getJumpTypeId(mJumpTypeUri) },
                        JumpTypeLastJumpQuery.SORT
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_JUMPTYPE_DETAIL:
                mJumpTypeCursor = cursor;
                loadJumpType();
                break;
            case LOADER_JUMPTYPE_STAT_JUMP_COUNT:
                mJumpTypeJumpCountCursor = cursor;
                loadJumpCount();
                break;
            case LOADER_JUMPTYPE_STAT_LAST_JUMP:
                mJumpTypeLastJumpCursor = cursor;
                loadLastJump();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_JUMPTYPE_DETAIL:
                mJumpTypeCursor = null;
                break;
            case LOADER_JUMPTYPE_STAT_JUMP_COUNT:
                mJumpTypeJumpCountCursor = null;
                break;
            case LOADER_JUMPTYPE_STAT_LAST_JUMP:
                mJumpTypeLastJumpCursor = null;
                break;
        }
    }

    private interface JumpTypeQuery {

        String[] PROJECTION = {
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.JumpTypes._ID
        };

        int NAME = 0;
        int _ID = 1;

    }

    private interface JumpTypeCountQuery {

        String[] PROJECTION = {
                "count(" + RemigesContract.Jumps.JUMP_NUMBER + ")"
        };

        String SELECTION = RemigesContract.Jumps.JUMPTYPE_ID + "=?";

        String SORT = "count(" + RemigesContract.Jumps.JUMP_NUMBER + ")";

        int COUNT = 0;

    }

    private interface JumpTypeLastJumpQuery {

        String[] PROJECTION = {
                "max(" + RemigesContract.Jumps.JUMP_DATE + ")",
                RemigesContract.Jumps.JUMP_DATE
        };

        String SELECTION = RemigesContract.Jumps.JUMPTYPE_ID + "=?";

        String SORT = RemigesContract.Jumps.JUMP_DATE;

        int MAX_DATE = 0;
        int DATE = 1;

    }

}
