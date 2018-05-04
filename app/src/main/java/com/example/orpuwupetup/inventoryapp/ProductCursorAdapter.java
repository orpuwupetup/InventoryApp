package com.example.orpuwupetup.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by cezar on 26.04.2018.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /** Global variables */
    public  ProductCursorAdapter (Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // inflate and return new view for item of the list
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // get views of the product item
        TextView productName = view.findViewById(R.id.product_name);
        TextView productQuantity = view.findViewById(R.id.product_quantity);
        TextView productPrice = view.findViewById(R.id.product_price);
        Button saleButton = view.findViewById(R.id.sale_button);

        // set text of the views to correct depiction of the product
        final String nameString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME));
        final String quantityString = String.valueOf(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY))) + " pcs";
        final String priceString = String.valueOf(0.01 * cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE))) + " $";
        productName.setText(nameString);
        productQuantity.setText(quantityString);
        productPrice.setText(priceString);

        final String[] selectionArgs = {nameString};
        final String idString = cursor.getString(cursor.getColumnIndex(InventoryEntry._ID));

        // set on click listener on saleButton to decrement quantity of product by one
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                check quantity of the product that we have in inventory, if it's bigger than zero,
                decrement it by one, if nothings left, tell about it to the user
                */
                    // get current quantity of the product
                String[] projection = {InventoryEntry.COLUMN_PRODUCT_QUANTITY};

                Cursor cursor1 = context.getContentResolver().query(
                        Uri.withAppendedPath(InventoryEntry.CONTENT_URI, idString),
                        projection,
                        InventoryEntry.COLUMN_PRODUCT_NAME+"=?",
                        selectionArgs,
                        null);
                cursor1.moveToFirst();
                int quantity = cursor1.getInt(cursor1.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY));

                    // if bigger than 1 'sell' one product (decrement quantity by 1)
                if ( quantity >= 1){
                    ContentValues values1 = new ContentValues();
                    values1.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity-1);
                    context.getContentResolver().update(Uri.withAppendedPath(InventoryEntry.CONTENT_URI, idString),
                            values1,
                            InventoryEntry.COLUMN_PRODUCT_NAME+"=?",
                            selectionArgs);

                    // else, notify the user with toast message
                }else{
                    Toast.makeText(context, context.getResources().getString(R.string.toast_message_cant_sell_more_nothings_left), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
