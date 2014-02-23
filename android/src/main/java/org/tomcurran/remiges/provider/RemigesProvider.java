package org.tomcurran.remiges.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.tomcurran.remiges.provider.RemigesContract.JumpTypes;
import org.tomcurran.remiges.provider.RemigesContract.Jumps;
import org.tomcurran.remiges.provider.RemigesDatabase.Tables;
import org.tomcurran.remiges.util.SelectionBuilder;

import java.util.ArrayList;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class RemigesProvider extends ContentProvider {
    private static final String TAG = makeLogTag(RemigesProvider.class);

    private RemigesDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int JUMPS = 100;
    private static final int JUMPS_ID = 101;

    private static final int JUMPTYPES = 200;
    private static final int JUMPTYPES_ID = 201;

    public RemigesProvider() {
    }

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RemigesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "jumps", JUMPS);
        matcher.addURI(authority, "jumps/#", JUMPS_ID);

        matcher.addURI(authority, "jumptypes", JUMPTYPES);
        matcher.addURI(authority, "jumptypes/#", JUMPTYPES_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RemigesDatabase(getContext());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JUMPS:
                return Jumps.CONTENT_TYPE;
            case JUMPS_ID:
                return Jumps.CONTENT_ITEM_TYPE;
            case JUMPTYPES:
                return JumpTypes.CONTENT_TYPE;
            case JUMPTYPES_ID:
                return JumpTypes.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildExpandedSelection(uri, match);
                Cursor cursor = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JUMPS: {
                long jump_id = db.insertOrThrow(Tables.JUMPS, null, values);
                notifyChange(uri);
                return Jumps.buildJumpUri(String.valueOf(jump_id));
            }
            case JUMPTYPES: {
                long jumptype_id = db.insertOrThrow(Tables.JUMPTYPES, null, values);
                notifyChange(uri);
                return JumpTypes.buildJumpTypeUri(String.valueOf(jumptype_id));
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JUMPS: {
                return builder.table(Tables.JUMPS);
            }
            case JUMPS_ID: {
                final String jumpId = Jumps.getJumpId(uri);
                return builder.table(Tables.JUMPS).where(Jumps._ID + "=?", jumpId);
            }
            case JUMPTYPES: {
                return builder.table(Tables.JUMPTYPES);
            }
            case JUMPTYPES_ID: {
                final String jumpTypeId = JumpTypes.getJumpTypeId(uri);
                return builder.table(Tables.JUMPTYPES).where(JumpTypes._ID + "=?", jumpTypeId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case JUMPS: {
                return builder.table(Tables.JUMPS_JOIN_JUMPTYPES)
                        .mapToTable(Jumps._ID, Tables.JUMPS)
                        .mapToTable(Jumps.JUMPTYPE_ID, Tables.JUMPTYPES);
            }
            case JUMPS_ID: {
                final String jumpId = Jumps.getJumpId(uri);
                return builder.table(Tables.JUMPS_JOIN_JUMPTYPES)
                        .mapToTable(Jumps._ID, Tables.JUMPS)
                        .mapToTable(Jumps.JUMPTYPE_ID, Tables.JUMPS)
                        .where(Qualified.JUMPS_JUMP_ID + "=?", jumpId);
            }
            case JUMPTYPES: {
                return builder.table(Tables.JUMPTYPES);
            }
            case JUMPTYPES_ID: {
                final String jumpTypeId = JumpTypes.getJumpTypeId(uri);
                return builder.table(Tables.JUMPTYPES).where(JumpTypes._ID + "=?", jumpTypeId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    private interface Qualified {
        String JUMPS_JUMP_ID = Tables.JUMPS + "." + Jumps._ID;
    }

}
