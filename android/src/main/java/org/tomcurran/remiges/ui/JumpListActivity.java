package org.tomcurran.remiges.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


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
 * {@link org.tomcurran.remiges.ui.JumpListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class JumpListActivity extends BaseActivity
        implements JumpListFragment.Callbacks, JumpDetailFragment.Callbacks, JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpListActivity.class);

    private static final int ACTIVITY_INSERT = 0;
    private static final int ACTIVITY_VIEW = 1;
    private static final int ACTIVITY_EDIT = 2;

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
        final String action = intent.getAction();
        final Uri uri = intent.getData();
        if (uri != null) {
            String uriType = getContentResolver().getType(uri);
            if (uri.equals(RemigesContract.Jumps.CONTENT_URI)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    // normal activity behaviour is to view jumps
                } else if (action.equals(Intent.ACTION_INSERT)) {
                    insertJump();
                } else {
                    unknownAction(action);
                    return;
                }
            } else if (uriType.equals(RemigesContract.Jumps.CONTENT_ITEM_TYPE)) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    viewJump(uri);
                } else if (action.equals(Intent.ACTION_EDIT)) {
                    editJump(uri);
                } else {
                    unknownAction(action);
                    return;
                }
            }
        }
    }

    private void unknownAction(String action) {
        LOGE(TAG, String.format("Unknown action (%s). Exiting", action));
        setResult(FragmentActivity.RESULT_CANCELED);
        finish();
    }

    /**
     * Callback method from {@link org.tomcurran.remiges.ui.JumpListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onJumpSelected(Uri uri) {
        viewJump(uri);
    }

    @Override
    public void onInsertJump() {
        insertJump();
    }

    @Override
    public void onEditJump(Uri uri) {
        editJump(uri);
    }

    @Override
    public void onDeleteJump(Uri uri) {
        deleteJump();
    }

    @Override
    public void onJumpEdited(Uri uri) {
    }

    private void viewJump(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        if (mTwoPane) {
            JumpDetailFragment fragment = new JumpDetailFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.jump_detail_container, fragment)
                    .commit();
        } else {
            intent.setClass(this, JumpDetailActivity.class);
            startActivityForResult(intent, ACTIVITY_VIEW);
        }
    }

    private void editJump(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_EDIT);
        if (mTwoPane) {
            JumpEditFragment fragment = new JumpEditFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.jump_detail_container, fragment)
                    .commit();
        } else {
            intent.setClass(this, JumpEditActivity.class);
            startActivityForResult(intent, ACTIVITY_EDIT);
        }
    }

    private void insertJump() {
        Intent intent = new Intent();
        intent.setData(RemigesContract.Jumps.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        if (mTwoPane) {
            JumpEditFragment fragment = new JumpEditFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(intent));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.jump_detail_container, fragment)
                    .commit();
        } else {
            intent.setClass(this, JumpEditActivity.class);
            startActivityForResult(intent, ACTIVITY_INSERT);
        }
    }

    private void deleteJump() {
        if (mTwoPane) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.jump_detail_container);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

}
