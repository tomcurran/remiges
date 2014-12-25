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
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.PlaceDetailActivity;
import org.tomcurran.remiges.ui.singlepane.PlaceEditActivity;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class PlaceFragment extends Fragment implements
        PlaceListFragment.Callbacks, PlaceDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(PlaceFragment.class);

    private static final int ACTIVITY_INSERT = 0;
    private static final int ACTIVITY_VIEW = 1;
    private static final int ACTIVITY_EDIT = 2;

    private static final String FRAGMENT_PLACE_LIST = "fragment_tag_place_list";

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

        FragmentActivity activity = getActivity();

        final Intent intent = activity.getIntent();
        final String action = intent.getAction();
        final Uri uri = intent.getData();
        LOGD(TAG, String.format("uri=%s action=%s", uri, action));
        if (uri != null) {
            String uriType = activity.getContentResolver().getType(uri);
            if (action == null) {
                unknownAction();
                return;
            }
            if (uri.equals(RemigesContract.Places.CONTENT_URI)) {
                // default behaviour is to view places, we do not check for Intent.ACTION_VIEW
                if (action.equals(Intent.ACTION_INSERT)) {
                    insertPlace();
                } else {
                    unknownAction();
                }
            } else if (uriType.equals(RemigesContract.Places.CONTENT_ITEM_TYPE)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    viewPlace(uri);
                } else if (action.equals(Intent.ACTION_EDIT)) {
                    editPlace(uri);
                } else {
                    unknownAction();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place, container, false);

        Fragment placeListFragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_PLACE_LIST);
        if (placeListFragment == null) {
            placeListFragment = new PlaceListFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.place_list_container, placeListFragment, FRAGMENT_PLACE_LIST)
                    .commit();
        }

        return view;
    }

    private void unknownAction() {
        LOGE(TAG, "Unknown action. Exiting");
        FragmentActivity activity = getActivity();
        activity.setResult(FragmentActivity.RESULT_CANCELED);
        activity.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_INSERT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    new Handler().post(new Runnable() {
                        public void run() {
                            if (mTwoPane) {
                                setDetailFragment(data);
                            }
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void onPlaceSelected(Uri uri) {
        viewPlace(uri);
    }

    @Override
    public void onInsertPlace() {
        insertPlace();
    }

    @Override
    public void onEditPlace(Uri uri) {
        editPlace(uri);
    }

    @Override
    public void onDeletePlace(Uri uri) {
        deletePlace();
    }

    private void viewPlace(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        if (mTwoPane) {
            setDetailFragment(intent);
        } else {
            intent.setClass(getActivity(), PlaceDetailActivity.class);
            startActivityForResult(intent, ACTIVITY_VIEW);
        }
    }

    private void editPlace(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(getActivity(), PlaceEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    private void insertPlace() {
        Intent intent = new Intent();
        intent.setData(RemigesContract.Places.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setClass(getActivity(), PlaceEditActivity.class);
        intent.putExtras(getActivity().getIntent());
        startActivityForResult(intent, ACTIVITY_INSERT);
    }

    private void deletePlace() {
        if (mTwoPane) {
            FragmentManager fragmentManager = getChildFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.place_detail_container);
            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

    private void setDetailFragment(Intent intent) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
        getChildFragmentManager().beginTransaction()
                .replace(R.id.place_detail_container, fragment)
                .commit();
    }

}
