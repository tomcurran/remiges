package org.tomcurran.remiges.ui.liberation;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.gson.JsonSyntaxException;

import org.tomcurran.remiges.liberation.RemigesLiberation;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.GoogleApiClientAsyncTask;
import org.tomcurran.remiges.util.Utils;

import java.io.IOException;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.LOGI;
import static org.tomcurran.remiges.util.LogUtils.LOGW;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class ImportActivity extends DataLiberationActivity {
    private static final String TAG = makeLogTag(ImportActivity.class);

    @Override
    public void liberation() {
        importFromDrive();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMPORT:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    new RetrieveDriveFileContentsAsyncTaskGoogle(this).execute(driveId);
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

    final private class RetrieveDriveFileContentsAsyncTaskGoogle extends GoogleApiClientAsyncTask<DriveId, Boolean, String> {

        private Activity mContext;

        public RetrieveDriveFileContentsAsyncTaskGoogle(Activity context) {
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
            } else {
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
            endLiberation();
            mContext.finish();
        }
    }

}
