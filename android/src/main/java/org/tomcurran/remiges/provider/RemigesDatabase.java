package org.tomcurran.remiges.provider;

import org.tomcurran.remiges.provider.RemigesContract.Jumps;
import org.tomcurran.remiges.provider.RemigesContract.JumpsColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGW;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class RemigesDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(RemigesDatabase.class);

    private static final String DATABASE_NAME = "remiges.db";

    private static final int VER_INIT = 1;
    private static final int DATABASE_VERSION = VER_INIT;

    interface Tables {
        String JUMPS = "jumps";
    }

    public RemigesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.JUMPS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JumpsColumns.JUMP_NUMBER + " INTEGER NOT NULL,"
                + JumpsColumns.JUMP_DATE + " INTEGER NOT NULL,"
                + JumpsColumns.JUMP_DESCRIPTION + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        int version = oldVersion;

//        switch (version) {
//            case VER_LAUNCH:
//                version = VER_nextcase...
//        }

        LOGD(TAG, "after upgrade logic, at version " + version);
        if (version != DATABASE_VERSION) {
            LOGW(TAG, "Destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.JUMPS);

            onCreate(db);
        }
    }
}
