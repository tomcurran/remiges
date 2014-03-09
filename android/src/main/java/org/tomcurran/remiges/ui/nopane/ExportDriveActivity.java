package org.tomcurran.remiges.ui.nopane;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.tomcurran.remiges.liberation.RemigesLiberation;

import java.io.IOException;
import java.io.OutputStream;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.LOGW;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

/**
 * An activity that illustrates how to use the creator
 * intent to create a new file. The creator intent allows the user
 * to select the parent folder and the title of the newly
 * created file.
 */
public class ExportDriveActivity extends DriveActivity {
    private static final String TAG = makeLogTag(ImportDriveActivity.class);

    private static final String MIME_TYPE = "application/javascript";

    protected static final int REQUEST_CODE_CREATOR = 1;

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Drive.DriveApi.newContents(getGoogleApiClient()).setResultCallback(contentsCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CODE_CREATOR:
            if (resultCode == RESULT_OK) {
                DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                showMessage("File created with ID: " + driveId);
            }
            finish();
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }

    final ResultCallback<ContentsResult> contentsCallback = new ResultCallback<ContentsResult>() {
        @Override
        public void onResult(ContentsResult result) {
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder().setMimeType(MIME_TYPE).build();
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
                startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
            } catch (SendIntentException e) {
                LOGW(TAG, "Unable to send intent", e);
            }
        }
    };
}
