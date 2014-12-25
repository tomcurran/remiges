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
            if (uri.equals(RemigesContract.JumpTypes.CONTENT_URI)) {
                // default behaviour is to view jump types, we do not check for Intent.ACTION_VIEW
                if (action.equals(Intent.ACTION_INSERT)) {
                    insertJumpType();
                } else {
                    unknownAction();
                }
            } else if (uriType.equals(RemigesContract.JumpTypes.CONTENT_ITEM_TYPE)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    viewJumpType(uri);
                } else if (action.equals(Intent.ACTION_EDIT)) {
                    editJumpType(uri);
                } else {
                    unknownAction();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jumptype, container, false);

        Fragment jumpTypeListFragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_JUMPTYPE_LIST);
        if (jumpTypeListFragment == null) {
            jumpTypeListFragment = new JumpTypeListFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jumptype_list_container, jumpTypeListFragment, FRAGMENT_JUMPTYPE_LIST)
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
            case ACTIVITY_EDIT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    new Handler().post(new Runnable() {
                        public void run() {
                            setListSelection(data.getData());
                        }
                    });
                }
                break;
            case ACTIVITY_INSERT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    new Handler().post(new Runnable() {
                        public void run() {
                            if (mTwoPane) {
                                setDetailFragment(data);
                            }
                            setListSelection(data.getData());
                        }
                    });
                }
                break;
        }
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
            setDetailFragment(intent);
        } else {
            intent.setClass(getActivity(), JumpTypeDetailActivity.class);
            startActivityForResult(intent, ACTIVITY_VIEW);
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
        intent.putExtras(getActivity().getIntent());
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

    private void setDetailFragment(Intent intent) {
        JumpTypeDetailFragment fragment = new JumpTypeDetailFragment();
        fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
        getChildFragmentManager().beginTransaction()
                .replace(R.id.jumptype_detail_container, fragment)
                .commit();
    }

    private void setListSelection(Uri uri) {
        ((JumpTypeListFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_JUMPTYPE_LIST))
                .setSelectedJumpType(uri);
    }

}
