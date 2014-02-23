package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

/**
 * A fragment representing a single Jump detail screen.
 * This fragment is either contained in a {@link JumpListActivity}
 * in two-pane mode (on tablets) or a {@link JumpDetailActivity}
 * on handsets.
 */
public class JumpDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpDetailFragment.class);

    private static final String SAVE_STATE_JUMP_URI = "jump_uri";

    private Uri mJumpUri;
    private Cursor mJumpCursor;

    private TextView mJumpNumber;
    private TextView mJumpDate;
    private TextView mJumpDescription;
    private TextView mJumpWay;
    private TextView mJumpType;
    private TextView mJumpExitAltitude;
    private TextView mJumpDeploymentAltitude;
    private TextView mJumpDelay;

    public interface Callbacks {
        public void onEditJump(Uri uri);
        public void onDeleteJump(Uri uri);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onEditJump(Uri uri) {
        }
        @Override
        public void onDeleteJump(Uri uri) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            mJumpUri = intent.getData();
        } else {
            mJumpUri = savedInstanceState.getParcelable(SAVE_STATE_JUMP_URI);
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jump_detail, container, false);

        mJumpNumber = (TextView) rootView.findViewById(R.id.detail_jump_number);
        mJumpDate = (TextView) rootView.findViewById(R.id.detail_jump_date);
        mJumpDescription = (TextView) rootView.findViewById(R.id.detail_jump_description);
        mJumpWay = (TextView) rootView.findViewById(R.id.detail_jump_way);
        mJumpType = (TextView) rootView.findViewById(R.id.detail_jump_type);
        mJumpExitAltitude = (TextView) rootView.findViewById(R.id.detail_jump_exit_altitude);
        mJumpDeploymentAltitude = (TextView) rootView.findViewById(R.id.detail_jump_deployment_altitude);
        mJumpDelay = (TextView) rootView.findViewById(R.id.detail_jump_delay);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.jump_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_jump_detail_edit:
                editJump();
                return true;
            case R.id.menu_jump_detail_delete:
                deleteJump();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_JUMP_URI, mJumpUri);
    }

    private void editJump() {
        mCallbacks.onEditJump(mJumpUri);
    }

    private void deleteJump() {
        int rowsDeleted = getActivity().getContentResolver().delete(mJumpUri, null, null);
        if (rowsDeleted > 0) {
            mCallbacks.onDeleteJump(mJumpUri);
        }
    }

    private void loadJump() {
        Cursor jumpCursor = mJumpCursor;
        if (jumpCursor.moveToFirst()) {
            mJumpNumber.setText(jumpCursor.getString(JumpQuery.NUMBER));
            mJumpDate.setText(DateFormat.format(getString(R.string.format_detail_jump_date), jumpCursor.getLong(JumpQuery.DATE)));
            mJumpDescription.setText(jumpCursor.getString(JumpQuery.DESCRIPTION));
            int way = jumpCursor.getInt(JumpQuery.WAY);
            mJumpWay.setText(way > 1 ? getString(R.string.detail_jump_way, way) : getString(R.string.detail_jump_solo));
            mJumpType.setText(jumpCursor.getString(JumpQuery.TYPE));
            mJumpExitAltitude.setText(jumpCursor.getString(JumpQuery.EXIT_ALTITUDE));
            mJumpDeploymentAltitude.setText(jumpCursor.getString(JumpQuery.DEPLOYMENT_ALTITUDE));
            mJumpDelay.setText(jumpCursor.getString(JumpQuery.DELAY));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
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
                RemigesContract.Jumps.JUMP_DATE,
                RemigesContract.Jumps.JUMP_DESCRIPTION,
                RemigesContract.Jumps.JUMP_WAY,
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.Jumps.JUMP_EXIT_ALTITUDE,
                RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE,
                RemigesContract.Jumps.JUMP_DELAY,
                RemigesContract.Jumps._ID
        };

        int NUMBER = 0;
        int DATE = 1;
        int DESCRIPTION = 2;
        int WAY = 3;
        int TYPE = 4;
        int EXIT_ALTITUDE = 5;
        int DEPLOYMENT_ALTITUDE = 6;
        int DELAY = 7;

    }

}
