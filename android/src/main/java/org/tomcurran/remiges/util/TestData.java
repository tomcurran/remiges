package org.tomcurran.remiges.util;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tomcurran.remiges.provider.RemigesContract.Jumps;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class TestData {
    private static final String TAG = makeLogTag(SelectionBuilder.class);

    private static final String TEST_DATA_ASSET = "test-data.json";

    private Context mContext;
    private SimpleDateFormat mDateFormatter;

    public TestData(Context context) {
        mContext = context;
    }

    public void insert() throws JSONException, ParseException {
        JSONObject json = loadJSON();
        if (json == null) {
            return;
        }
        ContentResolver resolver = mContext.getContentResolver();
        JSONObject jsonDateFormats = json.getJSONObject("dateFormats");
        mDateFormatter = new SimpleDateFormat(jsonDateFormats.getString("jump"));
        JSONArray jsonJumps = json.getJSONArray("jumps");
        ContentValues[] jumps = new ContentValues[jsonJumps.length()];
        for (int i = 0; i < jsonJumps.length(); i++) {
            JSONObject jsonJump = jsonJumps.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Jumps.JUMP_NUMBER, jsonJump.getInt("number"));
            values.put(Jumps.JUMP_DATE, mDateFormatter.parse(jsonJump.getString("date")).getTime());
            values.put(Jumps.JUMP_DESCRIPTION, jsonJump.getString("description"));
            jumps[i] = values;
        }
        resolver.bulkInsert(Jumps.CONTENT_URI, jumps);
    }

    private JSONObject loadJSON() throws JSONException {
        return new JSONObject(Utils.readAsset(mContext, TEST_DATA_ASSET));
    }

    public void delete() {
        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(Jumps.CONTENT_URI, null, null);
    }

}
