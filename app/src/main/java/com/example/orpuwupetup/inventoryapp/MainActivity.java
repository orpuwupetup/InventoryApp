package com.example.orpuwupetup.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        Button deletThis = findViewById(R.id.delet_this);
        deletThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteThis();
            }
        });
    }

    private void deleteThis(){
        getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        this.recreate();
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
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "philips");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 235);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 69);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "Bar");
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, "000-111-222");

        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
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
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI, projection, null, null, null);

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