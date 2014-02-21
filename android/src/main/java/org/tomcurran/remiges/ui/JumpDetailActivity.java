package org.tomcurran.remiges.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.tomcurran.remiges.R;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

/**
 * An activity representing a single Jump detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link JumpListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link JumpDetailFragment}.
 */
public class JumpDetailActivity extends BaseActivity implements JumpDetailFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpEditActivity.class);

    private static final int ACTIVITY_EDIT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            JumpDetailFragment fragment = new JumpDetailFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.jump_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, JumpListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
