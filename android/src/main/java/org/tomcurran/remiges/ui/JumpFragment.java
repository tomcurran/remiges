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
import org.tomcurran.remiges.ui.singlepane.JumpDetailActivity;
import org.tomcurran.remiges.ui.singlepane.JumpEditActivity;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpFragment extends Fragment implements
        JumpListFragment.Callbacks, JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpFragment.class);

    private static final int ACTIVITY_INSERT = 0;
    private static final int ACTIVITY_VIEW = 1;
    private static final int ACTIVITY_EDIT = 2;

    private static final String FRAGMENT_JUMP_LIST = "fragment_tag_jump_list";

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
            if (uri.equals(RemigesContract.Jumps.CONTENT_URI)) {
                // default behaviour is to view jumps, we do not check for Intent.ACTION_VIEW
                if (action.equals(Intent.ACTION_INSERT)) {
                    insertJump();
                } else {
                    unknownAction();
                }
            } else if (uriType.equals(RemigesContract.Jumps.CONTENT_ITEM_TYPE)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    viewJump(uri);
                } else if (action.equals(Intent.ACTION_EDIT)) {
                    editJump(uri);
                } else {
                    unknownAction();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jump, container, false);

        Fragment jumpListFragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_JUMP_LIST);
        if (jumpListFragment == null) {
            jumpListFragment = new JumpListFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jump_list_container, jumpListFragment, FRAGMENT_JUMP_LIST)
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
    public void onJumpSelected(Uri uri) {
        viewJump(uri);
    }

    @Override
    public void onInsertJump() {
        insertJump();
    }

    @Override
    public void onEditJump(Uri uri) {
        editJump(uri);
    }

    @Override
    public void onDeleteJump(Uri uri) {
        deleteJump();
    }

    private void viewJump(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        if (mTwoPane) {
            JumpDetailFragment fragment = new JumpDetailFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jump_detail_container, fragment)
                    .commit();
        } else {
            intent.setClass(getActivity(), JumpDetailActivity.class);
            startActivityForResult(intent, ACTIVITY_VIEW);
        }
    }

    private void editJump(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(getActivity(), JumpEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    private void insertJump() {
        Intent intent = new Intent();
        intent.setData(RemigesContract.Jumps.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setClass(getActivity(), JumpEditActivity.class);
        startActivityForResult(intent, ACTIVITY_INSERT);
    }

    private void deleteJump() {
        if (mTwoPane) {
            FragmentManager fragmentManager = getChildFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.jump_detail_container);
            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

}
