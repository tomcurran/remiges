package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.ListPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;

import org.tomcurran.remiges.BuildConfig;
import org.tomcurran.remiges.R;
import org.tomcurran.remiges.liberation.RemigesLiberation;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.Utils;

import java.io.File;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class SettingsFragment extends PreferenceFragmentCompat implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(SettingsFragment.class);

    public static final String PREFERENCE_PLACE = "preference_key_default_place";
    public static final String PREFERENCE_JUMPTYPE = "preference_key_default_jumptype";
    public static final String PREFERENCE_WAY = "preference_key_default_way";
    public static final String PREFERENCE_EXIT_ALTITUDE = "preference_key_default_exit_altitude";
    public static final String PREFERENCE_DEPLOYMENT_ALTITUDE = "preference_key_default_deployment_altitude";
    public static final String PREFERENCE_DELAY = "preference_key_default_delay";

    private static final int LOADER_PLACE = 0;
    private static final int LOADER_JUMPTYPE = 1;

    private ListPreference mJumpTypePreference;
    private Cursor mJumpTypeCursor;
    private ListPreferenceAdapter mJumpTypeAdapter;

    private ListPreference mPlacePreference;
    private Cursor mPlaceCursor;
    private ListPreferenceAdapter mPlaceAdapter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        mJumpTypePreference = (ListPreference) findPreference(PREFERENCE_JUMPTYPE);
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

        mPlacePreference = (ListPreference) findPreference(PREFERENCE_PLACE);
        mPlaceAdapter = new ListPreferenceAdapter() {
            @Override
            public CharSequence getEntry(Cursor data) {
                return data.getString(PlaceQuery.NAME);
            }
            @Override
            public CharSequence getEntryValue(Cursor data) {
                return data.getString(PlaceQuery._ID);
            }
        };

        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_WAY));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_EXIT_ALTITUDE));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_DEPLOYMENT_ALTITUDE));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(PREFERENCE_DELAY));

        LoaderManager loaderManager = getActivity().getLoaderManager();
        loaderManager.initLoader(LOADER_PLACE, null, this);
        loaderManager.initLoader(LOADER_JUMPTYPE, null, this);

        if (BuildConfig.DEBUG) {
            enableDebugSettings();
        }
    }

    @Override
    public void onPause() {
        for (Fragment fragment : getFragmentManager().getFragments()) {
            if (fragment instanceof ListPreferenceDialogFragmentCompat) {
                ((ListPreferenceDialogFragmentCompat) fragment).dismiss();
            }
        }

        super.onPause();
    }

    private void enableDebugSettings() {
        Context context = getActivity();

        PreferenceCategory category = new PreferenceCategory(context);
        category.setTitle(R.string.preference_title_debug);
        getPreferenceScreen().addPreference(category);

        Preference insertPreference = new Preference(context);
        insertPreference.setTitle(R.string.preference_title_debug_insert_data);
        insertPreference.setSummary(R.string.preference_summary_debug_insert_data);
        insertPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                try {
                    activity.getContentResolver().applyBatch(
                            RemigesContract.CONTENT_AUTHORITY,
                            RemigesLiberation.getImportOperations(
                                    Utils.readAsset(activity, "testdata" + File.separator + "tc.json")));
                    Toast.makeText(activity, "Test data inserted successfully", Toast.LENGTH_SHORT).show();
                } catch (JsonSyntaxException e) {
                    String message = "Test data JSON parse error";
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    LOGE(TAG, String.format("%s: %s", message, e.getMessage()));
                } catch (RemoteException e) {
                    String message = "Test data provider communication error";
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    LOGE(TAG, String.format("%s: %s", message, e.getMessage()));
                } catch (OperationApplicationException e) {
                    String message = "Test data insertion error";
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    LOGE(TAG, String.format("%s: %s", message, e.getMessage()));
                }
                return true;
            }
        });
        category.addPreference(insertPreference);

        Preference deletePreference = new Preference(context);
        deletePreference.setTitle(R.string.preference_title_debug_delete_data);
        deletePreference.setSummary(R.string.preference_summary_debug_delete_data);
        deletePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                try {
                    activity.getContentResolver().applyBatch(RemigesContract.CONTENT_AUTHORITY, RemigesLiberation.getDeleteOperations());
                    Toast.makeText(activity, "All data deleted successfully", Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    String message = "Test data provider communication error";
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    LOGE(TAG, String.format("%s: %s", message, e.getMessage()));
                } catch (OperationApplicationException e) {
                    String message = "Test data deletion error";
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    LOGE(TAG, String.format("%s: %s", message, e.getMessage()));
                }
                return true;
            }
        });
        category.addPreference(deletePreference);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_PLACE:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.Places.CONTENT_URI,
                        PlaceQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.Places.DEFAULT_SORT
                );
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
            case LOADER_PLACE:
                mPlaceCursor = data;
                loadCursor(mPlacePreference, mPlaceCursor, mPlaceAdapter);
                break;
            case LOADER_JUMPTYPE:
                mJumpTypeCursor = data;
                loadCursor(mJumpTypePreference, mJumpTypeCursor, mJumpTypeAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_PLACE:
                mPlaceCursor = null;
                break;
            case LOADER_JUMPTYPE:
                mJumpTypeCursor = null;
                break;
        }
    }

    private interface PlaceQuery {

        String[] PROJECTION = {
                RemigesContract.Places.PLACE_NAME,
                RemigesContract.Places._ID
        };

        int NAME = 0;
        int _ID = 1;

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

        boolean hasData = data.moveToFirst();
        if (hasData) {
            for(int i = 0; i < count; i++) {
                entries[i] = adapter.getEntry(data);
                entryValues[i] = adapter.getEntryValue(data);
                data.moveToNext();
            }
        }

        list.setEntries(entries);
        list.setEntryValues(entryValues);
        list.setEnabled(hasData);

        if (list.getOnPreferenceChangeListener() == null) {
            SettingsActivity.bindPreferenceSummaryToValue(list);
        }
    }

}
