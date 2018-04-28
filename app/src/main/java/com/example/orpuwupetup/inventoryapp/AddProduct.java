package com.example.orpuwupetup.inventoryapp;

import android.content.ContentValues;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

public class AddProduct extends AppCompatActivity {

    EditText productName, price, quantity, suplierPhoneNumber, suplierName, description;

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

        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

    }
}
