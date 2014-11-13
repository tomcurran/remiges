package org.tomcurran.remiges.ui.liberation;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.tomcurran.remiges.liberation.RemigesLiberation;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.LOGW;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class ExportActivity extends DataLiberationActivity {
    private static final String TAG = makeLogTag(ImportActivity.class);

    @Override
    public void liberation() {
        exportToDrive();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_EXPORT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    finish();
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

    public void exportToDrive() {
        Drive.DriveApi.newContents(getGoogleApiClient()).setResultCallback(mExportCallback);
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

}
