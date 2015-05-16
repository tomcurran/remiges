package org.tomcurran.remiges.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.TimeUtils;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpListFragment extends ItemListFragment {
    private static final String TAG = makeLogTag(JumpListFragment.class);

    private StickyListHeadersListView mHeaderListView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jump_list, container, false);
        mHeaderListView = (StickyListHeadersListView) rootView.findViewById(R.id.jump_list);
        mHeaderListView.setAdapter((StickyListHeadersAdapter) mAdapter);
        mHeaderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemSelected(buildItemUri(id));
            }
        });
        rootView.findViewById(R.id.fab_jump_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbacks.onInsertItem();
            }
        });
        mHeaderListView.setEmptyView(rootView.findViewById(R.id.jump_list_empty));
        mListEmptyMessage = (TextView) rootView.findViewById(R.id.jump_list_empty_message);
        mProgressImage = (ImageView) rootView.findViewById(R.id.jump_list_empty_progress);
        mListEmptyProgress = getProgressDrawable(rootView);
        mProgressImage.setImageDrawable(mListEmptyProgress);
        checkEmptyList();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mHeaderListView.setChoiceMode(getResources().getBoolean(R.bool.has_two_panes) ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    @Override
    protected void updateSelectedItem() {
        if (mItemUri != null) {
            Long id = Long.parseLong(getItemId(mItemUri));
            StickyListHeadersListView headerListView = mHeaderListView;
            if (headerListView.getWrappedList().getSelectedItemId() != id) {
                int listCount = headerListView.getCount();
                int idColumn = getQueryIdColumn();
                for (int i = 0; i < listCount; i++) {
                    if (id == ((Cursor) headerListView.getItemAtPosition(i)).getLong(idColumn)) {
                        headerListView.setItemChecked(i, true);
                        headerListView.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected CursorAdapter getListAdapter() {
        return new JumpListAdapter(getActivity());
    }

    @Override
    protected Loader<Cursor> getCursorLoader() {
        return new CursorLoader(
                getActivity(),
                RemigesContract.Jumps.CONTENT_URI,
                JumpsQuery.PROJECTION,
                null,
                null,
                RemigesContract.Jumps.DEFAULT_SORT
        );
    }

    protected Uri buildItemUri(long id) {
        return RemigesContract.Jumps.buildJumpUri(id);
    }

    protected String getItemId(Uri uri) {
        return RemigesContract.Jumps.getJumpId(uri);
    }

    protected int getQueryIdColumn() {
        return JumpsQuery._ID;
    }

    public static class JumpListAdapter extends SimpleCursorAdapter implements
            SimpleCursorAdapter.ViewBinder, StickyListHeadersAdapter {

        private FragmentActivity mActivity;

        private static final String[] FROM = {
                RemigesContract.Jumps.JUMP_NUMBER,
                RemigesContract.Jumps.JUMP_WAY,
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.Jumps.JUMP_DESCRIPTION
        };

        private static final int[] TO = {
                R.id.list_item_jump_number,
                R.id.list_item_jump_way_type,
                R.id.list_item_jump_way_type,
                R.id.list_item_jump_description
        };

        public JumpListAdapter(FragmentActivity context) {
            super(context, R.layout.list_item_jumps, null, FROM, TO, 0);
            mActivity = context;
            setViewBinder(this);
        }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (columnIndex) {
                case JumpsQuery.WAY: {
                    ViewHolder holder = getViewHolder(view);
                    int way = cursor.getInt(JumpsQuery.WAY);
                    if (way > 1) {
                        holder.wayType.setText(view.getContext().getString(R.string.list_jump_way, way));
                    } else {
                        holder.wayType.setText(view.getContext().getString(R.string.list_jump_solo));
                    }
                    return true;
                }
                case JumpsQuery.TYPE: {
                    ViewHolder holder = getViewHolder(view);
                    String jumpType = cursor.getString(JumpsQuery.TYPE);
                    if (!TextUtils.isEmpty(jumpType)) {
                        holder.wayType.append(" " + jumpType);
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
                holder.wayType = (TextView) view.findViewById(R.id.list_item_jump_way_type);
                holder.description = (TextView) view.findViewById(R.id.list_item_jump_description);
                view.setTag(holder);
            }
            return holder;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = mActivity.getLayoutInflater().inflate(R.layout.list_subheader_jump, parent, false);
                holder.date = (TextView) convertView.findViewById(R.id.list_subheader_jump);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            holder.date.setText(TimeUtils.getTimeAgo(convertView.getContext(), ((Cursor) getItem(position)).getLong(JumpsQuery.DATE)));
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return TimeUtils.getTimeAgo(mActivity, ((Cursor) getItem(position)).getLong(JumpsQuery.DATE)).hashCode();
        }

        static class ViewHolder {
            TextView wayType;
            TextView description;
        }

        static class HeaderViewHolder {
            TextView date;
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
