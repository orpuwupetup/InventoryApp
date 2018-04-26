package com.example.orpuwupetup.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by cezar on 26.04.2018.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public  ProductCursorAdapter (Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // inflate and return new view for item of the list
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // get views of the product item
        TextView productName = view.findViewById(R.id.product_name);
        TextView productQuantity = view.findViewById(R.id.product_quantity);
        TextView productPrice = view.findViewById(R.id.product_price);
        Button saleButton = view.findViewById(R.id.sale_button);

        // set text of the views to correct depiction of the product
        final String nameString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME));
        final String quantityString = String.valueOf(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY)));
        final String priceString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE));
        productName.setText(nameString);
        productQuantity.setText(quantityString);
        productPrice.setText(priceString);

        // set on click listener on saleButton to decrement quantity of product by one
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("list item", "update quantity of products");
            }
        });
    }
}
