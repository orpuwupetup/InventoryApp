package com.example.orpuwupetup.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.IOException;

public class ProductDetailsActivity extends AppCompatActivity {

    /** Global variables */
    TextView productName, price, quantity, suplierPhoneNumber, suplierName, description;
    ImageView productImage;
    ImageButton incrementQuantity, decrementQuantity;
    Uri productUri;
    String suplierPhoneNumberString;

    final private static int INCREMENT_QUANTITY = 1;
    final private static int DECREMENT_QUANTITY = 2;
    final private static String PRODUCT_URI_KEY = "product_uri";

    boolean isFabClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // try to fetch Uri of the product from the intent, we get at the start of Activity
        try {
            productUri = Uri.parse(getIntent().getExtras().getString(PRODUCT_URI_KEY));
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
        ImageButton dialSuplierButton = findViewById(R.id.call_suplier_button);
        final FloatingActionButton fab = findViewById(R.id.fab);
        final FloatingActionButton deleteProductFab = findViewById(R.id.delete_product_fab);
        final FloatingActionButton editProductFab = findViewById(R.id.edit_product_fab);

        // add animations for the opening and closing of floating action buttons
        final Animation fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        final Animation fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        final Animation editProductFabOpen = AnimationUtils.loadAnimation(this, R.anim.edit_product_fab_open);
        final Animation editProductFabClose = AnimationUtils.loadAnimation(this, R.anim.edit_product_fab_close);
        final Animation deleteProductFabOpen = AnimationUtils.loadAnimation(this, R.anim.delete_product_fab_open);
        final Animation deleteProductFabClose = AnimationUtils.loadAnimation(this, R.anim.delete_product_fab_close);

        // set on click listener to expand or contract "floating menu"
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFabClicked){
                    deleteProductFab.setVisibility(View.VISIBLE);
                    editProductFab.setVisibility(View.VISIBLE);

                    deleteProductFab.startAnimation(deleteProductFabOpen);
                    editProductFab.startAnimation(editProductFabOpen);
                    fab.startAnimation(fabOpen);
                    isFabClicked = true;
                }else{
                    deleteProductFab.startAnimation(deleteProductFabClose);
                    deleteProductFab.setVisibility(View.GONE);

                    editProductFab.startAnimation(editProductFabClose);
                    editProductFab.setVisibility(View.GONE);
                    fab.startAnimation(fabClose);
                    isFabClicked = false;
                }
            }
        });

        // set floating buttons of our "floating menu" to delete product, or go to EditProductActivity
        deleteProductFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
                builder.setMessage(getResources().getString(R.string.delete_product_dialog_message));
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // User clicked the "Yes" button, so delete the product.
                        getContentResolver().delete(productUri, null, null);
                        finish();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                /*
                User clicked the "Cancel" button, so dismiss the dialog
                and continue editing the product.
                */
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }
        });
        editProductFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editDetails = new Intent(ProductDetailsActivity.this, AddProduct.class);
                editDetails.putExtra(PRODUCT_URI_KEY, productUri.toString());
                startActivity(editDetails);
            }
        });

        // fill all the view in the Activity with available information about the currently opened product
        populateViews();

        /*
        set phone button to either dial supplier number, or say to user that we don't have supplier number,
        or provided number is incorrect
        */
        dialSuplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (!suplierPhoneNumberString.equals("") || !suplierPhoneNumberString.isEmpty()) {
                        Intent diallSuplier = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", suplierPhoneNumberString, null));
                        startActivity(diallSuplier);
                    } else {
                        Toast.makeText(ProductDetailsActivity.this, getResources().getString(R.string.toast_message_invalid_supplier_number), Toast.LENGTH_SHORT).show();
                    }
                }catch (NullPointerException e){
                    Toast.makeText(ProductDetailsActivity.this, getResources().getString(R.string.toast_message_no_supplier_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set buttons incrementing and decrementing quantity of currently displayed product
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

    /*
    method for changing quantity of displayed product (either decrement or increment it, according
    to what input method has)
    */
    private void changeQuantity(int whichButton){

        // check former quantity
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

        /*
        if we want to decrement quantity, additionally check if it is not already at 0 (if it is
        tell tp the user that he have nothing to sell)
        */
        if(whichButton == DECREMENT_QUANTITY) {
            if(currentQuantity == 0){
                Toast.makeText(this, getResources().getString(R.string.toast_message_cant_sell_more_nothings_left), Toast.LENGTH_LONG).show();
            }else {
                newQuantity = currentQuantity - 1;
            }
        }else{
            newQuantity = currentQuantity + 1;
        }

        /*
        if new quantity is different than former quantity, update quantity of product in the table
        and display it to the user
        */
        if(currentQuantity != newQuantity){
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
            getContentResolver().update(productUri, values, null, null);
            quantity.setText(String.valueOf(newQuantity));
        }
    }

    // fill views with updated info after user comes back from EditProductActivity
    @Override
    protected void onResume() {
        super.onResume();
        populateViews();
    }

    // method for filling views with information about product, taken from the SQL table
    private void populateViews(){

        // get info about product from table, from row specified by product Uri we got via intent
        String [] projection = {"*"};
        Cursor cursor = getContentResolver().query(productUri,
                projection,
                null,
                null,
                null);
        cursor.moveToFirst();

        // extract all the information from provided cursor
        productName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME)));
        double priceValue = 0.01 * (cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE)));
        String priceString = String.valueOf(priceValue) + " $";
        price.setText(priceString);
        quantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY))));
        suplierPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)));
        suplierName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)));
        description.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION)));
        String productImageUriString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE_URI_STRING));

        // try to get phone number of products supplier, if there is non, tell it to the user
        try {
            suplierPhoneNumberString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER));
        }catch (NullPointerException e){
            Toast.makeText(this, getResources().getString(R.string.toast_message_no_supplier_number), Toast.LENGTH_SHORT).show();
        }

        /*
        try to load image for the product, if it don't have any, or there is problem with getching it,
        display generic image, set in layout
        */
        try {
            AsyncImageLoadingTask loadImageTask = new AsyncImageLoadingTask();
            loadImageTask.execute(Uri.parse(productImageUriString));
        } catch (NullPointerException e){
            Log.d("Loading image", "Product has no image selected (probably.;P), just display generic image for product");
        }
    }

    // inner class used to asynchronous loading of the image (and scaling it down to fit in the View)
    private class AsyncImageLoadingTask extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... uris) {
            if(uris[0] == null || uris.length == 0){
                return null;
            }
            return getBitmapFromUri(uris[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                productImage.setImageBitmap(bitmap);
            }
        }

        private Bitmap getBitmapFromUri(Uri uri) {

            if (uri == null || uri.toString().isEmpty()) {
                return null;
            }

            Bitmap scaled = null;

            try {

                /*
                get Bitmap from Uri, and scale it correctly, both if its wider than taller, and
                taller than wider
                */
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                int imageHeight = imageBitmap.getHeight();
                int imageWidth = imageBitmap.getWidth();

                int newWidthHeight = (int) getResources().getDimension(R.dimen.image_width_and_height);

                if (imageHeight < imageWidth) {
                    scaled = Bitmap.createScaledBitmap(imageBitmap, (imageWidth * newWidthHeight) / imageHeight, newWidthHeight, false);
                } else if (imageHeight > imageWidth) {
                    scaled = Bitmap.createScaledBitmap(imageBitmap, newWidthHeight, (imageHeight * newWidthHeight) / imageWidth, false);
                } else {
                    scaled = Bitmap.createScaledBitmap(imageBitmap, newWidthHeight, newWidthHeight, false);
                }
            }catch (IOException e){
                Log.d("getBitmapFromUri", "Problem loading image");
            }
            return scaled;
        }
    }
}
