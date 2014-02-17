package org.tomcurran.remiges.util;

import android.content.Context;
import android.database.Cursor;

import org.tomcurran.remiges.provider.RemigesContract;

public class DbAdpater {

    public static final String[] SELECTION_ARG_ZERO = { "0" };

    public static int getHighestJumpNumber(Context context) {
        final Cursor cursor = context.getContentResolver().query(
                RemigesContract.Jumps.CONTENT_URI,
                HighestNumberQuery.PROJECTION,
                HighestNumberQuery.SELECTION,
                SELECTION_ARG_ZERO,
                RemigesContract.Jumps.DEFAULT_SORT
        );
        int highestJumpNumber = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return highestJumpNumber;
    }

    private interface HighestNumberQuery {

        String[] PROJECTION = {
                "max(" + RemigesContract.Jumps.JUMP_NUMBER + ")"
        };

        String SELECTION = RemigesContract.Jumps.JUMP_NUMBER + ">?";
    }

}
