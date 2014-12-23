package org.tomcurran.remiges.ui.singlepane;

import android.support.v4.app.Fragment;

import org.tomcurran.remiges.ui.JumpTypeEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeEditActivity extends EditItemActivity {
    private static final String TAG = makeLogTag(JumpTypeEditActivity.class);

    @Override
    protected Fragment onCreatePane() {
        return new JumpTypeEditFragment();
    }

}
