package org.tomcurran.remiges.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.TimeUtils;


public class StatisticsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_ID = "id";
    private static final String ARG_SELECTION = "selection";

    private static final int LOADER_STAT_JUMP_COUNT = 0;
    private static final int LOADER_STAT_LAST_JUMP = 1;

    private String mId;
    private String mSelection;

    private Cursor mJumpCountCursor;
    private Cursor mLastJumpCursor;

    private TextView mJumpCount;
    private TextView mLastJump;

    public static StatisticsFragment newInstance(String id, String selection) {
        StatisticsFragment f = new StatisticsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_SELECTION, selection);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mId = args.getString(ARG_ID);
        mSelection = args.getString(ARG_SELECTION) + "=?";

        getLoaderManager().initLoader(LOADER_STAT_JUMP_COUNT, null, this);
        getLoaderManager().initLoader(LOADER_STAT_LAST_JUMP, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        mJumpCount = (TextView) rootView.findViewById(R.id.detail_jump_count);
        mLastJump = (TextView) rootView.findViewById(R.id.detail_jump_last);

        return rootView;
    }

    private void loadJumpCount() {
        Cursor cursor = mJumpCountCursor;
        if (cursor.moveToFirst()) {
            mJumpCount.setText(cursor.getString(CountQuery.COUNT));
        }
    }

    private void loadLastJump() {
        Cursor cursor = mLastJumpCursor;
        if (cursor.moveToFirst()) {
            long date = cursor.getLong(LastJumpQuery.DATE);
            if (date == 0) {
                mLastJump.setText(R.string.stats_last_jump_none);
            } else {
                mLastJump.setText(TimeUtils.getTimeAgo(getActivity(), date));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_STAT_JUMP_COUNT:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.Jumps.CONTENT_URI,
                        CountQuery.PROJECTION,
                        mSelection,
                        new String[] { mId },
                        CountQuery.SORT
                );
            case LOADER_STAT_LAST_JUMP:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.Jumps.CONTENT_URI,
                        LastJumpQuery.PROJECTION,
                        mSelection,
                        new String[] { mId },
                        LastJumpQuery.SORT
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_STAT_JUMP_COUNT:
                mJumpCountCursor = cursor;
                loadJumpCount();
                break;
            case LOADER_STAT_LAST_JUMP:
                mLastJumpCursor = cursor;
                loadLastJump();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_STAT_JUMP_COUNT:
                mJumpCountCursor = null;
                break;
            case LOADER_STAT_LAST_JUMP:
                mLastJumpCursor = null;
                break;
        }
    }

    private interface CountQuery {

        String[] PROJECTION = {
                "count(" + RemigesContract.Jumps.JUMP_NUMBER + ")"
        };

        String SORT = "count(" + RemigesContract.Jumps.JUMP_NUMBER + ")";

        int COUNT = 0;

    }

    private interface LastJumpQuery {

        String[] PROJECTION = {
                "max(" + RemigesContract.Jumps.JUMP_DATE + ")",
                RemigesContract.Jumps.JUMP_DATE
        };

        String SORT = RemigesContract.Jumps.JUMP_DATE;

        int MAX_DATE = 0;
        int DATE = 1;

    }
}
