package org.tomcurran.remiges.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.PlaceDetailActivity;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place, container, false);

        Fragment placeListFragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_PLACE_LIST);
        if (placeListFragment == null) {
            placeListFragment = new PlaceListFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.place_list_container, placeListFragment, FRAGMENT_PLACE_LIST)
                    .commit();
        }

        if (view.findViewById(R.id.place_detail_container) != null) {
            mTwoPane = true;
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();

        final Intent intent = activity.getIntent();
        final String action = intent.getAction();
        final Uri uri = intent.getData();
        LOGD(TAG, String.format("uri=%s action=%s", uri, action));
        if (uri != null) {
            String uriType = activity.getContentResolver().getType(uri);
            if (action == null) {
                unknownAction(action);
                return;
            }
            if (uri.equals(RemigesContract.Places.CONTENT_URI)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    // normal activity behaviour is to view jumps
                } else if (action.equals(Intent.ACTION_INSERT)) {
                    insertPlace();
                } else {
                    unknownAction(action);
                    return;
                }
            } else if (uriType.equals(RemigesContract.Places.CONTENT_ITEM_TYPE)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    viewPlace(uri);
                } else if (action.equals(Intent.ACTION_EDIT)) {
                    editPlace(uri);
                } else {
                    unknownAction(action);
                    return;
                }
            }
        }
    }

    private void unknownAction(String action) {
        LOGE(TAG, String.format("Unknown action (%s). Exiting", action));
        FragmentActivity activity = getActivity();
        activity.setResult(FragmentActivity.RESULT_CANCELED);
        activity.finish();
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
            PlaceDetailFragment fragment = new PlaceDetailFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.place_detail_container, fragment)
                    .commit();
        } else {
            intent.setClass(getActivity(), PlaceDetailActivity.class);
            startActivityForResult(intent, ACTIVITY_VIEW);
        }
    }

    private void editPlace(Uri uri) {
        Toast.makeText(getActivity(), String.format("editPlace(%s)", uri), Toast.LENGTH_SHORT).show();
    }

    private void insertPlace() {
        Toast.makeText(getActivity(), "insertPlace()", Toast.LENGTH_SHORT).show();
    }

    private void deletePlace() {
        Toast.makeText(getActivity(), "deletePlace()", Toast.LENGTH_SHORT).show();
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

}
