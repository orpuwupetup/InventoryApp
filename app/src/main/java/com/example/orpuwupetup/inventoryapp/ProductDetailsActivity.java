package com.example.orpuwupetup.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

public class ProductDetailsActivity extends AppCompatActivity {

    TextView productName, price, quantity, suplierPhoneNumber, suplierName, description;
    ImageView productImage;
    ImageButton incrementQuantity, decrementQuantity;
    Uri productUri;

    final private static int INCREMENT_QUANTITY = 1;
    final private static int DECREMENT_QUANTITY = 2;

    boolean isFabClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);


        try {
            productUri = Uri.parse(getIntent().getExtras().getString("product_uri"));
        }catch (NullPointerException e){
            productUri = null;
        }

        // find all views for the product details
        productName = findViewById(R.id.product_name);
        price = findViewById(R.id.product_price);
        quantity = findViewById(R.id.product_quantity);
        suplierPhoneNumber = findViewById(R.id.phone_number);
        suplierName = findViewById(R.id.suplier_name);
        description = findViewById(R.id.description);
        productImage = findViewById(R.id.product_image);
        incrementQuantity = findViewById(R.id.increment_quantity);
        decrementQuantity = findViewById(R.id.decrement_quantity);
        FloatingActionButton fab = findViewById(R.id.fab);
        final FloatingActionButton deleteProductFab = findViewById(R.id.delete_product_fab);
        final FloatingActionButton editProductFab = findViewById(R.id.edit_product_fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFabClicked){
                    deleteProductFab.setVisibility(View.VISIBLE);
                    editProductFab.setVisibility(View.VISIBLE);
                    isFabClicked = true;
                }else{
                    deleteProductFab.setVisibility(View.GONE);
                    editProductFab.setVisibility(View.GONE);
                    isFabClicked = false;
                }
            }
        });

        deleteProductFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add dialog to ask user if he wants to really delete the product or not
                getContentResolver().delete(productUri, null, null);
                finish();
            }
        });

        editProductFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editDetails = new Intent(ProductDetailsActivity.this, AddProduct.class);
                editDetails.putExtra("product_uri", productUri.toString());
                startActivity(editDetails);
            }
        });

        populateViews();

        incrementQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeQuantity(INCREMENT_QUANTITY);
            }
        });

        decrementQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeQuantity(DECREMENT_QUANTITY);
            }
        });

    }

    private void changeQuantity(int whichButton){
        String [] projection = {InventoryEntry.COLUMN_PRODUCT_QUANTITY};
        Cursor cursor = getContentResolver().query(productUri,
                projection,
                null,
                null,
                null);
        cursor.moveToFirst();
        int currentQuantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        int newQuantity = currentQuantity;
        ContentValues values = new ContentValues();
        if(whichButton == DECREMENT_QUANTITY) {
            if(currentQuantity == 0){
                Toast.makeText(this, "Can't sell more, nothings left", Toast.LENGTH_LONG).show();
            }else {
                newQuantity = currentQuantity - 1;
            }
        }else{
            newQuantity = currentQuantity + 1;
        }

        if(currentQuantity != newQuantity){
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
            getContentResolver().update(productUri, values, null, null);
            quantity.setText(String.valueOf(newQuantity));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateViews();
    }

    private void populateViews(){


        // TODO: Display description and product image
        String [] projection = {"*"};
        Cursor cursor = getContentResolver().query(productUri,
                projection,
                null,
                null,
                null);
        cursor.moveToFirst();
        productName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME)));
        double priceValue = 0.01 * (cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE)));
        String priceString = String.valueOf(priceValue) + " $";
        price.setText(priceString);
        quantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY))));
        suplierPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)));
        suplierName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)));
    }
}