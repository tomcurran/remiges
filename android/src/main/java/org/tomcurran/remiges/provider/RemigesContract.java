package org.tomcurran.remiges.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class RemigesContract {

    interface JumpsColumns {
        String JUMP_ID = "jump_id";
        String JUMP_NUMBER = "jump_number";
        String JUMP_DATE = "jump_date";
        String JUMP_DESCRIPTION = "jump_description";
    }

    public static final String CONTENT_AUTHORITY = "org.tomcurran.remiges";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_JUMPS = "jumps";

    public static class Jumps implements JumpsColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_JUMPS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.remiges.jump";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.remiges.jump";
        public static final String DEFAULT_SORT = Jumps.JUMP_NUMBER + " DESC, " + Jumps.JUMP_DATE + " DESC";

        public static Uri buildJumpUri(String jumpId) {
            return CONTENT_URI.buildUpon().appendPath(jumpId).build();
        }

        public static Uri buildJumpUri(long jumpId) {
            return buildJumpUri(String.valueOf(jumpId));
        }

        public static String getJumpId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

}
