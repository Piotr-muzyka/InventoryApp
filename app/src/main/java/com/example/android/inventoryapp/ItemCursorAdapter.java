package com.example.android.inventoryapp;

/**
 * Created by PiotrM on 16.07.2017.
 */


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;
import com.squareup.picasso.Picasso;

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
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.photo);
        TextView providerTextView = (TextView) view.findViewById(R.id.provider);

        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        int photoColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO);
        int providerColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PROVIDER);

        final Uri currentUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry._ID)) );

        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);
        String itemProvider = cursor.getString(providerColumnIndex);

        nameTextView.setText("Name: " + itemName);
        priceTextView.setText("Price: " + itemPrice);
        quantityTextView.setText("Quantity: " + itemQuantity);
        providerTextView.setText("Provider: " + itemProvider);

        // http://square.github.io/picasso/ - lib used here
        Uri productPhoto = Uri.parse(cursor.getString(photoColumnIndex));
        Picasso.with(context).load(productPhoto)
                .placeholder(R.drawable.ic_image_area_close_black_48dp)
                .fit()
                .into(photoImageView);

        Button sellButton = (Button) view.findViewById(R.id.sell_item);

        final int quantity = cursor.getInt(quantityColumnIndex);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();

                if (quantity > 0) {

                    int decrementQuantity = quantity;
                    values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, --decrementQuantity);
                    resolver.update(
                            currentUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentUri, null);
                } else {

                    Toast.makeText(context, R.string.item_sold, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
