package org.tomcurran.remiges.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = makeLogTag(MainActivity.class);

    private static final String SAVE_STATE_NAVIGATION = "navigation_view_selection";

    private static final String FRAGMENT_JUMPS = "fragment_tag_jumps";
    private static final String FRAGMENT_PLACES = "fragment_tag_places";
    private static final String FRAGMENT_JUMPTYPES = "fragment_tag_jumptypes";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private int mNavigationSelection = R.id.navigation_drawer_jumps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_navigation_drawer);
            toolbar.setNavigationContentDescription(R.string.navigation_drawer_open);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View navigationHeader = mNavigationView.findViewById(R.id.navigation_drawer_header);
            int statusBarHeight = UIUtils.getStatusBarHeight(getResources());
            navigationHeader.setPaddingRelative(
                    navigationHeader.getPaddingStart(),
                    navigationHeader.getPaddingTop() + statusBarHeight,
                    navigationHeader.getPaddingRight(),
                    navigationHeader.getPaddingEnd());
        }

        if (savedInstanceState == null) {
            Uri uri = getIntent().getData();
            String uriType = getContentResolver().getType(uri != null ? uri : RemigesContract.Jumps.CONTENT_URI);
            if (uriType != null) {
                switch (uriType) {
                    case RemigesContract.Jumps.CONTENT_TYPE:
                    case RemigesContract.Jumps.CONTENT_ITEM_TYPE:
                        mNavigationSelection = R.id.navigation_drawer_jumps;
                        break;
                    case RemigesContract.Places.CONTENT_TYPE:
                    case RemigesContract.Places.CONTENT_ITEM_TYPE:
                        mNavigationSelection = R.id.navigation_drawer_places;
                        break;
                    case RemigesContract.JumpTypes.CONTENT_TYPE:
                    case RemigesContract.JumpTypes.CONTENT_ITEM_TYPE:
                        mNavigationSelection = R.id.navigation_drawer_jumptypes;
                        break;
                }
            }
        } else {
            mNavigationSelection = savedInstanceState.getInt(SAVE_STATE_NAVIGATION);
        }

        mNavigationView.setNavigationItemSelectedListener(this);
        navigate(mNavigationSelection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_NAVIGATION, mNavigationSelection);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        return navigate(menuItem.getItemId());
    }

    private boolean navigate(int menuItemId) {
        switch (menuItemId) {
            case R.id.navigation_drawer_jumps:
            case R.id.navigation_drawer_places:
            case R.id.navigation_drawer_jumptypes:
                navigateFragment(menuItemId);
                return true;
            case R.id.navigation_drawer_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                mDrawerLayout.closeDrawers();
                return true;
        }
        return false;
    }

    private void navigateFragment(int menuItemId) {
        try {
            switch (menuItemId) {
                case R.id.navigation_drawer_jumps:
                    setTitle(R.string.navigation_drawer_jumps);
                    attachFragment(JumpFragment.class, FRAGMENT_JUMPS);
                    break;
                case R.id.navigation_drawer_places:
                    setTitle(R.string.navigation_drawer_places);
                    attachFragment(PlaceFragment.class, FRAGMENT_PLACES);
                    break;
                case R.id.navigation_drawer_jumptypes:
                    setTitle(R.string.navigation_drawer_jumptypes);
                    attachFragment(JumpTypeFragment.class, FRAGMENT_JUMPTYPES);
                    break;
            }
            mNavigationView.getMenu().findItem(menuItemId).setChecked(true);
            mNavigationSelection = menuItemId;
            mDrawerLayout.closeDrawers();
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

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // API workaround. Issue https://code.google.com/p/android/issues/detail?id=40323
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment != null && fragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                fragment.getChildFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

}
