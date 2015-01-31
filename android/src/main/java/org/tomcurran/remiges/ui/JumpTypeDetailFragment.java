package org.tomcurran.remiges.ui;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpTypeDetailFragment.class);

    private static final String SAVE_STATE_JUMPTYPE_URI = "jumptype_uri";

    private static final int DIALOG_FRAGMENT = 0;

    private static final int LOADER_JUMPTYPE_DETAIL = 0;

    private Uri mJumpTypeUri;
    private Cursor mJumpTypeCursor;

    private TextView mJumpTypeName;

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

        getLoaderManager().initLoader(LOADER_JUMPTYPE_DETAIL, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jumptype_detail, container, false);

        mJumpTypeName = (TextView) rootView.findViewById(R.id.detail_jumptype_name);

        if (savedInstanceState == null) {
            StatisticsFragment statisticsFragment = StatisticsFragment.newInstance(
                    RemigesContract.JumpTypes.getJumpTypeId(mJumpTypeUri), RemigesContract.Jumps.JUMPTYPE_ID);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.detail_jumptype_statistics, statisticsFragment).commit();
        }

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
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_JUMPTYPE_DETAIL:
                mJumpTypeCursor = null;
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

}
