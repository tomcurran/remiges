package org.tomcurran.remiges.ui;


import android.app.ActionBar;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.tomcurran.remiges.BuildConfig;
import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.TestData;

import java.text.ParseException;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.Callbacks,
        JumpListFragment.Callbacks, JumpDetailFragment.Callbacks, JumpEditFragment.Callbacks,
        JumpTypeListFragment.Callbacks {
    private static final String TAG = makeLogTag(MainActivity.class);

    private static final String FRAGMENT_JUMPS = "fragment_tag_jumps";
    private static final String FRAGMENT_JUMPTYPES = "fragment_tag_jumptypes";

    private static final int SECTION_JUMPS = 0;
    private static final int SECTION_JUMPTYPES = 1;

    private TestData mTestData;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    private String[] mSectionTitles;
    private int mSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSectionTitles = getResources().getStringArray(R.array.navigation_drawer_titles);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        int section = SECTION_JUMPS;
        Uri uri = getIntent().getData();
        if (uri != null) {
            String uriType = getContentResolver().getType(uri);
            if (uriType != null) {
                if (uriType.equals(RemigesContract.Jumps.CONTENT_TYPE) || uriType.equals(RemigesContract.Jumps.CONTENT_ITEM_TYPE)) {
                    section = SECTION_JUMPS;
                } else if (uriType.equals(RemigesContract.JumpTypes.CONTENT_TYPE) || uriType.equals(RemigesContract.JumpTypes.CONTENT_ITEM_TYPE)) {
                    section = SECTION_JUMPTYPES;
                }
            }
        }

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), section);

        mTestData = new TestData(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.container);
        switch (position) {
            case SECTION_JUMPS: {
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .detach(fragment)
                            .commit();
                }

                fragment = fragmentManager.findFragmentByTag(FRAGMENT_JUMPS);
                if (fragment == null) {
                    fragment = new JumpFragment();
                    fragment.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
                    fragmentManager.beginTransaction()
                            .add(R.id.container, fragment, FRAGMENT_JUMPS)
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .attach(fragment)
                            .commit();
                }

                mTitle = mSectionTitles[SECTION_JUMPS];
                mSection = SECTION_JUMPS;
                break;
            }
            case SECTION_JUMPTYPES: {
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .detach(fragment)
                            .commit();
                }

                fragment = fragmentManager.findFragmentByTag(FRAGMENT_JUMPTYPES);
                if (fragment == null) {
                    fragment = new JumpTypeFragment();
                    fragment.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
                    fragmentManager.beginTransaction()
                            .add(R.id.container, fragment, FRAGMENT_JUMPTYPES)
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .attach(fragment)
                            .commit();
                }

                mTitle = mSectionTitles[SECTION_JUMPTYPES];
                mSection = SECTION_JUMPTYPES;
                break;
            }
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            if (BuildConfig.DEBUG) {
                getMenuInflater().inflate(R.menu.debug, menu);
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_debug_insert_data:
                try {
                    mTestData.insert();
                } catch (JSONException e) {
                    LOGE(TAG, String.format("Test data JSON error: %s", e.getMessage()));
                } catch (ParseException e) {
                    LOGE(TAG, String.format("Test data JSON parse error: %s", e.getMessage()));
                } catch (RemoteException e) {
                    LOGE(TAG, String.format("Test data provider communication error: %s", e.getMessage()));
                } catch (OperationApplicationException e) {
                    LOGE(TAG, String.format("Test data insertion error: %s", e.getMessage()));
                }
                return true;
            case R.id.menu_debug_delete_data:
                try {
                    mTestData.delete();
                } catch (RemoteException e) {
                    LOGE(TAG, String.format("Test data provider communication error: %s", e.getMessage()));
                } catch (OperationApplicationException e) {
                    LOGE(TAG, String.format("Test data deletion error: %s", e.getMessage()));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // pass call backs to containing fragment

    @Override
    public void onJumpSelected(Uri uri) {
        LOGD(TAG, String.format("onJumpSelected(%s)", uri));
        ((JumpFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onJumpSelected(uri);
    }

    @Override
    public void onInsertJump() {
        LOGD(TAG, "onInsertJump()");
        ((JumpFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onInsertJump();
    }

    @Override
    public void onEditJump(Uri uri) {
        LOGD(TAG, String.format("onEditJump(%s)", uri));
        ((JumpFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onEditJump(uri);
    }

    @Override
    public void onJumpEdited(Uri uri) {
        LOGD(TAG, String.format("onJumpEdited(%s)", uri));
        ((JumpFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onJumpEdited(uri);
    }

    @Override
    public void onDeleteJump(Uri uri) {
        LOGD(TAG, String.format("onDeleteJump(%s)", uri));
        ((JumpFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onDeleteJump(uri);
    }

    @Override
    public void onJumpTypeSelected(Uri uri) {
        LOGD(TAG, String.format("onJumpTypeSelected(%s)", uri));
        ((JumpTypeFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onJumpTypeSelected(uri);
    }

    @Override
    public void onInsertJumpType() {
        LOGD(TAG, "onInsertJumpType()");
        ((JumpTypeFragment)getSupportFragmentManager().findFragmentById(R.id.container)).onInsertJumpType();
    }

}
