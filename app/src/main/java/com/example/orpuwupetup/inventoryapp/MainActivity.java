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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import com.example.orpuwupetup.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Global variables */
    private ProductCursorAdapter adapter;
    private ImageView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find list view for displaying products (and its empty view)
        ListView productsList = findViewById(R.id.list);
        emptyView = findViewById(R.id.empty_list_view);
        productsList.setEmptyView(emptyView);

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

        /*
        set onClickListener so that clicking on the item from the list will send user to the Activity
        with details of the clicked product
        */
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
                Toast.makeText(this, getResources().getString(R.string.toast_message_all_items_deleted), Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // method for deleting all products from the list
    private void deleteThis(){
        getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
    }

    // methods associated with creating CursorLoader object, and using it to fill the products list
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
        emptyView.setBackgroundResource(R.drawable.empty_carton_box_without_borders);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }
}