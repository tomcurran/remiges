package org.tomcurran.remiges.ui;


import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import org.tomcurran.remiges.ui.singlepane.EditItemActivity;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public abstract class ItemEditFragment extends Fragment implements EditItemActivity.Callbacks {
    private static final String TAG = makeLogTag(ItemEditFragment.class);

    protected static final int STATE_INSERT = 0;
    protected static final int STATE_EDIT = 1;

    protected static final String SAVE_STATE_ITEM_URI = "item_uri";
    protected static final String SAVE_STATE_ITEM_STATE = "item_state";

    protected int mState;
    protected Uri mItemUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            final String action = intent.getAction();
            if (Intent.ACTION_INSERT.equals(action)) {
                mState = STATE_INSERT;
                mItemUri = null;
            } else if (Intent.ACTION_EDIT.equals(action)) {
                mState = STATE_EDIT;
                mItemUri = intent.getData();
            } else {
                LOGE(TAG, "Unknown action");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
            }
        } else {
            mItemUri = savedInstanceState.getParcelable(SAVE_STATE_ITEM_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_ITEM_STATE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            ContentValues values = getDefaultValues();
            if (mState == STATE_INSERT) {
                Bundle extras = BaseActivity.fragmentArgumentsToIntent(getArguments()).getExtras();
                if (extras != null) {
                    values = passIntentValues(extras, values);
                }
            }
            setViewValues(values);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_ITEM_URI, mItemUri);
        outState.putInt(SAVE_STATE_ITEM_STATE, mState);
    }

    @Override
    public void onSaveItem() {
        switch (mState) {
            case STATE_INSERT:
                insertItem();
                break;
            case STATE_EDIT:
                updateItem();
                break;
        }
    }

    private void insertItem() {
        FragmentActivity activity = getActivity();
        Uri itemUri = activity.getContentResolver().insert(getContentUri(), getViewValues());
        if (itemUri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSERT);
            intent.setData(itemUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
    }

    private void updateItem() {
        FragmentActivity activity = getActivity();
        if (activity.getContentResolver().update(mItemUri, getViewValues(), null, null) > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(mItemUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
    }

    protected abstract Uri getContentUri();
    protected abstract ContentValues getDefaultValues();
    protected abstract ContentValues getViewValues();
    protected abstract void setViewValues(ContentValues values);
    protected abstract ContentValues passIntentValues(Bundle extras, ContentValues values);

}
