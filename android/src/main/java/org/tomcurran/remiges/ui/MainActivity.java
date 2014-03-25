package org.tomcurran.remiges.ui;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.gson.JsonSyntaxException;

import org.tomcurran.remiges.BuildConfig;
import org.tomcurran.remiges.R;
import org.tomcurran.remiges.liberation.RemigesLiberation;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.GoogleApiClientAsyncTask;
import org.tomcurran.remiges.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.LOGI;
import static org.tomcurran.remiges.util.LogUtils.LOGW;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements
        NavigationDrawerFragment.Callbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = makeLogTag(MainActivity.class);

    private static final String FRAGMENT_JUMPS = "fragment_tag_jumps";
    private static final String FRAGMENT_PLACES = "fragment_tag_places";
    private static final String FRAGMENT_JUMPTYPES = "fragment_tag_jumptypes";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    private String[] mSectionTitles;
    private int mSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    mSection = NavigationDrawerFragment.SECTION_JUMPS;
                    break;
                case NavigationDrawerFragment.SECTION_PLACES:
                    attachFragment(PlaceFragment.class, FRAGMENT_PLACES);
                    mTitle = mSectionTitles[NavigationDrawerFragment.SECTION_PLACES];
                    mSection = NavigationDrawerFragment.SECTION_PLACES;
                    break;
                case NavigationDrawerFragment.SECTION_JUMPTYPES:
                    attachFragment(JumpTypeFragment.class, FRAGMENT_JUMPTYPES);
                    mTitle = mSectionTitles[NavigationDrawerFragment.SECTION_JUMPTYPES];
                    mSection = NavigationDrawerFragment.SECTION_JUMPTYPES;
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
            case R.id.menu_export:
                exportToDrive();
                return true;
            case R.id.menu_import:
                importFromDrive();
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_debug_insert_data:
                try {
                    getContentResolver().applyBatch(
                            RemigesContract.CONTENT_AUTHORITY,
                            RemigesLiberation.getImportOperations(
                                    Utils.readAsset(this, "testdata" + File.separator + "tc.json")));
                } catch (JsonSyntaxException e) {
                    LOGE(TAG, String.format("Test data JSON parse error: %s", e.getMessage()));
                } catch (RemoteException e) {
                    LOGE(TAG, String.format("Test data provider communication error: %s", e.getMessage()));
                } catch (OperationApplicationException e) {
                    LOGE(TAG, String.format("Test data insertion error: %s", e.getMessage()));
                }
                return true;
            case R.id.menu_debug_delete_data:
                try {
                    getContentResolver().applyBatch(RemigesContract.CONTENT_AUTHORITY, RemigesLiberation.getDeleteOperations());
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

    // Data Liberation

    private static final int REQUEST_RESOLUTION = 1;
    private static final int REQUEST_EXPORT = 2;
    private static final int REQUEST_IMPORT = 3;

    private static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
    private static final String MIME_TYPE_JAVASCRIPT = "application/javascript";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESOLUTION:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
            case REQUEST_EXPORT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                }
                break;
            case REQUEST_IMPORT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    new RetrieveDriveFileContentsAsyncTaskGoogle(this).execute(driveId);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LOGI(TAG, "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        LOGI(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        LOGI(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            LOGE(TAG, "Exception while starting resolution activity", e);
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    final ResultCallback<DriveApi.ContentsResult> mExportCallback = new ResultCallback<DriveApi.ContentsResult>() {
        @Override
        public void onResult(DriveApi.ContentsResult result) {
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setTitle("remiges-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".json")
                    .setMimeType(MIME_TYPE_JAVASCRIPT)
                    .build();
            OutputStream outputStream = result.getContents().getOutputStream();
            try {
                outputStream.write(RemigesLiberation.getExportJson(getContentResolver()).getBytes());
            } catch (IOException e) {
                LOGE(TAG, String.format("I/O error exporting data: %s", e.getMessage()));
            }
            IntentSender intentSender = Drive.DriveApi
                    .newCreateFileActivityBuilder()
                    .setInitialMetadata(metadataChangeSet)
                    .setInitialContents(result.getContents())
                    .build(getGoogleApiClient());
            try {
                startIntentSenderForResult(intentSender, REQUEST_EXPORT, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                LOGW(TAG, "Unable to send intent", e);
            }
        }
    };

    final private class RetrieveDriveFileContentsAsyncTaskGoogle extends GoogleApiClientAsyncTask<DriveId, Boolean, String> {

        private Context mContext;

        public RetrieveDriveFileContentsAsyncTaskGoogle(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), params[0]);
            DriveApi.ContentsResult contentsResult = file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                return null;
            }
            try {
                contents = Utils.readFromInputStream(contentsResult.getContents().getInputStream());
            } catch (IOException e) {
                LOGE(TAG, "IOException while reading from the stream", e);
            }
            file.discardContents(getGoogleApiClient(), contentsResult.getContents()).await();
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                String error = "Import data JSON parse error";
                LOGE(TAG, String.format("%s", error));
                Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
                return;
            }
            try {
                getContentResolver().applyBatch(
                        RemigesContract.CONTENT_AUTHORITY,
                        RemigesLiberation.getImportOperations(result)
                );
            } catch (JsonSyntaxException e) {
                String error = "Import data JSON parse error";
                LOGE(TAG, String.format("%s: %s", error, e.getMessage()));
                Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
            } catch (RemoteException e) {
                String error = "Import data provider communication error";
                LOGE(TAG, String.format("%s: %s", error, e.getMessage()));
                Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
            } catch (OperationApplicationException e) {
                String error = "Import data insertion error";
                LOGE(TAG, String.format("%s: %s", error, e.getMessage()));
                Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void exportToDrive() {
        Drive.DriveApi.newContents(getGoogleApiClient()).setResultCallback(mExportCallback);
    }

    public void importFromDrive() {
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { MIME_TYPE_PLAIN_TEXT, MIME_TYPE_JAVASCRIPT })
                .build(getGoogleApiClient());
        try {
            startIntentSenderForResult(intentSender, REQUEST_IMPORT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            LOGW(TAG, "Unable to send intent", e);
        }
    }

}
