package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;
import org.tomcurran.remiges.util.TimeUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpListFragment.class);

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private int mActivatedPosition = ListView.INVALID_POSITION;

    private CursorAdapter mAdapter;

    public interface Callbacks {
        public void onJumpSelected(Uri uri);
        public void onInsertJump();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onJumpSelected(Uri uri) {
        }
        @Override
        public void onInsertJump() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new JumpListAdapter(getActivity());
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.jump_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_jump_list_insert:
                insertJump();
                return true;
        }
        return false;
    }

    private void insertJump() {
        mCallbacks.onInsertJump();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        mCallbacks.onJumpSelected(RemigesContract.Jumps.buildJumpUri(id));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    public void setSelectedJump(String jumpId) {
        Long id = Long.parseLong(jumpId);
        if (id == null) {
            return;
        }
        ListView listView = getListView();
        for (int i = 0; i < listView.getCount(); i++) {
            if (id == ((Cursor) listView.getItemAtPosition(i)).getLong(JumpsQuery._ID)) {
                listView.setSelection(i);
                break;
            }
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }

    @Override
    public void onEditJump(Uri uri) {
    }

    @Override
    public void onDeleteJump(Uri uri) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                RemigesContract.Jumps.CONTENT_URI,
                JumpsQuery.PROJECTION,
                null,
                null,
                RemigesContract.Jumps.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
    }

    public static class JumpListAdapter extends SimpleCursorAdapter implements SimpleCursorAdapter.ViewBinder {

        private static final String[] FROM = {
                RemigesContract.Jumps.JUMP_NUMBER,
                RemigesContract.Jumps.JUMP_DATE,
                RemigesContract.Jumps.JUMP_WAY,
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.Jumps.JUMP_DESCRIPTION
        };

        private static final int[] TO = {
                R.id.list_item_jump_number,
                R.id.list_item_jump_date,
                R.id.list_item_jump_way,
                R.id.list_item_jump_type,
                R.id.list_item_jump_description
        };

        public JumpListAdapter(Context context) {
            super(context, R.layout.list_item_jumps, null, FROM, TO, 0);
            setViewBinder(this);
        }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (columnIndex) {
                case JumpsQuery.DATE: {
                    ViewHolder holder = getViewHolder(view);
                    holder.date.setText(
                            TimeUtils.getTimeAgo(view.getContext(), cursor.getLong(JumpsQuery.DATE))
                    );
                    return true;
                }
                case JumpsQuery.WAY: {
                    ViewHolder holder = getViewHolder(view);
                    int way = cursor.getInt(JumpsQuery.WAY);
                    if (way > 1) {
                        holder.way.setText(view.getContext().getString(R.string.list_jump_way, way));
                    } else {
                        holder.way.setText(view.getContext().getString(R.string.list_jump_solo));
                    }
                    return true;
                }
                case JumpsQuery.DESCRIPTION: {
                    ViewHolder holder = getViewHolder(view);
                    String description = cursor.getString(JumpsQuery.DESCRIPTION);
                    holder.description.setText(description);
                    holder.description.setVisibility(description.isEmpty() ? View.GONE : View.VISIBLE);
                    return true;
                }
                default:
                    return false;
            }
        }

        private ViewHolder getViewHolder(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.date = (TextView) view.findViewById(R.id.list_item_jump_date);
                holder.way = (TextView) view.findViewById(R.id.list_item_jump_way);
                holder.description = (TextView) view.findViewById(R.id.list_item_jump_description);
                view.setTag(holder);
            }
            return holder;
        }

        static class ViewHolder {
            TextView date;
            TextView way;
            TextView description;
        }

    }

    private interface JumpsQuery {

        String[] PROJECTION = {
                RemigesContract.Jumps.JUMP_NUMBER,
                RemigesContract.Jumps.JUMP_DATE,
                RemigesContract.Jumps.JUMP_WAY,
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.Jumps.JUMP_DESCRIPTION,
                RemigesContract.Jumps._ID
        };

        int NUMBER = 0;
        int DATE = 1;
        int WAY = 2;
        int TYPE = 3;
        int DESCRIPTION = 4;
        int _ID = 5;

    }

}
