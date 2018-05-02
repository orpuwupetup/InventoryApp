package com.example.orpuwupetup.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.R;
import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import java.net.URI;

/**
 * Created by cezar on 26.04.2018.
 */

public class ProductProvider extends ContentProvider{

    private InventoryDbHelper dbHelper;

    /** URI matcher code for the content URI for the whole inventory table */
    private static final int ALL_PRODUCTS = 60;

    /** URI matcher code for the content URI for the single product */
    private static final int PRODUCT_ID = 61;

    /** global UriMatcher returning codes for the specified Uri */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** variable telling if insertion to the table was present (or quantity of existing product changed) */
    private boolean wasInserted;

    /** Static initializer to add our two codes to the UriMatcher */
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ALL_PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {

        // Create Data Base Helper for all the other methods of the provider
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Cursor for returning queried data
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_PRODUCTS:

                // for whole table, query database for specified entries, possible more than one result in Cursor
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case PRODUCT_ID:

                // For single product queries, extract id of the requested row from the URI itself
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // perform the query on the table to get Cursor with requested product
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //notification URI will tell us that we need to update the cursor if data at this URI changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        // this method returns MIME type of the of the data for the content URI.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_PRODUCTS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        wasInserted = false;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_PRODUCTS:

                /*
                call our insertProduct() method to actually insert product into the database (only one case is
                checked, because we want to store our products in all products list, not at specific id)
                */
                Uri returnUri = insertProduct(uri, values);
                if (wasInserted){

                    //notify all listeners that the data in this URI has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnUri;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues){


        // Data validation
            // Product should contain name, price and supplier name
        if(contentValues.get(InventoryEntry.COLUMN_PRODUCT_NAME) == null){
            throw new IllegalArgumentException("Product should have name");
        }
        if(contentValues.get(InventoryEntry.COLUMN_PRODUCT_PRICE) == null){
            throw new IllegalArgumentException("Product should have price");
        }
        if(contentValues.get(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME) == null){
            throw new IllegalArgumentException("Product should have supplier name");
        }

        /*
        because I specified product name column to has an UNIQUE value, we can put each specific product values just once,
        so each time we want to put new entry in database we have to check if it is already present
        and if so, just change it quantity to higher, and only if its not, add it as a new row
        */

        // open database in write mode to put in new product
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // open database in read mode to get old quantity value
        SQLiteDatabase readDb = dbHelper.getReadableDatabase();

        // Insert a new row for product in the database, returning the ID of that new row.
        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        /*
        check if product is already in the database (if not, newRowId will be equal to -1).
        unfortunately its not 100% foolproof yet, because if someone will put wrong SQL query
        app will crash, because newRowId value will be -1 ass well, but product won't be present
        in the table, and app will throw Exception
        */
        if (newRowId == -1) {

        /*
        construct String[] with arguments passed to the query, such as what rows we want to get
        and what is the where clause for our query, then make query (for quantity of product in the
        database if it was inserted before
        */
            String[] newProjection = {InventoryEntry.COLUMN_PRODUCT_QUANTITY};
            String[] whereClause = {(String) contentValues.get(InventoryEntry.COLUMN_PRODUCT_NAME)};

            /*
            try to change quantity of product that is present in the table already (because we
            couldn't add it in new row, so it could either already be in the table, or there was
            problem with the query), if the query was wrong, say to user that there was problem with
            adding product to the table, and if product was in the table, just change it quantity and
            say user about it (Prevention against bug depicted in line 153)
            */
            try {

                // ask if product is already in table
                Cursor cursor = readDb.query(InventoryEntry.TABLE_NAME,
                        newProjection,
                        InventoryEntry.COLUMN_PRODUCT_NAME + "=?",
                        whereClause,
                        null,
                        null,
                        null);

        /*
        we know that we will get just one row (only one product can have this specific name)
        so we can just move to the first row, without while(cursor.moveToNext()) loop
        */
                cursor.moveToFirst();

                // get current quantity of the product and calculate new quantity
                int currentQuantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
                int newQuantity = (int) contentValues.get(InventoryEntry.COLUMN_PRODUCT_QUANTITY) + currentQuantity;

                // create new ContentValues object with new product quantity
                ContentValues updatedQuantity = new ContentValues();
                updatedQuantity.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                /* update table with new value of quantity of the product (if its different than 0) */
                if(Integer.parseInt(contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) != 0) {
                    db.update(InventoryEntry.TABLE_NAME, updatedQuantity, InventoryEntry.COLUMN_PRODUCT_NAME + "=?", whereClause);

                    // close cursor and tell user quantity of what was updated and by how much
                    cursor.close();
                    Toast.makeText(getContext(), whereClause[0] + " "
                            + getContext().getResources().getString(R.string.toast_message_product_already_in_table_quantity_updated)
                            + " " + contentValues.get(InventoryEntry.COLUMN_PRODUCT_QUANTITY), Toast.LENGTH_LONG).show();

                    // notify that data in the database has been changed
                    wasInserted = true;
                    // TODO: Extract all string to the R.Strings
                }else{
                    Toast.makeText(getContext(), whereClause[0] + " was already added to the table.", Toast.LENGTH_SHORT).show();
                }
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "Problem with adding product", Toast.LENGTH_LONG).show();
            }
        } else {

            // If newRowId is different than -1, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(getContext(), "Product saved to the table", Toast.LENGTH_SHORT).show();

            // notify that data in the database has been changed
            wasInserted = true;
        }

        // close readable and writable database to prevent memory leaks
        readDb.close();
        db.close();
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (match) {
            case ALL_PRODUCTS:

                // delete from data base
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0){

                    //notify all listeners that the data in this URI has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case PRODUCT_ID:

                /*
                For the PRODUCT_ID code, extract out the ID from the URI,
                so we know which row to update. Selection will be "_id=?" and selection
                arguments will be a String array containing the actual ID.
                */
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // delete from data base and check if any row was deleted
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0){

                    //notify all listeners that the data in this URI has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ALL_PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:

                /*
                For the PRODUCT_ID code, extract out the ID from the URI,
                so we know which row to update. Selection will be "_id=?" and selection
                arguments will be a String array containing the actual ID.
                */
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                int updatedRows = updateProduct(uri, values, selection, selectionArgs);

                if(updatedRows != 0) {

                    //notify all listeners that the data in this URI has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return updatedRows;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct (Uri uri, ContentValues values, String selection, String[] selectionArgs){

        /*
        We don't have to check anything here, (except size of ContentValues provided) because all
        data types (and scopes) are checked at the user input
            If there are no values to update, then don't try to update the database, otherwise update
        the database with new info
        */
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
