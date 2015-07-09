package com.contentprovider.allan.contenproviderexample_01;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class MyContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "allan.provider.URIAuthorities";
    static final String PROVIDER_URL = "content://" + PROVIDER_NAME + "/friends";
    static final Uri CONTENT_URI = Uri.parse(PROVIDER_URL);

    // fields for the database
    static final String ID = "id";
    static final String NAME = "name";
    static final String BIRTHDAY = "birthday";

    // integer values used in content URI
    static final int FRIENDS = 1;
    static final int FRIENDS_ID = 2;

    // database declarations
    private SQLiteDatabase database;
    static final String DATA_BASE_NAME = "BirthdayReminder";
    static final String TABLE_NAME = "birthTable";
    static final int DATA_BASE_VERSION = 1;

    static final String CREATE_TABLE =
            " CREATE TABLE " + TABLE_NAME +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " name TEXT NOT NULL, " +
                    " birthday TEXT NOT NULL);";

    private DBHelper dbHelper;

    // projection map for a query
    private static HashMap<String, String> BirthMap;

    // maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "friends", FRIENDS);
        uriMatcher.addURI(PROVIDER_NAME, "friends/#", FRIENDS_ID);
    }

    public MyContentProvider() {
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        if (database == null) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                // delete all the records of the table
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case FRIENDS_ID:
                String id = uri.getLastPathSegment(); //gets the id
                count = database.delete(TABLE_NAME, ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                return "vnd.android.cursor.dir/vnd.example.friends";
            // Get a particular friend
            case FRIENDS_ID:
                return "vnd.android.cursor.item/vnd.example.friends";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = database.insert(TABLE_NAME, "", values);
        // If record is added successfully
        if (row > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // the TABLE_NAME to query on
        queryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                queryBuilder.setProjectionMap(BirthMap);
                break;

            case FRIENDS_ID:
                queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == "") {
            // No sorting-> sort on names by default
            sortOrder = NAME;
        }
        Cursor cursor = queryBuilder.query(database, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case FRIENDS_ID:
                count = database.update(TABLE_NAME, values, ID +
                        " = " + uri.getLastPathSegment() +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    /**
     * DBHelper inner class
     */
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
