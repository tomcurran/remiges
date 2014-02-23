package org.tomcurran.remiges.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class RemigesContract {

    interface JumpTypesColumns {
        String JUMPTPYE_NAME = "jumptype_name";
    }

    interface JumpsColumns {
        String JUMP_NUMBER = "jump_number";
        String JUMP_DATE = "jump_date";
        String JUMP_DESCRIPTION = "jump_description";
        String JUMP_WAY = "jump_way";
        String JUMP_EXIT_ALTITUDE = "jump_exit_altitude";
        String JUMP_DEPLOYMENT_ALTITUDE = "jump_deployment_altitude";
        String JUMP_DELAY = "jump_delay";
    }

    public static final String CONTENT_AUTHORITY = "org.tomcurran.remiges";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_JUMPTYPES = "jumptypes";
    private static final String PATH_JUMPS = "jumps";

    public static class JumpTypes implements JumpTypesColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_JUMPTYPES).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.remiges.jumptype";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.remiges.jumptype";
        public static final String DEFAULT_SORT = JumpTypesColumns.JUMPTPYE_NAME + " DESC";

        public static Uri buildJumpTypeUri(String jumpTypeId) {
            return CONTENT_URI.buildUpon().appendPath(jumpTypeId).build();
        }

        public static Uri buildJumpTypeUri(long jumpTypeId) {
            return buildJumpTypeUri(String.valueOf(jumpTypeId));
        }

        public static String getJumpTypeId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static class Jumps implements JumpsColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_JUMPS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.remiges.jump";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.remiges.jump";
        public static final String DEFAULT_SORT = Jumps.JUMP_NUMBER + " DESC, " + Jumps.JUMP_DATE + " DESC";

        public static final String JUMPTYPE_ID = "jumptype_id";

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
