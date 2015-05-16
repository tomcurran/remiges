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
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpTypeListFragment extends ItemListFragment implements ItemFragment.ItemListFragment {
    private static final String TAG = makeLogTag(JumpDetailFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        rootView.findViewById(R.id.fab_item_list).setContentDescription(getResources().getString(R.string.fab_jumptype_list));
        ((TextView)rootView.findViewById(R.id.item_list_empty_message)).setText(R.string.list_jumptype_empty);
        return rootView;
    }

    @Override
    protected CursorAdapter getListAdapter() {
        return new JumpTypeListAdapter(getActivity());
    }

    @Override
    protected Loader<Cursor> getCursorLoader() {
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
    protected Uri buildItemUri(long id) {
        return RemigesContract.JumpTypes.buildJumpTypeUri(id);
    }

    @Override
    protected String getItemId(Uri uri) {
        return RemigesContract.JumpTypes.getJumpTypeId(uri);
    }

    @Override
    protected int getQueryIdColumn() {
        return JumpTypeQuery._ID;
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
