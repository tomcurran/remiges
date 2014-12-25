package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpTypeListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpDetailFragment.class);

    private static final String SAVE_STATE_JUMPTYPE_URI = "jumptype_uri";

    private Uri mJumpTypeUri;

    private ListView mListView;

    public interface Callbacks {
        public void onJumpTypeSelected(String jumpTypeId);
        public void onInsertJumpType();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onJumpTypeSelected(String jumpTypeId) {
        }
        @Override
        public void onInsertJumpType() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    private CursorAdapter mAdapter;

    public JumpTypeListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVE_STATE_JUMPTYPE_URI)) {
                mJumpTypeUri = savedInstanceState.getParcelable(SAVE_STATE_JUMPTYPE_URI);
            }
        }

        mAdapter = new JumpTypeListAdapter(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jumptype_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.jumptype_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onJumpTypeSelected(String.valueOf(id));
            }
        });
        rootView.findViewById(R.id.fab_jumptype_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertJumpType();
            }
        });
        return rootView;
    }

    private void insertJumpType() {
        mCallbacks.onInsertJumpType();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setActivateOnItemClick(getResources().getBoolean(R.bool.has_two_panes));
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mJumpTypeUri != null) {
            outState.putParcelable(SAVE_STATE_JUMPTYPE_URI, mJumpTypeUri);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        mListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    public void setSelectedJumpType(Uri uri) {
        mJumpTypeUri = uri;
        updateSelectedJumpType();
    }

    private void updateSelectedJumpType() {
        if (mJumpTypeUri != null) {
            Long id = Long.parseLong(RemigesContract.JumpTypes.getJumpTypeId(mJumpTypeUri));
            ListView listView = mListView;
            if (listView.getSelectedItemId() != id) {
                int listCount = listView.getCount();
                for (int i = 0; i < listCount; i++) {
                    if (id == ((Cursor) listView.getItemAtPosition(i)).getLong(JumpTypeQuery._ID)) {
                        listView.setItemChecked(i, true);
                        listView.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                RemigesContract.JumpTypes.CONTENT_URI,
                JumpTypeQuery.PROJECTION,
                null,
                null,
                RemigesContract.JumpTypes.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        updateSelectedJumpType();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public static class JumpTypeListAdapter extends SimpleCursorAdapter {

        private static final String[] FROM = {
                RemigesContract.JumpTypes.JUMPTPYE_NAME
        };

        private static final int[] TO = {
                R.id.list_item_jumptype_name
        };

        public JumpTypeListAdapter(Context context) {
            super(context, R.layout.list_item_jumptypes, null, FROM, TO, 0);
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
