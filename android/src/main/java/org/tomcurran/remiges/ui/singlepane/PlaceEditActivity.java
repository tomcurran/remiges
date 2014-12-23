package org.tomcurran.remiges.ui.singlepane;

import android.support.v4.app.Fragment;

import org.tomcurran.remiges.ui.PlaceEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class PlaceEditActivity extends EditItemActivity {
    private static final String TAG = makeLogTag(PlaceEditFragment.class);

    @Override
    protected Fragment onCreatePane() {
        return new PlaceEditFragment();
    }

}
