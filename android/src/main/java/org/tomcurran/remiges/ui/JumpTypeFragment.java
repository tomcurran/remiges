package org.tomcurran.remiges.ui;


import android.net.Uri;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.JumpTypeDetailActivity;
import org.tomcurran.remiges.ui.singlepane.JumpTypeEditActivity;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeFragment extends ItemFragment implements
        JumpTypeListFragment.Callbacks, JumpTypeDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpTypeFragment.class);

    @Override
    protected String getContentType() {
        return RemigesContract.JumpTypes.CONTENT_TYPE;
    }

    @Override
    protected String getContentItemType() {
        return RemigesContract.JumpTypes.CONTENT_ITEM_TYPE;
    }

    @Override
    protected Uri getContentUri() {
        return RemigesContract.JumpTypes.CONTENT_URI;
    }

    @Override
    protected Fragment getListFragment() {
        return new JumpTypeListFragment();
    }

    @Override
    protected Fragment getDetailFragment() {
        return new JumpTypeDetailFragment();
    }

    @Override
    protected Class<?> getDetailActivity() {
        return JumpTypeDetailActivity.class;
    }

    @Override
    protected Class<?> getEditActivity() {
        return JumpTypeEditActivity.class;
    }

    @Override
    public void onItemSelected(Uri uri) {
        viewItem(uri);
    }

    @Override
    public void onInsertItem() {
        insertItem(null);
    }

    @Override
    public void onEditJumpType(String jumpTypeId) {
        editItem(RemigesContract.JumpTypes.buildJumpTypeUri(jumpTypeId));
    }

    @Override
    public void onDeleteJumpType(String jumpTypeId) {
        deleteItem();
    }

}
