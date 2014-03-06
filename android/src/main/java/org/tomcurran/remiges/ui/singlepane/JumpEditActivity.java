package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.widget.Toast;

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
    public void onJumpEdited(Uri uri) {
    }

    @Override
    public void onDeleteJump(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
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
        if (requestCode == ACTIVITY_JUMPTYPE) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                if (data.getAction().equals(Intent.ACTION_INSERT)) {
                    JumpEditFragment fragment = (JumpEditFragment) getFragment();
                    fragment.setJumpType(RemigesContract.JumpTypes.getJumpTypeId(data.getData()));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.setData(RemigesContract.Jumps.buildJumpUri(
                        ((JumpEditFragment) getFragment()).getJumpId()));
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
