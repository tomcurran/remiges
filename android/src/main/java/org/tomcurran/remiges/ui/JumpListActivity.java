package org.tomcurran.remiges.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.LOGE;


/**
 * An activity representing a list of Jumps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link JumpDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link JumpListFragment} and the item details
 * (if present) is a {@link JumpDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link JumpListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class JumpListActivity extends BaseActivity
        implements JumpListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump_list);

        if (findViewById(R.id.jump_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((JumpListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.jump_list))
                    .setActivateOnItemClick(true);
        }

        final Intent intent = getIntent();
        final Uri uri = intent.getData();
        if (uri != null) {
            String uriType = getContentResolver().getType(uri);
            if (uriType.equals(RemigesContract.Jumps.CONTENT_ITEM_TYPE)) {
                onItemSelected(uri);
            }
        }
    }

    /**
     * Callback method from {@link JumpListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Uri uri) {
        Intent detailIntent = new Intent();
        detailIntent.setData(uri);
        if (mTwoPane) {
            JumpDetailFragment fragment = new JumpDetailFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(detailIntent));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.jump_detail_container, fragment)
                    .commit();
        } else {
            detailIntent.setClass(this, JumpDetailActivity.class);
            startActivity(detailIntent);
        }
    }

}
