package org.tomcurran.remiges.ui;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;

import org.tomcurran.remiges.R;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.Callbacks {
    private static final String TAG = makeLogTag(MainActivity.class);

    private static final String FRAGMENT_JUMPS = "fragment_tag_jumps";
    private static final String FRAGMENT_PLACES = "fragment_tag_places";
    private static final String FRAGMENT_JUMPTYPES = "fragment_tag_jumptypes";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    private String[] mSectionTitles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mSectionTitles = getResources().getStringArray(R.array.navigation_drawer_titles);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        try {
            switch (position) {
                case NavigationDrawerFragment.SECTION_JUMPS:
                    attachFragment(JumpFragment.class, FRAGMENT_JUMPS);
                    mTitle = mSectionTitles[NavigationDrawerFragment.SECTION_JUMPS];
                    break;
                case NavigationDrawerFragment.SECTION_PLACES:
                    attachFragment(PlaceFragment.class, FRAGMENT_PLACES);
                    mTitle = mSectionTitles[NavigationDrawerFragment.SECTION_PLACES];
                    break;
                case NavigationDrawerFragment.SECTION_JUMPTYPES:
                    attachFragment(JumpTypeFragment.class, FRAGMENT_JUMPTYPES);
                    mTitle = mSectionTitles[NavigationDrawerFragment.SECTION_JUMPTYPES];
                    break;
                case NavigationDrawerFragment.SECTION_SETTINGS:
                    startActivity(new Intent(this, SettingsActivity.class));
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
