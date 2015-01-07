package org.tomcurran.remiges.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tomcurran.remiges.R;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public abstract class ItemFragment extends Fragment {
    private static final String TAG = makeLogTag(ItemFragment.class);

    protected abstract String getContentType();
    protected abstract String getContentItemType();
    protected abstract Uri getContentUri();
    protected abstract Fragment getListFragment();
    protected abstract Fragment getDetailFragment();
    protected abstract Class<?> getDetailActivity();
    protected abstract Class<?> getEditActivity();

    private static final int ACTIVITY_INSERT = 0;
    private static final int ACTIVITY_VIEW = 1;
    private static final int ACTIVITY_EDIT = 2;

    private static final String FRAGMENT_ITEM_LIST = "fragment_tag_item_list";

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

        FragmentActivity activity = getActivity();

        final Intent intent = activity.getIntent();
        final String action = intent.getAction();
        final Uri uri = intent.getData();
        if (uri != null) {
            String uriType = activity.getContentResolver().getType(uri);
            if (getContentType().equals(uriType)) {
                if (Intent.ACTION_INSERT.equals(action)) {
                    insertItem(intent.getExtras());
                } else if (!Intent.ACTION_VIEW.equals(action)) {
                    unknownAction(action);
                }
            } else if (getContentItemType().equals(uriType)) {
                if (Intent.ACTION_VIEW.equals(action)) {
                    viewItem(uri);
                    handledSetListSelection(uri);
                } else if (Intent.ACTION_EDIT.equals(action)) {
                    editItem(uri);
                    handledSetListSelection(uri);
                    if (mTwoPane) {
                        handledSetDetailFragment(intent);
                    }
                } else {
                    unknownAction(action);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        Fragment listFragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_ITEM_LIST);
        if (listFragment == null) {
            listFragment = getListFragment();
            if (!(listFragment instanceof ItemListFragment)) {
                throw new IllegalStateException(String.format("List fragment must implement %s.", ItemListFragment.class.getSimpleName()));
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.item_list_container, listFragment, FRAGMENT_ITEM_LIST)
                    .commit();
        }

        return view;
    }

    private void unknownAction(String action) {
        LOGE(TAG, String.format("Unknown action: %s", action));
        FragmentActivity activity = getActivity();
        activity.setResult(FragmentActivity.RESULT_CANCELED);
        activity.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_EDIT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    handledSetListSelection(data.getData());
                }
                break;
            case ACTIVITY_INSERT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    if (mTwoPane) {
                        handledSetDetailFragment(data);
                    }
                    handledSetListSelection(data.getData());
                }
                break;
        }
    }

    protected void viewItem(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        if (mTwoPane) {
            setDetailFragment(intent);
        } else {
            intent.setClass(getActivity(), getDetailActivity());
            startActivityForResult(intent, ACTIVITY_VIEW);
        }
    }

    protected void editItem(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(getActivity(), getEditActivity());
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    protected void insertItem(Bundle extras) {
        Intent intent = new Intent();
        intent.setData(getContentUri());
        intent.setAction(Intent.ACTION_INSERT);
        intent.setClass(getActivity(), getEditActivity());
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivityForResult(intent, ACTIVITY_INSERT);
    }

    protected void deleteItem() {
        if (mTwoPane) {
            FragmentManager fragmentManager = getChildFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.item_detail_container);
            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

    protected void setDetailFragment(Intent intent) {
        Fragment fragment = getDetailFragment();
        fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
        getChildFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, fragment)
                .commit();
    }

    public interface ItemListFragment {
        public void setSelectedItem(Uri uri);
    }

    protected void setListSelection(Uri uri) {
        ((ItemListFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_ITEM_LIST))
                .setSelectedItem(uri);
    }

    protected void handledSetListSelection(final Uri uri) {
        new Handler().post(new Runnable() {
            public void run() {
                setListSelection(uri);
            }
        });
    }

    protected void handledSetDetailFragment(final Intent intent) {
        new Handler().post(new Runnable() {
            public void run() {
                setDetailFragment(intent);
            }
        });
    }

}
