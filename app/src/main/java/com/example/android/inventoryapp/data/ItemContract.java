package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by PiotrM on 16.07.2017.
 */

/**  Item contract is assigned as a final so it can't be extended, all declared values are final. */
public final class ItemContract {

    /**  Empty constructor - prevents accidential instatiation of the contract class.*/
    private ItemContract(){
    }

    /**  CONTENT_AUTHORITY - package name is used since it is unique on the device. Name of App ContentProvider.*/
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**  BASE_CONTENT_URI which can be extended to point to a particular elements by apps using content provider.*/
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**  possible path to access DB item information with Content Provider.*/
    public static final String PATH_ITEMS = "items";

    /**  Inner class - includes constant values for Items DB. */
    public static final class ItemEntry implements BaseColumns{

        /** URI to access item data in DB */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /** Uri for a list of items. */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /** Uri for a single item. */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /** DB table name for items */
        public final static String TABLE_NAME = "items";

        /** Unique Item ID  (_ID INTEGER)  */
        public final static String _ID = BaseColumns._ID;

        /** Item name (name TEXT) */
        public final static String COLUMN_ITEM_NAME ="name";

        /** Item price (price INTEGER) */
        public final static String COLUMN_ITEM_PRICE = "price";

        /** Item photo (photo BLOB) */
        public final static String COLUMN_ITEM_PHOTO = "photo";

        /** Item quantity (quantity INTEGER) */
        public final static String COLUMN_ITEM_QUANTITY = "quantity";

    }
}

