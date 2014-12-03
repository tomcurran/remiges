package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.ui.JumpDetailFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpDetailActivity extends SimpleSinglePaneActivity implements JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpDetailActivity.class);

    private static final int ACTIVITY_EDIT = 0;

    @Override
    protected Fragment onCreatePane() {
        return new JumpDetailFragment();
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
                    onDeleteJump(data.getData());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onEditJump(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(this, JumpEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    @Override
    public void onDeleteJump(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

}
