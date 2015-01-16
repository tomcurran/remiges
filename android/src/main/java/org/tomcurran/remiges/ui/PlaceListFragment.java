package org.tomcurran.remiges.ui;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class PlaceListFragment extends ItemListFragment {
    private static final String TAG = makeLogTag(PlaceListFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        rootView.findViewById(R.id.fab_item_list).setContentDescription(getResources().getString(R.string.fab_place_list));
        return rootView;
    }

    @Override
    protected CursorAdapter getListAdapter() {
        return new PlaceListAdapter(getActivity());
    }

    @Override
    protected Loader<Cursor> getCursorLoader() {
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
    protected Uri buildItemUri(long id) {
        return RemigesContract.Places.buildPlaceUri(id);
    }

    @Override
    protected String getItemId(Uri uri) {
        return RemigesContract.JumpTypes.getJumpTypeId(uri);
    }

    @Override
    protected int getQueryIdColumn() {
        return PlaceQuery._ID;
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
