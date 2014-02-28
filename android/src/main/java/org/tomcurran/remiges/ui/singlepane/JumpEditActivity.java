package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.JumpEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpEditActivity extends SimpleSinglePaneActivity implements JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpEditActivity.class);

    private static final int ACTIVITY_JUMPTYPE = 0;

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
        if (requestCode == ACTIVITY_JUMPTYPE) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                if (data.getAction().equals(Intent.ACTION_INSERT)) {
                    JumpEditFragment fragment = (JumpEditFragment) getFragment();
                    fragment.setJumpType(data.getData());
                }
            }
        }
    }
}
