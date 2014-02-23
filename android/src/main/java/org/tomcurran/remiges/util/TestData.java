package org.tomcurran.remiges.util;


import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.provider.RemigesContract.JumpTypes;
import org.tomcurran.remiges.provider.RemigesContract.Jumps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class TestData {
    private static final String TAG = makeLogTag(SelectionBuilder.class);

    private static final String TEST_DATA_ASSET = "test-data.json";

    private Context mContext;

    public TestData(Context context) {
        mContext = context;
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

        // Jump Types
        Map<Integer, Integer> jumpTypeBackRefs = new HashMap<Integer, Integer>();
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
                    .build());
        }

        // Insert data
        mContext.getContentResolver().applyBatch(RemigesContract.CONTENT_AUTHORITY, operations);
    }

    private JSONObject loadJSON() throws JSONException {
        return new JSONObject(Utils.readAsset(mContext, TEST_DATA_ASSET));
    }

    public void delete() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newDelete(Jumps.CONTENT_URI).build());
        operations.add(ContentProviderOperation.newDelete(JumpTypes.CONTENT_URI).build());
        mContext.getContentResolver().applyBatch(RemigesContract.CONTENT_AUTHORITY, operations);
    }

}
