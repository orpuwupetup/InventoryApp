package com.example.orpuwupetup.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddProduct extends AppCompatActivity {

    EditText productName, price, quantity, suplierPhoneNumber, suplierName, description;
    Uri productUri;
    ImageButton changeImage;
    String imageUriString;
    ImageView productImage;
    final private static int PICK_IMAGE = 0;
    final static private int ADD_PRODUCT_ACTIVITY = 3;
    final static private int EDIT_PRODUCT_ACTIVITY = 5;
    private boolean pictureWasPicked, productWasChanged;
    private int currentActivity;


    // TODO: Change so that on up button click, user will be send to the Details
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
        changeImage = findViewById(R.id.change_image_button);
        productImage = findViewById(R.id.product_image);


        // find floating button and set onClickListener on it
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
            }
        });

        /*
        try to get Uri from the intent, if we can it means that we are in EditDetails, and if not,
        we are in AddProductActivity
        */
        try {
            // we are in Edit details
            productUri = Uri.parse(getIntent().getExtras().getString("product_uri"));
            this.setTitle("Edit product details");

            currentActivity = EDIT_PRODUCT_ACTIVITY;

            // get details of the editing product and display them on screen
            String[] projection = {"*"};
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
            String descriptionString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION));
            if (!descriptionString.equals("No description...")) {
                description.setText(descriptionString);
            }

            String currentImageUriString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE_URI_STRING));
            if (!currentImageUriString.equals("") || !currentImageUriString.isEmpty()) {

                AsyncImageLoadingTask asyncLoadTask = new AsyncImageLoadingTask();
                asyncLoadTask.execute(Uri.parse(currentImageUriString));
            }

        } catch (NullPointerException e) {
            if (currentActivity != EDIT_PRODUCT_ACTIVITY) {
                // we are in Add new product activity
                currentActivity = ADD_PRODUCT_ACTIVITY;
                productUri = null;
                this.setTitle("Add new product");
            }
        }

        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getImage;

                if (Build.VERSION.SDK_INT < 19) {
                    getImage = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    getImage = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    getImage.addCategory(Intent.CATEGORY_OPENABLE);
                }
                getImage.putExtra("product_uri", productUri);
                getImage.setType("image/*");
                startActivityForResult(Intent.createChooser(getImage, "Select Picture"), PICK_IMAGE);
            }
        });

        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                productWasChanged = true;
                return false;
            }
        };

        changeImage.setOnTouchListener(mTouchListener);
        productName.setOnTouchListener(mTouchListener);
        price.setOnTouchListener(mTouchListener);
        quantity.setOnTouchListener(mTouchListener);
        description.setOnTouchListener(mTouchListener);
        suplierPhoneNumber.setOnTouchListener(mTouchListener);
        suplierName.setOnTouchListener(mTouchListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {

                    try {
                        /*
                        if the image we picked is correct, we can assign it to the ImageView (but
                        not save it to the product yet)
                        */
                        imageUriString = data.getData().toString();

                        AsyncImageLoadingTask asyncLoadTask = new AsyncImageLoadingTask();
                        asyncLoadTask.execute(Uri.parse(imageUriString));

                        pictureWasPicked = true;

                    } catch (NullPointerException e) {
                        Toast.makeText(this, "Problem fetching image", Toast.LENGTH_SHORT).show();
                        imageUriString = "";
                    }
                }
        }
    }

    private void addProduct() {

        // get all of inputs to the new product
        String productNameString = productName.getText().toString();

        int productPriceInt;

        try {
            // save price as integer (number of cents)
            productPriceInt = (int) (Float.parseFloat(price.getText().toString()) * 100);
        } catch (NumberFormatException e) {
            // if there was no price provided, set it as 0
            productPriceInt = 0;
        }
        int productQuantity;
        try {
            productQuantity = Integer.parseInt(quantity.getText().toString());
        } catch (NumberFormatException e) {
            productQuantity = 0;
        }
        String suplierPhoneNumberString = suplierPhoneNumber.getText().toString();
        String suplierNameString = suplierName.getText().toString();
        String descriptionString = description.getText().toString();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, suplierNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceInt);
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, suplierPhoneNumberString);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);

        if(pictureWasPicked) {
            values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE_URI_STRING, imageUriString);
        }

        /*
        if we are in AddProductActivity, add new product, if we are in EditProductActivity, update
        old product
        */
        if (currentActivity == ADD_PRODUCT_ACTIVITY) {

            String whatIsMissing = "";

            if (suplierNameString.equals("") || suplierNameString.isEmpty()) {

                whatIsMissing = whatIsMissing + "supplier name";
            }

            if (productNameString.equals("") || productNameString.isEmpty()){
                if (!whatIsMissing.equals("")){
                    whatIsMissing = whatIsMissing + " and product name";
                }else{
                    whatIsMissing = whatIsMissing + "product name";
                }
            }

            if (productPriceInt == 0){
                if (!whatIsMissing.equals("")){
                    whatIsMissing = whatIsMissing + " and product price";
                }else{
                    whatIsMissing = whatIsMissing + "product price";
                }
            }

            final ContentValues finalValues = values;

            if(!whatIsMissing.equals("")) {


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Do you want to add product without " + whatIsMissing + "?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Yes" button, so add the product without those values.
                        getContentResolver().insert(InventoryEntry.CONTENT_URI, finalValues);
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        // and continue editing the product.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else{
                getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                finish();
            }

        } else {
            getContentResolver().update(productUri, values, null, null);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(!productWasChanged) {
            super.onBackPressed();
            return;
        }

        showDiscardChangesConfirmationDialog();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                if(currentActivity == ADD_PRODUCT_ACTIVITY){
                    NavUtils.navigateUpFromSameTask(AddProduct.this);
                }else{
                    finish();
                }
                break;
        }
        return true;
    }

    private void showDiscardChangesConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to discard changes?");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private class AsyncImageLoadingTask extends AsyncTask<Uri, Void, Bitmap>{
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
                Log.d("getBitmapFromUri", "problem loading image");
            }

            return scaled;
        }
    }
}