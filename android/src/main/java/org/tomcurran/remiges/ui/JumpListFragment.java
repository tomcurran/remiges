package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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

import org.json.JSONException;
import org.tomcurran.remiges.BuildConfig;
import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.TestData;
import org.tomcurran.remiges.util.TimeUtils;

import java.text.ParseException;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

/**
 * A list fragment representing a list of Jumps. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link JumpDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link org.tomcurran.remiges.ui.JumpListFragment.Callbacks}
 * interface.
 */
public class JumpListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpListFragment.class);

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private TestData mTestData;
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

        mTestData = new TestData(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.jump_list, menu);
        if (BuildConfig.DEBUG) {
            inflater.inflate(R.menu.debug, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_debug_insert_data:
                try {
                    mTestData.insert();
                } catch (JSONException e) {
                    LOGE(TAG, String.format("Test data JSON error: %s", e.getMessage()));
                } catch (ParseException e) {
                    LOGE(TAG, String.format("Test data JSON parse error: %s", e.getMessage()));
                } catch (RemoteException e) {
                    LOGE(TAG, String.format("Test data provider communication error: %s", e.getMessage()));
                } catch (OperationApplicationException e) {
                    LOGE(TAG, String.format("Test data insertion error: %s", e.getMessage()));
                }
                return true;
            case R.id.menu_debug_delete_data:
                try {
                    mTestData.delete();
                } catch (RemoteException e) {
                    LOGE(TAG, String.format("Test data provider communication error: %s", e.getMessage()));
                } catch (OperationApplicationException e) {
                    LOGE(TAG, String.format("Test data deletion error: %s", e.getMessage()));
                }
                return true;
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

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onJumpSelected(RemigesContract.Jumps.buildJumpUri(id));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
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
                view.setTag(holder);
            }
            return holder;
        }

        static class ViewHolder {
            TextView date;
            TextView way;
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

    }

}
