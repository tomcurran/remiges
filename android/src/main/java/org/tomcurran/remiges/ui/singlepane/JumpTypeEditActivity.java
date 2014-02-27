package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.ui.JumpTypeEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeEditActivity extends SimpleSinglePaneActivity implements JumpTypeEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeEditActivity.class);

    @Override
    protected Fragment onCreatePane() {
        return new JumpTypeEditFragment();
    }

    @Override
    public void onJumpTypeEdited(Uri uri) {
    }

    @Override
    public void onDeleteJumpType(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

}
