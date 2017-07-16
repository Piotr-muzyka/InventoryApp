package com.example.android.inventoryapp;

/**
 * Created by PiotrM on 16.07.2017.
 */


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ItemContract;

/**
 * Adapter creates a list item for each row of item data in the Cursor.
 */
public class ItemCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = ItemCursorAdapter.class.getSimpleName();

    /**
     * CursorAdapter constructor.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Binding item data to a certain listView item (filling up TextViews etc.).
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        //ImageView photoImageView = (ImageView) view.findViewById(R.id.photo);

        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        //int photoColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO);

        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemQuantity = cursor.getString(priceColumnIndex);
        //String itemPhoto = cursor.getString(priceColumnIndex);

        nameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        quantityTextView.setText(itemName);
        //photoImageView.setImageResource(itemPhoto);
    }
}
