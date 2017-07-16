package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by PiotrM on 16.07.2017.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /**
     * DB file name
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Class constructor
     */
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Required classes : onCreate / onUpgrade
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        /** String containing SQL statement for items table, specifying columns characterizing each item */
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + " ("
                + ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemContract.ItemEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_PHOTO + " BLOB, "
                + ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        /** Create DB with the statement {@Link SQL_CREATE_ITEMS_TABLE} */
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO - needed to update DB version.
    }
}
