package com.example.orpuwupetup.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import com.example.orpuwupetup.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.only_text);

        insertData();
        getData();
    }

    public void insertData() {

        // Create database helper
        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

        /*
        Create a ContentValues object where column names are the keys,
        and dummy product value are the values.
        */
        ContentValues values = new ContentValues();

        /*
        because I specified product name column to has an UNIQUE value, we can put those specific product values just once,
        so each time we want to put new entry in database we have to check if it is already present
        and if so, just change it quantity to higher, and only if its not, add it as a new row
        */
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Foo");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 235);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 10);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "Bar");
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, "000-111-222");

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // open database in read mode to get old quantity value
        SQLiteDatabase readDb = mDbHelper.getReadableDatabase();

        // Insert a new row for product in the database, returning the ID of that new row.
        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        /*
        check if product is already in the database (if not, newRowId will be equal to -1).
        unfortunately its not 100% foolproof yet, because if someone will put wrong SQL query
        app will crash, because newRowId value will be -1 ass well, but product won't be present
        in the, and app will throw Exception
        */
        if (newRowId == -1) {

        /*
        construct String[] with arguments passed to the query, such as what rows we want to get
        and what is the where clause for our query, then make query
        */
            String[] newProjection = {InventoryEntry.COLUMN_PRODUCT_QUANTITY};
            String[] whereClause = {(String) values.get(InventoryEntry.COLUMN_PRODUCT_NAME)};

            /*
            try to change quantity of product that is present in the table already (because we
            couldn't add it in new row, so it could either already be in the table, or there was
            problem with the query), if the query was wrong, say to user that there was problem with
            adding product to the table, and if product was in the table, just change it quantity and
            say user about it (Prevention against bug depicted in line 65)
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
        so we can just move to the first row, without while(cursor.moveToNext() loop
        */
                cursor.moveToFirst();

                // get index of column we want, and then value associated with it
                int quantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
                int currentQuantity = cursor.getInt(quantityIndex);
                int newQuantity = (int) values.get(InventoryEntry.COLUMN_PRODUCT_QUANTITY) + currentQuantity;

                // create new ContentValues object with new product quantity
                ContentValues updatedQuantity = new ContentValues();
                updatedQuantity.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                // update table with new value of quantity of the product
                db.update(InventoryEntry.TABLE_NAME, updatedQuantity, InventoryEntry.COLUMN_PRODUCT_NAME + "=?", whereClause);

                // close cursor and tell user quantity of what was updated and by how much
                cursor.close();
                Toast.makeText(this, "Quantity of " + whereClause[0] + " was updated by " + values.get(InventoryEntry.COLUMN_PRODUCT_QUANTITY), Toast.LENGTH_LONG).show();
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(this, "Problem with adding product", Toast.LENGTH_LONG).show();
            }
        } else {

            // If newRowId is different than -1, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "Product saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }

        // close readable database
        readDb.close();
        db.close();

    }

    private Cursor getData() {

        // Create database helper
        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY};

        // Perform a query on the inventory table
        Cursor cursor = db.query(
                InventoryEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order

        // Figure out the index of each column
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        while (cursor.moveToNext()) {

            // Use that index to extract the String or Int value of the word
            // at the current row the cursor is on. I don't use this data anywhere, I'm just showing that i can get it
            int currentID = cursor.getInt(idColumnIndex);
            String currentName = cursor.getString(nameColumnIndex);
            int currentPrice = cursor.getInt(priceColumnIndex);
            int currentQuantity = cursor.getInt(quantityColumnIndex);

            text.append("\n" + currentName + " " + currentPrice + " " + currentQuantity + " " + currentID);
        }

        // cursor.close();  I know that i have to close the cursor, I just can't do that here because
        // then i will return just an empty one and it was specified in project rubric that i have to
        // return it in this function ..
        db.close();
        return cursor;
    }
}