package org.tomcurran.remiges.ui.nopane;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.tomcurran.remiges.liberation.RemigesLiberation;
import org.tomcurran.remiges.provider.RemigesContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

/**
 * An activity to illustrate how to pick a file with the
 * opener intent.
 */
public class ImportDriveActivity extends DriveActivity {
    private static final String TAG = makeLogTag(ImportDriveActivity.class);

    private static final String MIME_TYPE = "application/javascript";

    private static final int REQUEST_CODE_OPENER = 1;

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain", MIME_TYPE })
                .build(getGoogleApiClient());
        try {
            startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } catch (SendIntentException e) {
          Log.w(TAG, "Unable to send intent", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CODE_OPENER:
            if (resultCode == RESULT_OK) {
                DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                showMessage("Selected file's ID: " + driveId);
                new RetrieveDriveFileContentsAsyncTask(this).execute(driveId);
            }
            finish();
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    final private class RetrieveDriveFileContentsAsyncTask extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), params[0]);
            DriveApi.ContentsResult contentsResult = file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(contentsResult.getContents().getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
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
                showMessage("Error while reading from the file");
                return;
            }
//            showMessage("File contents: " + result.substring(0, 128) + "...");
            try {
                getContentResolver().applyBatch(
                        RemigesContract.CONTENT_AUTHORITY,
                        RemigesLiberation.getImportOperations(result)
                );
            } catch (ParseException e) {
                LOGE(TAG, String.format("Import data JSON parse error: %s", e.getMessage()));
            } catch (RemoteException e) {
                LOGE(TAG, String.format("Import data provider communication error: %s", e.getMessage()));
            } catch (OperationApplicationException e) {
                LOGE(TAG, String.format("Import data insertion error: %s", e.getMessage()));
            }
        }
    }
}
