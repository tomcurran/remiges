package org.tomcurran.remiges.util;


import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.provider.RemigesContract.JumpTypes;
import org.tomcurran.remiges.provider.RemigesContract.Jumps;
import org.tomcurran.remiges.provider.RemigesContract.Places;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class TestData {
    private static final String TAG = makeLogTag(TestData.class);

    private static final String TEST_DATA_ASSETS = "testdata"  + File.separator;

    private Context mContext;
    private String mAsset;
    private JSONObject mJson;

    public TestData(Context context, String testDataAsset) {
        mContext = context;
        mAsset = TEST_DATA_ASSETS + testDataAsset;
    }

    public void insert() throws JSONException, ParseException, RemoteException, OperationApplicationException {
        JSONObject json = loadJSON();
        if (json == null) {
            return;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        // Date Formats
        JSONObject jsonDateFormats = json.getJSONObject("dateFormats");
        SimpleDateFormat jumpDateFormatter = new SimpleDateFormat(jsonDateFormats.getString("jump"));

        // Places
        SparseIntArray placesBackRefs = new SparseIntArray();
        JSONArray jsonPlaces = json.getJSONArray("places");
        for (int i = 0; i < jsonPlaces.length(); i++) {
            JSONObject jsonPlace = jsonPlaces.getJSONObject(i);

            operations.add(ContentProviderOperation
                    .newInsert(Places.CONTENT_URI)
                    .withValue(Places.PLACE_NAME, jsonPlace.getString("name"))
                    .withValue(Places.PLACE_LONGITUDE, jsonPlace.getDouble("longitude"))
                    .withValue(Places.PLACE_LATITUDE, jsonPlace.getDouble("latitude"))
                    .build());

            placesBackRefs.put(jsonPlace.getInt("id"), operations.size() - 1);
        }

        // Jump Types
        SparseIntArray jumpTypeBackRefs = new SparseIntArray();
        JSONArray jsonJumpTypes = json.getJSONArray("jumpTypes");
        for (int i = 0; i < jsonJumpTypes.length(); i++) {
            JSONObject jsonJumpType = jsonJumpTypes.getJSONObject(i);

            operations.add(ContentProviderOperation
                    .newInsert(JumpTypes.CONTENT_URI)
                    .withValue(JumpTypes.JUMPTPYE_NAME, jsonJumpType.getString("name"))
                    .build());

            jumpTypeBackRefs.put(jsonJumpType.getInt("id"), operations.size() - 1);
        }

        // Jumps
        JSONArray jsonJumps = json.getJSONArray("jumps");
        for (int i = 0; i < jsonJumps.length(); i++) {
            JSONObject jsonJump = jsonJumps.getJSONObject(i);
            operations.add(ContentProviderOperation
                    .newInsert(Jumps.CONTENT_URI)
                    .withValue(Jumps.JUMP_NUMBER, jsonJump.getInt("number"))
                    .withValue(Jumps.JUMP_DATE, jumpDateFormatter.parse(jsonJump.getString("date")).getTime())
                    .withValue(Jumps.JUMP_DESCRIPTION, jsonJump.getString("description"))
                    .withValue(Jumps.JUMP_WAY, jsonJump.getInt("way"))
                    .withValue(Jumps.JUMP_EXIT_ALTITUDE, jsonJump.getInt("exitAltitude"))
                    .withValue(Jumps.JUMP_DEPLOYMENT_ALTITUDE, jsonJump.getInt("deploymentAltitude"))
                    .withValue(Jumps.JUMP_DELAY, jsonJump.getInt("delay"))
                    .withValueBackReference(Jumps.JUMPTYPE_ID, jumpTypeBackRefs.get(jsonJump.getInt("typeJump")))
                    .withValueBackReference(Jumps.PLACE_ID, placesBackRefs.get(jsonJump.getInt("place")))
                    .build());
        }

        // Insert data
        mContext.getContentResolver().applyBatch(RemigesContract.CONTENT_AUTHORITY, operations);
    }

    private JSONObject loadJSON() throws JSONException {
        if (mJson == null) {
            mJson = new JSONObject(Utils.readAsset(mContext, mAsset));
        }
        return mJson;
    }

    public void delete() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newDelete(Jumps.CONTENT_URI).build());
        operations.add(ContentProviderOperation.newDelete(Places.CONTENT_URI).build());
        operations.add(ContentProviderOperation.newDelete(JumpTypes.CONTENT_URI).build());
        mContext.getContentResolver().applyBatch(RemigesContract.CONTENT_AUTHORITY, operations);
    }

}
