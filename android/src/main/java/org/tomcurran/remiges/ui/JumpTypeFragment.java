package org.tomcurran.remiges.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeFragment extends Fragment implements JumpTypeListFragment.Callbacks {
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
    public void onJumpTypeSelected(Uri uri) {
        viewJumpType(uri);
    }

    @Override
    public void onInsertJumpType() {
        insertJumpType();
    }

    private void viewJumpType(Uri uri) {
        Toast.makeText(getActivity(), String.format("viewJumpType(%s)", uri), Toast.LENGTH_SHORT).show();
    }

    private void editJumpType(Uri uri) {
        Toast.makeText(getActivity(), String.format("editJumpType(%s)", uri), Toast.LENGTH_SHORT).show();
    }

    private void insertJumpType() {
        Toast.makeText(getActivity(), "insertJumpType()", Toast.LENGTH_SHORT).show();
    }

    private void deleteJumpType() {
        Toast.makeText(getActivity(), "deleteJumpType()", Toast.LENGTH_SHORT).show();
    }

}
