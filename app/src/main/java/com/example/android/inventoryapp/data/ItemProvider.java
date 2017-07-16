package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.net.URI;

/**
 * Created by PiotrM on 16.07.2017.
 */

public class ItemProvider extends ContentProvider {

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /** items table URI matcher code. */
    private static final int ITEMS = 100;

    /** single item URI matcher code. */
    private static final int ITEM_ID = 101;

    /** Object to match a content URI to a URI matcher code. */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** The following code will be executed upon this class content is called for the first time. */
    static {

        /** based on the URI call a single item or a items table should be accessed.*/
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    private ItemDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        /** Check if URI can be matched for a input code. */
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                /** "=?" wildcard is used so a specific table item can be returned. */
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /** Insert a new item into the database with the given content values. Return the new content URI for that specific row in the database. */
    private Uri insertItem(Uri uri, ContentValues values) {

        /** Check that the name is not null */
        String name = values.getAsString(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        /** If the price is provided, check that it's greater than or equal to 0 $. */
        Integer price = values.getAsInteger(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Item requires valid price");
        }

        /** If the quantity is provided, check that it's greater than or equal to 0. */
        Integer quantity = values.getAsInteger(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Item requires valid quantity");
        }

        /** Get writable database */
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        /** Insert the new item with the given values */
        long id = database.insert(ItemContract.ItemEntry.TABLE_NAME, null, values);

        /** If the ID is -1, then the insertion failed. Log an error and return null. */
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        /** Notify all listeners that the data has changed for the item content URI. */
        getContext().getContentResolver().notifyChange(uri, null);

        /** Return the new URI with the ID (of the newly inserted row) appended at the end. */
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemContract.ItemEntry._ID + "=?";
                /** Upon update an item ID is extracted from the URI so the correct row gets updated. */
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        /** Same rules as for insertion apply here. */
        if (values.containsKey(ItemContract.ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }


        if (values.containsKey(ItemContract.ItemEntry.COLUMN_ITEM_PRICE)) {

            Integer price = values.getAsInteger(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }


        if (values.containsKey(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY)) {

            Integer quantity = values.getAsInteger(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        /** If there are no values to update, then don't try to update the database. */
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ItemContract.ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        /** returns number of rows which were updated in the table. */
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /** Delete rows matchin selection. */
                rowsDeleted = database.delete(ItemContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                /** Delete for a single row. */
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

       /** number of deleted rows is returned. */
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemContract.ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemContract.ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
