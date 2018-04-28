package com.example.orpuwupetup.inventoryapp;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

public class ProductDetailsActivity extends AppCompatActivity {

    TextView productName, price, quantity, suplierPhoneNumber, suplierName, description;
    ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        Uri productUri;
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

        populateViews(productUri);

    }

    private void populateViews(Uri uri){


        // TODO: Display description and product image
        String [] projection = {"*"};
        Cursor cursor = getContentResolver().query(uri,
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
