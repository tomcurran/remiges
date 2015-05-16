package org.tomcurran.remiges.ui;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.FragmentUtils;
import org.tomcurran.remiges.util.ViewHolder;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
    private static final String TAG = makeLogTag(NavigationDrawerFragment.class);

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    public static final int SECTION_JUMPS = 0;
    public static final int SECTION_PLACES = 1;
    public static final int SECTION_JUMPTYPES = 2;
    public static final int SECTION_SEPARATOR = 3;
    public static final int SECTION_SETTINGS = 4;

    public static interface Callbacks {
        void onNavigationDrawerItemSelected(int position);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onNavigationDrawerItemSelected(int position) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = SECTION_JUMPS;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState == null) {
            Uri uri = getActivity().getIntent().getData();
            if (uri != null) {
                String uriType = getActivity().getContentResolver().getType(uri);
                if (uriType != null) {
                    if (uriType.equals(RemigesContract.Jumps.CONTENT_TYPE) || uriType.equals(RemigesContract.Jumps.CONTENT_ITEM_TYPE)) {
                        mCurrentSelectedPosition = SECTION_JUMPS;
                    } else if (uriType.equals(RemigesContract.Places.CONTENT_TYPE) || uriType.equals(RemigesContract.Places.CONTENT_ITEM_TYPE)) {
                        mCurrentSelectedPosition = SECTION_PLACES;
                    } else if (uriType.equals(RemigesContract.JumpTypes.CONTENT_TYPE) || uriType.equals(RemigesContract.JumpTypes.CONTENT_ITEM_TYPE)) {
                        mCurrentSelectedPosition = SECTION_JUMPTYPES;
                    }
                }
            }
        } else {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        return mDrawerListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDrawerListView.setAdapter(new NavigationDrawerAdapter(getActionBar().getThemedContext()));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    }

    private class NavigationDrawerAdapter extends BaseAdapter implements ListAdapter {

        private static final int TYPE_COUNT = 3;
        private static final int TYPE_NAVIGATE = 0;
        private static final int TYPE_SEPARATE = 1;
        private static final int TYPE_EXTRA = 2;

        private class NavigationItem {
            int type;
            int title;
            int icon;
        }

        private final Context mContext;
        private SparseArray<NavigationItem> mItems;

        public NavigationDrawerAdapter(Context context) {
            mContext = context;
            mItems = new SparseArray<NavigationItem>();
            Resources resources = context.getResources();
            NavigationItem item;

            item = new NavigationItem();
            item.type = TYPE_NAVIGATE;
            item.title = R.string.navigation_drawer_title_navigate_jumps;
            item.icon = R.drawable.ic_navigation_jumps_selector;
            mItems.put(0, item);

            item = new NavigationItem();
            item.type = TYPE_NAVIGATE;
            item.title = R.string.navigation_drawer_title_navigate_places;
            item.icon = R.drawable.ic_navigation_places_selector;
            mItems.put(1, item);

            item = new NavigationItem();
            item.type = TYPE_NAVIGATE;
            item.title = R.string.navigation_drawer_title_navigate_jumptypes;
            item.icon = R.drawable.ic_navigation_jumptypes_selector;
            mItems.put(2, item);

            item = new NavigationItem();
            item.type = TYPE_SEPARATE;
            mItems.put(3, item);

            item = new NavigationItem();
            item.type = TYPE_EXTRA;
            item.title = R.string.navigation_drawer_title_extra_settings;
            mItems.put(4, item);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public NavigationItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NavigationItem item;
            TextView textView;
            switch (getItemViewType(position)) {
                case TYPE_NAVIGATE:
                    if (convertView == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.navigation_drawer_navigate, parent, false);
                    }
                    item = getItem(position);
                    textView = ViewHolder.get(convertView, R.id.navigation_drawer_navigate);
                    textView.setText(item.title);
                    textView.setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0);
                    break;
                case TYPE_SEPARATE:
                    if (convertView == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.navigation_drawer_separator, parent, false);
                    }
                    break;
                case TYPE_EXTRA:
                    if (convertView == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.navigation_drawer_extra, parent, false);
                    }
                    item = getItem(position);
                    textView = ViewHolder.get(convertView, R.id.navigation_drawer_extra);
                    textView.setText(item.title);
                    break;
                default:
                    return null;
            }
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).type != TYPE_SEPARATE;
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        if (position < SECTION_SEPARATOR) {
            mCurrentSelectedPosition = position;
        }
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        mCallbacks.onNavigationDrawerItemSelected(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
        if (mCallbacks == null) {
            throw new IllegalStateException("Parent must implement fragment's callbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            menu.clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

}
