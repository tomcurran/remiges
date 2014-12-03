package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.JumpTypeDetailFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpTypeDetailActivity extends SimpleSinglePaneActivity implements JumpTypeDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeDetailActivity.class);

    private static final int ACTIVITY_EDIT = 0;

    @Override
    protected Fragment onCreatePane() {
        return new JumpTypeDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_EDIT) {
            if (resultCode == RESULT_OK) {
                if (data.getAction().equals(Intent.ACTION_DELETE)) {
                    onDeleteJumpType(RemigesContract.JumpTypes.getJumpTypeId(data.getData()));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onEditJumpType(String jumpTypeId) {
        Intent intent = new Intent();
        intent.setData(RemigesContract.JumpTypes.buildJumpTypeUri(jumpTypeId));
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(this, JumpTypeEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    @Override
    public void onDeleteJumpType(String jumpTypeId) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, RemigesContract.JumpTypes.buildJumpTypeUri(jumpTypeId)));
        finish();
    }

}
