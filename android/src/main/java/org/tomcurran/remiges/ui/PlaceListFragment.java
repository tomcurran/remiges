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

public class PlaceListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ItemFragment.ItemListFragment {
    private static final String TAG = makeLogTag(PlaceListFragment.class);

    private static final String SAVE_STATE_PLACE_URI = "place_uri";

    private Uri mPlaceUri;

    private ListView mListView;

    private CursorAdapter mAdapter;

    public interface Callbacks {
        public void onPlaceSelected(Uri uri);
        public void onInsertPlace();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onPlaceSelected(Uri uri) {
        }
        @Override
        public void onInsertPlace() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public PlaceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVE_STATE_PLACE_URI)) {
                mPlaceUri = savedInstanceState.getParcelable(SAVE_STATE_PLACE_URI);
            }
        }

        mAdapter = new PlaceListAdapter(getActivity());

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.place_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onPlaceSelected(RemigesContract.Places.buildPlaceUri(id));
            }
        });
        rootView.findViewById(R.id.fab_place_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertPlace();
            }
        });
        return rootView;
    }

    private void insertPlace() {
        mCallbacks.onInsertPlace();
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
        if (mPlaceUri != null) {
            outState.putParcelable(SAVE_STATE_PLACE_URI, mPlaceUri);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        mListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    public void setSelectedItem(Uri uri) {
        mPlaceUri = uri;
        updateSelectedPlace();
    }

    private void updateSelectedPlace() {
        if (mPlaceUri != null) {
            Long id = Long.parseLong(RemigesContract.JumpTypes.getJumpTypeId(mPlaceUri));
            ListView listView = mListView;
            if (listView.getSelectedItemId() != id) {
                int listCount = listView.getCount();
                for (int i = 0; i < listCount; i++) {
                    if (id == ((Cursor) listView.getItemAtPosition(i)).getLong(PlaceQuery._ID)) {
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
                RemigesContract.Places.CONTENT_URI,
                PlaceQuery.PROJECTION,
                null,
                null,
                RemigesContract.Places.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        updateSelectedPlace();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public static class PlaceListAdapter extends SimpleCursorAdapter {

        private static final String[] FROM = {
                RemigesContract.Places.PLACE_NAME
        };

        private static final int[] TO = {
                R.id.list_item_place_name
        };

        public PlaceListAdapter(Context context) {
            super(context, R.layout.list_item_places, null, FROM, TO, 0);
        }

    }

    private interface PlaceQuery {

        String[] PROJECTION = {
                RemigesContract.Places.PLACE_NAME,
                RemigesContract.Places._ID
        };

        int NAME = 0;
        int _ID = 1;

    }

}
