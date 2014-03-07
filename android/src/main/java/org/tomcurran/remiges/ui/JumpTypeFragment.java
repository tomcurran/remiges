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

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.JumpTypeDetailActivity;
import org.tomcurran.remiges.ui.singlepane.JumpTypeEditActivity;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeFragment extends Fragment implements
        JumpTypeListFragment.Callbacks, JumpTypeDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeFragment.class);

    private static final int ACTIVITY_INSERT = 0;
    private static final int ACTIVITY_VIEW = 1;
    private static final int ACTIVITY_EDIT = 2;

    private static final String FRAGMENT_JUMPTYPE_LIST = "fragment_tag_jumptype_list";

    private boolean mTwoPane;

    public JumpTypeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jumptype, container, false);

        if (getChildFragmentManager().findFragmentByTag(FRAGMENT_JUMPTYPE_LIST) == null) {
            JumpTypeListFragment jumpTypeListFragment = new JumpTypeListFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jumptype_list_container, jumpTypeListFragment, FRAGMENT_JUMPTYPE_LIST)
                    .commit();
        }

        if (view.findViewById(R.id.jumptype_detail_container) != null) {
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
            if (uri.equals(RemigesContract.JumpTypes.CONTENT_URI)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    // normal activity behaviour is to view jump type
                } else if (action.equals(Intent.ACTION_INSERT)) {
                    insertJumpType();
                } else {
                    unknownAction(action);
                    return;
                }
            } else if (uriType.equals(RemigesContract.JumpTypes.CONTENT_ITEM_TYPE)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    viewJumpType(uri);
                } else if (action.equals(Intent.ACTION_EDIT)) {
                    editJumpType(uri);
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
    public void onJumpTypeSelected(String jumpTypeId) {
        viewJumpType(RemigesContract.JumpTypes.buildJumpTypeUri(jumpTypeId));
    }

    @Override
    public void onInsertJumpType() {
        insertJumpType();
    }

    @Override
    public void onEditJumpType(String jumpTypeId) {
        editJumpType(RemigesContract.JumpTypes.buildJumpTypeUri(jumpTypeId));
    }

    @Override
    public void onDeleteJumpType(String jumpTypeId) {
        deleteJumpType();
    }

    private void viewJumpType(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        if (mTwoPane) {
            JumpTypeDetailFragment fragment = new JumpTypeDetailFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jumptype_detail_container, fragment)
                    .commit();
        } else {
            intent.setClass(getActivity(), JumpTypeDetailActivity.class);
            startActivityForResult(intent, ACTIVITY_EDIT);
        }
    }

    private void editJumpType(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(getActivity(), JumpTypeEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    private void insertJumpType() {
        Intent intent = new Intent();
        intent.setData(RemigesContract.JumpTypes.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setClass(getActivity(), JumpTypeEditActivity.class);
        startActivityForResult(intent, ACTIVITY_INSERT);
    }

    private void deleteJumpType() {
        if (mTwoPane) {
            FragmentManager fragmentManager = getChildFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.jumptype_detail_container);
            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

}
