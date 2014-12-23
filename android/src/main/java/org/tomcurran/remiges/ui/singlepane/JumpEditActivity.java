package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.JumpEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpEditActivity extends EditItemActivity implements JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpEditActivity.class);

    private static final int ACTIVITY_PLACE = 0;
    private static final int ACTIVITY_JUMPTYPE = 1;

    @Override
    protected Fragment onCreateEditPane() {
        return new JumpEditFragment();
    }

    @Override
    public void onAddPlace() {
        Intent intent = new Intent();
        intent.setData(RemigesContract.Places.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setClass(this, PlaceEditActivity.class);
        startActivityForResult(intent, ACTIVITY_PLACE);
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
                    ((JumpEditFragment) getFragment()).setJumpType(
                            RemigesContract.JumpTypes.getJumpTypeId(data.getData()));
                }
                break;
            case ACTIVITY_PLACE:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    ((JumpEditFragment) getFragment()).setPlace(
                            RemigesContract.Places.getPlaceId(data.getData()));
                }
                break;
        }
    }


}
