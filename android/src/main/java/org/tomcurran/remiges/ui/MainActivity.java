package org.tomcurran.remiges.ui;


import android.app.ActionBar;
import android.content.OperationApplicationException;
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
import org.tomcurran.remiges.util.TestData;

import java.text.ParseException;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.Callbacks {
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
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        mTestData = new TestData(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        try {
            switch (position) {
                case SECTION_JUMPS:
                    attachFragment(JumpFragment.class, FRAGMENT_JUMPS);
                    mTitle = mSectionTitles[SECTION_JUMPS];
                    mSection = SECTION_JUMPS;
                    break;
                case SECTION_JUMPTYPES:
                    attachFragment(JumpTypeFragment.class, FRAGMENT_JUMPTYPES);
                    mTitle = mSectionTitles[SECTION_JUMPTYPES];
                    mSection = SECTION_JUMPTYPES;
                    break;
            }
        } catch (IllegalAccessException e) {
            LOGE(TAG, String.format("Fragment field or method not accessible: %s", e.getMessage()));
        } catch (InstantiationException e) {
            LOGE(TAG, String.format("Fragment constructor not accessible: %s", e.getMessage()));
        }
    }

    private void attachFragment(Class<? extends Fragment> clazz, String tag) throws IllegalAccessException, InstantiationException {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.container);
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .detach(fragment)
                    .commit();
        }

        fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = clazz.newInstance();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .attach(fragment)
                    .commit();
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

    // API workaround. Issue https://code.google.com/p/android/issues/detail?id=40323
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null && fragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
            fragment.getChildFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
