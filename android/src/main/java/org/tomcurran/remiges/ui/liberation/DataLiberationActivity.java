package org.tomcurran.remiges.ui.liberation;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.ui.BaseActivity;
import org.tomcurran.remiges.ui.widget.MaterialProgressDrawable;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.LOGI;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public abstract class DataLiberationActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = makeLogTag(DataLiberationActivity.class);

    protected static final int REQUEST_RESOLUTION = 1;
    protected static final int REQUEST_EXPORT = 2;
    protected static final int REQUEST_IMPORT = 3;

    public static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
    public static final String MIME_TYPE_JAVASCRIPT = "application/javascript";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liberation);

        View container = findViewById(R.id.liberation_container);
        ImageView progressImage = (ImageView) findViewById(R.id.liberation_progress);
        progressImage.setImageDrawable(UIUtils.getProgressDrawable(this, container));
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESOLUTION:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                if (resultCode == FragmentActivity.RESULT_CANCELED) {
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
