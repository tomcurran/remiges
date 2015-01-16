package org.tomcurran.remiges.ui;

import android.net.Uri;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.singlepane.PlaceDetailActivity;
import org.tomcurran.remiges.ui.singlepane.PlaceEditActivity;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class PlaceFragment extends ItemFragment implements
        PlaceListFragment.Callbacks, PlaceDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(PlaceFragment.class);

    @Override
    protected String getContentType() {
        return RemigesContract.Places.CONTENT_TYPE;
    }

    @Override
    protected String getContentItemType() {
        return RemigesContract.Places.CONTENT_ITEM_TYPE;
    }

    @Override
    protected Uri getContentUri() {
        return RemigesContract.Places.CONTENT_URI;
    }

    @Override
    protected Fragment getListFragment() {
        return new PlaceListFragment();
    }

    @Override
    protected Fragment getDetailFragment() {
        return new PlaceDetailFragment();
    }

    @Override
    protected Class<?> getDetailActivity() {
        return PlaceDetailActivity.class;
    }

    @Override
    protected Class<?> getEditActivity() {
        return PlaceEditActivity.class;
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
    public void onEditPlace(Uri uri) {
        editItem(uri);
    }

    @Override
    public void onDeletePlace(Uri uri) {
        deleteItem();
    }

}
