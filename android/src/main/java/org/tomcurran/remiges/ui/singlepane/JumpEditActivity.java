package org.tomcurran.remiges.ui.singlepane;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.JumpEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpEditActivity extends SimpleSinglePaneActivity implements JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpEditActivity.class);

    private static final int ACTIVITY_PLACE = 0;
    private static final int ACTIVITY_JUMPTYPE = 1;

    @Override
    protected Fragment onCreatePane() {
        return new JumpEditFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.donebar_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        barDone();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        barCancel();
                    }
                });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onAddPlace() {
        Toast.makeText(this, "add place", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddJumpType() {
        Intent intent = new Intent();
        intent.setData(RemigesContract.JumpTypes.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setClass(this, JumpTypeEditActivity.class);
        startActivityForResult(intent, ACTIVITY_JUMPTYPE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_JUMPTYPE:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    if (data.getAction().equals(Intent.ACTION_INSERT)) {
                        ((JumpEditFragment) getFragment()).setJumpType(
                                RemigesContract.JumpTypes.getJumpTypeId(data.getData()));
                    }
                }
                break;
            case ACTIVITY_PLACE:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    if (data.getAction().equals(Intent.ACTION_INSERT)) {
                        ((JumpEditFragment) getFragment()).setPlace(
                                RemigesContract.Places.getPlaceId(data.getData()));
                    }
                }
                break;
        }
    }

    private void barDone() {
        Intent intent = getIntent();
        String jumpId = ((JumpEditFragment)getFragment()).barDone();
        intent.setData(RemigesContract.Jumps.buildJumpUri(jumpId));
        setResult(FragmentActivity.RESULT_OK, intent);
        finish();
    }

    private void barCancel() {
        ((JumpEditFragment)getFragment()).barCancel();
        setResult(FragmentActivity.RESULT_CANCELED, getIntent());
        finish();
    }

}
