package com.example.orpuwupetup.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

public class AddProduct extends AppCompatActivity {

    EditText productName, price, quantity, suplierPhoneNumber, suplierName, description;
    Uri productUri;


    // TODO: Change so that on back button click or on up button click, user will be send to the Details
    // TODO: Activity (if he is in the EditProduct Activity) and to Main if he is in AddProductActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);


        // find all TextViews associated with adding new product
        productName = findViewById(R.id.product_name);
        price = findViewById(R.id.product_price);
        quantity = findViewById(R.id.product_quantity);
        suplierPhoneNumber = findViewById(R.id.phone_number_edit_text);
        suplierName = findViewById(R.id.suplier_name_edit_text);
        description = findViewById(R.id.description_edit_text);

        // find floating button and set onClickListener on it
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
                finish();
            }
        });

        /*
        try to get Uri from the intent, if we can it means that we are in EditDetails, and if not,
        we are in AddProductActivity
        */
        try {
            // Edit details
            productUri = Uri.parse(getIntent().getExtras().getString("product_uri"));
            this.setTitle("Edit product details");

            // get details of the editing product and display them on screen
            String [] projection = {"*"};
            Cursor cursor = getContentResolver().query(productUri,
                    projection,
                    null,
                    null,
                    null);
            cursor.moveToFirst();
            productName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME)));
            String priceString = String.valueOf(0.01 * cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE)));
            price.setText(priceString);
            quantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY))));
            suplierName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)));
            suplierPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)));

            //TODO: Add description and image to the displayed details

        }catch (NullPointerException e){
            // Add new product
            productUri = null;
            this.setTitle("Add new product");
        }
    }

    private void addProduct(){

        // get all of inputs to the new product
        String productNameString = productName.getText().toString();

        int productPriceInt;

        try {
            // save price as integer (number of cents)
            productPriceInt = (int) (Float.parseFloat(price.getText().toString()) * 100);
        }catch (NumberFormatException e){
            // if there was no price provided, set it as 0
            productPriceInt = 0;
        }
        int productQuantity;
        try {
            productQuantity = Integer.parseInt(quantity.getText().toString());
        }catch (NumberFormatException e){
            productQuantity = 0;
        }
        String suplierPhoneNumberString = suplierPhoneNumber.getText().toString();
        String suplierNameString = suplierName.getText().toString();
        String descriptionString = description.getText().toString();
        // TODO: Add method to get ImageUriString to image chosen from gallery
        // TODO: Add description to the table

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, suplierNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceInt);
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, suplierPhoneNumberString);

        /*
        if we are in AddProductActivity, add new product, if we are in EditProductActivity, update
        old product
        */
        if(productUri == null) {
            getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        }else{
            getContentResolver().update(productUri, values, null, null);
        }

    }
}
