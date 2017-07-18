package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;

public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = InventoryActivity.class.getSimpleName();
    /**
     * Identifier for the item data loader.
     */
    private static final int ITEM_LOADER = 0;
    /**
     * ListView adapter
     */
    ItemCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        /** FloatingActionButton for adding new items to the inventory DB via EditorActivity. */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView itemListView = (ListView) findViewById(R.id.list);

        /** An emptyView is used when the list is empty, it should contain suggestions for user input. */
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        cursorAdapter = new ItemCursorAdapter(this, null);

        itemListView.setAdapter(cursorAdapter);

        /** Clicking on an item in a list allows to edit its data. */
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                /** Uri for a specific item row. */
                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);
                /** Uri is passed to the intent.*/
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }


    private void insertItem() {
        /** insert dummy data for an item. */
        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, "Sodie pops");
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, "42");
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, 1);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO, R.drawable.ic_image_area_close_white_48dp);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PROVIDER, "PAPA JOHN");

        Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemContract.ItemEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from Inventory DB");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        /** Specify a columns on which to focus. */
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemContract.ItemEntry.COLUMN_ITEM_PHOTO,
                ItemContract.ItemEntry.COLUMN_ITEM_PROVIDER};

        /** ContentProvider's query method will be executed on a background thread */
        return new CursorLoader(this,   // Parent activity context
                ItemContract.ItemEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /** Updates cursor with the new item data*/
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /** Inserts null - used when there is need to delete data*/
        cursorAdapter.swapCursor(null);
    }

    public void sell(Cursor cursor, int position) {
        cursor.moveToPosition(position);
        ContentValues values = new ContentValues();
        int itemId = cursor.getColumnIndex(ItemContract.ItemEntry._ID);
        int itemQuantity = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        int value = cursor.getInt(itemQuantity);

        if (-- value <= -1) {
            Toast.makeText(this, "Not enough stock", Toast.LENGTH_SHORT).show();
            return;
        }

        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
        Uri itemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, cursor.getInt(itemId));
        int rowsAffected = getContentResolver().update(itemUri, values, null, null);
        Toast.makeText(this, rowsAffected + "Sold", Toast.LENGTH_SHORT).show();
    }
}

