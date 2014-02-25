package org.tomcurran.remiges.ui;


import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.Callbacks,
        JumpListFragment.Callbacks, JumpDetailFragment.Callbacks, JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(MainActivity.class);

    private static final int SECTION_JUMPS = 0;
    private static final int SECTION_JUMPTYPES = 1;

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
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case SECTION_JUMPS:
                LOGD(TAG, "jumps");
                if (!(getSupportFragmentManager().findFragmentById(R.id.container) instanceof JumpFragment)) {
                    JumpFragment fragment = new JumpFragment();
                    fragment.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit();
                }
                mTitle = mSectionTitles[SECTION_JUMPS];
                mSection = SECTION_JUMPS;
                break;
            case SECTION_JUMPTYPES:
                LOGD(TAG, "jump types");
                mTitle = mSectionTitles[SECTION_JUMPTYPES];
                mSection = SECTION_JUMPTYPES;
                break;
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
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

}
