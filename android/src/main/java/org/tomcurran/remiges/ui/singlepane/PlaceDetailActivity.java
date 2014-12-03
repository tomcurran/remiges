package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.tomcurran.remiges.ui.PlaceDetailFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class PlaceDetailActivity extends SimpleSinglePaneActivity implements PlaceDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(PlaceDetailActivity.class);

    private static final int ACTIVITY_EDIT = 0;

    @Override
    protected Fragment onCreatePane() {
        return new PlaceDetailFragment();
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
                    onDeletePlace(data.getData());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onEditPlace(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setClass(this, PlaceEditActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    @Override
    public void onDeletePlace(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

}
