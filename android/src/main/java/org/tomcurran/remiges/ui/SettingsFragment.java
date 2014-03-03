package org.tomcurran.remiges.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;


public class SettingsFragment extends PreferenceFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String PREFERENCE_JUMPTYPE = "preference_key_default_jumptype";
    public static final String PREFERENCE_WAY = "preference_key_default_way";
    public static final String PREFERENCE_EXIT_ALTITUDE = "preference_key_default_exit_altitude";
    public static final String PREFERENCE_DEPLOYMENT_ALTITUDE = "preference_key_default_deployment_altitude";
    public static final String PREFERENCE_DELAY = "preference_key_default_delay";

    public static final String PREFERENCE_DEFAULT_JUMPTYPE = "0";
    public static final String PREFERENCE_DEFAULT_WAY = "1";
    public static final String PREFERENCE_DEFAULT_EXIT_ALTITUDE = "0";
    public static final String PREFERENCE_DEFAULT_DEPLOYMENT_ALTITUDE = "0";
    public static final String PREFERENCE_DEFAULT_DELAY = "0";

    private static final int LOADER_JUMPTYPE = 0;

    private ListPreference mJumpType;
    private Cursor mJumpTypeCursor;
    private ListPreferenceAdapter mJumpTypeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mJumpType = (ListPreference) findPreference(PREFERENCE_JUMPTYPE);
        mJumpTypeAdapter = new ListPreferenceAdapter() {
            @Override
            public CharSequence getEntry(Cursor data) {
                return data.getString(JumpTypeQuery.NAME);
            }
            @Override
            public CharSequence getEntryValue(Cursor data) {
                return data.getString(JumpTypeQuery._ID);
            }
        };

        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_WAY));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_EXIT_ALTITUDE));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_DEPLOYMENT_ALTITUDE));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_DELAY));

        LoaderManager loaderManager = getActivity().getLoaderManager();
        loaderManager.initLoader(LOADER_JUMPTYPE, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_JUMPTYPE:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.JumpTypes.CONTENT_URI,
                        JumpTypeQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.JumpTypes.DEFAULT_SORT
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_JUMPTYPE:
                mJumpTypeCursor = data;
                loadCursor(mJumpType, mJumpTypeCursor, mJumpTypeAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_JUMPTYPE:
                mJumpTypeCursor = null;
                break;
        }
    }

    private interface JumpTypeQuery {

        String[] PROJECTION = {
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.JumpTypes._ID
        };

        int NAME = 0;
        int _ID = 1;

    }

    private interface ListPreferenceAdapter {
        CharSequence getEntry(Cursor data);
        CharSequence getEntryValue(Cursor data);
    }

    private void loadCursor(ListPreference list, Cursor data, ListPreferenceAdapter adapter) {
        int count = data.getCount();
        CharSequence[] entries = new CharSequence[count];
        CharSequence[] entryValues = new CharSequence[count];

        if (data.moveToFirst()) {
            for(int i = 0; i < count; i++) {
                entries[i] = adapter.getEntry(data);
                entryValues[i] = adapter.getEntryValue(data);
                data.moveToNext();
            }
        }

        list.setEntries(entries);
        list.setEntryValues(entryValues);

        if (list.getOnPreferenceChangeListener() == null) {
            SettingsActivity.bindPreferenceSummaryToValue(mJumpType);
        }
    }

}
