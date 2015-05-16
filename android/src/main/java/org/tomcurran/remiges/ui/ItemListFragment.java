package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.ui.widget.MaterialProgressDrawable;
import org.tomcurran.remiges.util.FragmentUtils;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public abstract class ItemListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ItemFragment.ItemListFragment {
    private static final String TAG = makeLogTag(ItemListFragment.class);

    private static final String SAVE_STATE_ITEM_URI = "item_uri";

    protected Uri mItemUri;
    protected CursorAdapter mAdapter;

    private ListView mListView;
    protected TextView mListEmptyMessage;
    protected MaterialProgressDrawable mListEmptyProgress;
    protected ImageView mProgressImage;

    public interface Callbacks {
        public void onItemSelected(Uri uri);
        public void onInsertItem();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Uri uri) {
        }
        @Override
        public void onInsertItem() {
        }
    };

    protected Callbacks mCallbacks = sDummyCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVE_STATE_ITEM_URI)) {
                mItemUri = savedInstanceState.getParcelable(SAVE_STATE_ITEM_URI);
            }
        }

        mAdapter = getListAdapter();

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.item_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemSelected(buildItemUri(id));
            }
        });
        rootView.findViewById(R.id.fab_item_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbacks.onInsertItem();
            }
        });
        mListView.setEmptyView(rootView.findViewById(R.id.item_list_empty));
        mListEmptyMessage = (TextView) rootView.findViewById(R.id.item_list_empty_message);
        mProgressImage = (ImageView) rootView.findViewById(R.id.item_list_empty_progress);
        mListEmptyProgress = getProgressDrawable(rootView);
        mProgressImage.setImageDrawable(mListEmptyProgress);
        checkEmptyList();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mListView.setChoiceMode(getResources().getBoolean(R.bool.has_two_panes) ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
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
        if (mItemUri != null) {
            outState.putParcelable(SAVE_STATE_ITEM_URI, mItemUri);
        }
    }

    @Override
    public void setSelectedItem(Uri uri) {
        mItemUri = uri;
        updateSelectedItem();
    }

    protected void updateSelectedItem() {
        if (mItemUri != null) {
            Long id = Long.parseLong(getItemId(mItemUri));
            ListView listView = mListView;
            if (listView.getSelectedItemId() != id) {
                int listCount = listView.getCount();
                int idColumn = getQueryIdColumn();
                for (int i = 0; i < listCount; i++) {
                    if (id == ((Cursor) listView.getItemAtPosition(i)).getLong(idColumn)) {
                        listView.setItemChecked(i, true);
                        listView.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    protected void checkEmptyList() {
        if (mAdapter.getCursor() != null && mListEmptyProgress != null) {
            mProgressImage.setVisibility(View.GONE);
            mListEmptyMessage.setVisibility(View.VISIBLE);
            mListEmptyProgress.setVisible(false, false);
            mListEmptyProgress.stop();
        }
    }

    protected MaterialProgressDrawable getProgressDrawable(View view) {
        MaterialProgressDrawable progress = new MaterialProgressDrawable(getActivity(), view);
        progress.setAlpha(255);
        progress.setColorSchemeColors(getResources().getColor(R.color.primary));
        progress.updateSizes(MaterialProgressDrawable.LARGE);
        progress.start();
        return progress;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        updateSelectedItem();
        checkEmptyList();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    protected abstract CursorAdapter getListAdapter();
    protected abstract Loader<Cursor> getCursorLoader();
    protected abstract Uri buildItemUri(long id);
    protected abstract String getItemId(Uri uri);
    protected abstract int getQueryIdColumn();

}
