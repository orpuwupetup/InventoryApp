package com.example.orpuwupetup.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import com.example.orpuwupetup.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView productsList;
    private ProductCursorAdapter adapter;

    // TODO: Make app pretty
        // TODO: Add icons to floating buttons
        // TODO: Add animation to floating buttons
        // TODO: Change font colors in the main list
        // TODO: Change font colors in the details activity
        // TODO: Add emptyView to the main list
        // TODO: Add default picture of the product
        // TODO: maybe change layout of the Details activity a little (blue views under "price" and "quantity" headers? blue circle around productImage?)
        // TODO: Add icons to buttons
            // TODO: Choosing Image
            // TODO: Sale and increment and decrement of quantity


    // TODO: Add comments and clean the code
        // TODO: Exctract Strings resources
        // TODO: Clean code of unused methods and white spaces
        // TODO: Add more comments in correct places

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find list view for displaying products
        productsList = findViewById(R.id.list);

        //Find floating action button, and set intent on it to open AddProduct Activity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openAddProduct = new Intent(MainActivity.this, AddProduct.class);
                startActivity(openAddProduct);
            }
        });

        adapter = new ProductCursorAdapter(this, null);
        productsList.setAdapter(adapter);
        getLoaderManager().initLoader(1, null, this);

        productsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // create URI for clicked pet
                Uri productUri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, String.valueOf(id));

                Intent openDetails = new Intent(MainActivity.this, ProductDetailsActivity.class);
                openDetails.putExtra("product_uri", productUri.toString());
                startActivity(openDetails);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        Inflate the menu options from the res/menu/menu_inventory_list.xml file.
        This adds menu items to the app bar.
        */
        getMenuInflater().inflate(R.menu.menu_inventoy_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all products" menu option
            case R.id.action_delete_all_entries:
                deleteThis();
                Toast.makeText(this, "All products deleted from the list", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteThis(){
        getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
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