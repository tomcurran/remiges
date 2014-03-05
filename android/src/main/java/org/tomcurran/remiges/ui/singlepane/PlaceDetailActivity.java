package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.Toast;

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
        Toast.makeText(this, String.format("editPlace(%s)", uri), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePlace(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

}
