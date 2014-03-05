package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.JumpTypeEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeEditActivity extends SimpleSinglePaneActivity implements JumpTypeEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeEditActivity.class);

    @Override
    protected Fragment onCreatePane() {
        return new JumpTypeEditFragment();
    }

    @Override
    public void onJumpTypeEdited(String jumpTypeId) {
    }

    @Override
    public void onDeleteJumpType(String jumpTypeId) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, RemigesContract.JumpTypes.buildJumpTypeUri(jumpTypeId)));
        finish();
    }

}
