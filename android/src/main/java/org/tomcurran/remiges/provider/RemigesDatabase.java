package org.tomcurran.remiges.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.tomcurran.remiges.provider.RemigesContract.JumpTypesColumns;
import org.tomcurran.remiges.provider.RemigesContract.Jumps;
import org.tomcurran.remiges.provider.RemigesContract.JumpsColumns;

import static org.tomcurran.remiges.util.LogUtils.LOGD;
import static org.tomcurran.remiges.util.LogUtils.LOGW;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class RemigesDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(RemigesDatabase.class);

    private static final String DATABASE_NAME = "remiges.db";

    private static final int VER_INIT = 1;
    private static final int DATABASE_VERSION = VER_INIT;

    private static final String LEFT_OUTER_JOIN = " LEFT OUTER JOIN %3$s ON %1$s.%2$s=%3$s.%4$s";

    interface Tables {
        String JUMPS = "jumps";
        String JUMPTYPES = "jumptypes";

        String JUMPS_JOIN_JUMPTYPES = Tables.JUMPS
                + String.format(LEFT_OUTER_JOIN, Tables.JUMPS, Jumps.JUMPTYPE_ID, Tables.JUMPTYPES, BaseColumns._ID);

    }

    private interface References {
        String JUMPTYPE_ID = "REFERENCES " + Tables.JUMPTYPES + "(" + BaseColumns._ID + ")";;
    }

    public RemigesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.JUMPTYPES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JumpTypesColumns.JUMPTPYE_NAME + " TEXT NOT NULL)"
        );

        db.execSQL("CREATE TABLE " + Tables.JUMPS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Jumps.JUMPTYPE_ID + " INTEGER " + References.JUMPTYPE_ID + ","
                + JumpsColumns.JUMP_NUMBER + " INTEGER NOT NULL,"
                + JumpsColumns.JUMP_DATE + " INTEGER NOT NULL,"
                + JumpsColumns.JUMP_DESCRIPTION + " TEXT,"
                + JumpsColumns.JUMP_WAY + " INTEGER NOT NULL,"
                + JumpsColumns.JUMP_EXIT_ALTITUDE + " INTEGER,"
                + JumpsColumns.JUMP_DEPLOYMENT_ALTITUDE + " INTEGER,"
                + JumpsColumns.JUMP_DELAY + " INTEGER)"
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
            db.execSQL("DROP TABLE IF EXISTS " + Tables.JUMPTYPES);

            onCreate(db);
        }
    }
}
