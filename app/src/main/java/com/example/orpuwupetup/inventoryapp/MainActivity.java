package com.example.orpuwupetup.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import com.example.orpuwupetup.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView productsList;
    private ProductCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find list view for displaying products
        productsList = findViewById(R.id.list);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "costam");
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 30);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 430);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "sony");

        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);


        Button deletThis = findViewById(R.id.delet_this);
        deletThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteThis();
            }
        });

        adapter = new ProductCursorAdapter(this, null);
        productsList.setAdapter(adapter);
        getLoaderManager().initLoader(1, null, this);
    }

    private void deleteThis(){
        getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        this.recreate();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String [] projection ={InventoryEntry._ID, InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE, InventoryEntry.COLUMN_PRODUCT_QUANTITY};
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }
}