package com.example.orpuwupetup.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by cezar on 22.04.2018.
 */

// Helper class for creating and managing data base file
public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shop.db";

    private static final int DATABASE_VERSION = 2;

    // class constructor calling one of constructors of the super class (SQLiteOpenHelper)
    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // TODO: do something to keep price as integer (number of cents, not dollars)
        // TODO: Add description TEXT column and ImageUri TEXT column
        // create String that contains SQL statement to create new data base
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL UNIQUE, " // this one is UNIQUE because I want only one entry of specific product in my database, just change it quantity to higher if I find more pieces of it
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER + " TEXT);"; // this one can be stored as TEXT because we don't need to use it as a value (for equations for example)

        // execute SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String MODIFY_TABEL_ADD_DESCRIPTION = "ALTER TABLE " + InventoryEntry.TABLE_NAME + " ADD COLUMN " + InventoryEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT NOT NULL DEFAULT 'No description...';";

        String MODIFY_TABEL_ADD_IMAGE_URI_STRING = "ALTER TABLE " + InventoryEntry.TABLE_NAME + " ADD COLUMN " + InventoryEntry.COLUMN_PRODUCT_IMAGE_URI_STRING + " TEXT;";

        db.execSQL(MODIFY_TABEL_ADD_DESCRIPTION);
        db.execSQL(MODIFY_TABEL_ADD_IMAGE_URI_STRING);
        Log.d("dbhelper", "table modified!");
    }
}
