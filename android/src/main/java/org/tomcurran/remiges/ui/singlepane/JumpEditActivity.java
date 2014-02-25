package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.ui.JumpEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpEditActivity extends SimpleSinglePaneActivity implements JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpEditActivity.class);

    @Override
    protected Fragment onCreatePane() {
        return new JumpEditFragment();
    }

    @Override
    public void onJumpEdited(Uri uri) {
    }

    @Override
    public void onDeleteJump(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

}
