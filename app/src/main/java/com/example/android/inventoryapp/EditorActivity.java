package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by PiotrM on 16.07.2017.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    public static final int PHOTO_REQUEST_CODE = 177;
    private static final int EXISTING_ITEM_LOADER = 0;
    /**
     * Photo input ImageView and Uri
     */
    public ImageView productPhotoView;
    /**
     * Content URI for the existing item
     */
    private Uri currentItemUri;
    /**
     * EditText field to enter the item's name
     */
    private EditText nameEditText;
    /**
     * EditText field to enter the item's price
     */
    private EditText priceEditText;
    /**
     * EditText field to enter the item's quantity
     */
    private EditText quantityEditText;
    private String productPhotoUri = "empty";
    /**
     * EditText field to enter the item's provider
     */
    private EditText providerEditText;
    /**
     * Increment quantity button
     */
    private Button plusButton;
    /**
     * Decrement quantity button
     */
    private Button minusButton;
    /**
     * Order more items from Provider button
     */
    private Button orderButton;
    private boolean itemHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the itemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        currentItemUri = intent.getData();

        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new item.
        if (currentItemUri == null) {
            // This is a new item, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.add_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.edit_item));

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        nameEditText = (EditText) findViewById(R.id.edit_item_name);
        priceEditText = (EditText) findViewById(R.id.edit_item_price);
        quantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        providerEditText = (EditText) findViewById(R.id.edit_item_provider);
        productPhotoView = (ImageView) findViewById(R.id.edit_item_photo);

        /** Button binding */
        plusButton = (Button) findViewById(R.id.button_plus);
        minusButton = (Button) findViewById(R.id.button_minus);
        orderButton = (Button) findViewById(R.id.button_order);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        providerEditText.setOnTouchListener(mTouchListener);

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity;

                if (TextUtils.isEmpty(quantityEditText.getText().toString())) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityEditText.getText().toString()) + 1;
                }
                quantityEditText.setText("" + quantity);
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity;
                if (Integer.parseInt(quantityEditText.getText().toString()) >= 1) {
                    if (TextUtils.isEmpty(quantityEditText.getText().toString())) {
                        quantity = 0;
                    } else {
                        quantity = Integer.parseInt(quantityEditText.getText().toString()) - 1;
                    }
                    quantityEditText.setText("" + quantity);
                }
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderMoreEmail();
            }
        });

        productPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View photoView) {
                productPhotoAddition();
            }
        });

    }

    /**
     * Get user input from editor and save item into database.
     */
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String providerString = providerEditText.getText().toString().trim();

        // Check if this is supposed to be a new item and check if all the fields in the editor are blank
        if (currentItemUri == null && TextUtils.isEmpty(nameString) || TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(providerString)) {

            Toast.makeText(this, "Item wasn't added. Please fill up all required fields.", Toast.LENGTH_SHORT).show();

            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PROVIDER, providerString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO, productPhotoUri);


        // If the price or quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

        // Determine if this is a new or existing item by checking if currentItemUri is null or not
        if (currentItemUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.insert_item_fail,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.insert_item_success,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: currentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because currentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.edit_item_fail,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.edit_item_success,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void productPhotoAddition() {

        Intent choosePhoto = new Intent(Intent.ACTION_PICK);

        // point to photo directory
        File photoDirectory = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = photoDirectory.getPath();

        // create photo URI
        Uri data = Uri.parse(pictureDirectoryPath);
        choosePhoto.setDataAndType(data, "image/*");
        startActivityForResult(choosePhoto, PHOTO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
            }

            Uri currentProductPhoto = data.getData();
            productPhotoUri = currentProductPhoto.toString();


            // http://square.github.io/picasso/ - lib used here
            Picasso.with(this).load(productPhotoUri)
                    .placeholder(R.drawable.ic_image_area_close_black_48dp)
                    .fit()
                    .into(productPhotoView);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemContract.ItemEntry.COLUMN_ITEM_PHOTO,
                ItemContract.ItemEntry.COLUMN_ITEM_PROVIDER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
            int photoColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO);
            int providerColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PROVIDER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String provider = cursor.getString(providerColumnIndex);


            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            priceEditText.setText(Integer.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            providerEditText.setText(provider);

            // Update the photo on the screen with the value from the database
            productPhotoUri = cursor.getString(photoColumnIndex);
            Picasso.with(this).load(productPhotoUri)
                    .placeholder(R.drawable.ic_image_area_close_black_48dp)
                    .fit()
                    .into(productPhotoView);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        providerEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.changes_not_saved);
        builder.setPositiveButton(R.string.changes_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.changes_edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_msg);
        builder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (currentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the currentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_success),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    // after https://stackoverflow.com/questions/8701634/send-email-intent
    private void orderMoreEmail() {
        String nameString = nameEditText.getText().toString().trim();
        String providerString = providerEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String subject = "Order for" + nameString;
        String body = "This is an order for " + nameString + " in quantity : " + quantityString;
        String mailTo = "mailto:" + providerString + "@example.com" +
                "?&subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body);

        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setData(Uri.parse(mailTo));
        startActivity(Intent.createChooser(emailIntent, "Send email to provider ..."));
    }
}