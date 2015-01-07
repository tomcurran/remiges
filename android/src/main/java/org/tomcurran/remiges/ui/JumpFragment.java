package org.tomcurran.remiges.ui;

import android.net.Uri;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.JumpDetailActivity;
import org.tomcurran.remiges.ui.singlepane.JumpEditActivity;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpFragment extends ItemFragment implements
        JumpListFragment.Callbacks, JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpFragment.class);

    @Override
    protected String getContentType() {
        return RemigesContract.Jumps.CONTENT_TYPE;
    }

    @Override
    protected String getContentItemType() {
        return RemigesContract.Jumps.CONTENT_ITEM_TYPE;
    }

    @Override
    protected Uri getContentUri() {
        return RemigesContract.Jumps.CONTENT_URI;
    }

    @Override
    protected Fragment getListFragment() {
        return new JumpListFragment();
    }

    @Override
    protected Fragment getDetailFragment() {
        return new JumpDetailFragment();
    }

    @Override
    protected Class<?> getDetailActivity() {
        return JumpDetailActivity.class;
    }

    @Override
    protected Class<?> getEditActivity() {
        return JumpEditActivity.class;
    }

    @Override
    public void onJumpSelected(Uri uri) {
        viewItem(uri);
    }

    @Override
    public void onInsertJump() {
        insertItem(null);
    }

    @Override
    public void onEditJump(Uri uri) {
        editItem(uri);
    }

    @Override
    public void onDeleteJump(Uri uri) {
        deleteItem();
    }

}