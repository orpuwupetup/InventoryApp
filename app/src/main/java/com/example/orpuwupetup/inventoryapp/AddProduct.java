package com.example.orpuwupetup.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.orpuwupetup.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddProduct extends AppCompatActivity {

    final private static int PICK_IMAGE = 0;
    final static private int ADD_PRODUCT_ACTIVITY = 3;
    final static private int EDIT_PRODUCT_ACTIVITY = 5;
    private final static int TAKE_PICTURE = 1;
    /**
     * Global variables and constants
     */
    EditText productName, price, quantity, suplierPhoneNumber, suplierName, description;
    Uri productUri;
    ImageButton chooseImageFromGallery, chooseImageFromCamera, chooseImageSrc, closeImageOptions;
    String imageUriString;
    ImageView productImage;
    Uri photoPath;
    private boolean pictureWasPicked, productWasChanged, imageSrcOptionsOpen;
    private int currentActivity;

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
        chooseImageFromGallery = findViewById(R.id.choose_image_from_gallery);
        chooseImageFromCamera = findViewById(R.id.choose_image_from_camera);
        productImage = findViewById(R.id.product_image);
        chooseImageSrc = findViewById(R.id.choose_image_source);
        closeImageOptions = findViewById(R.id.close_image_options);

        /*
        find floating button and set onClickListener on it to save product to the list (or update
        its quantity if its already in the table)
        */
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
            productUri = Uri.parse(getIntent().getExtras().getString("product_uri"));

            // we are in Edit details, set correct title of the activity and set global variable for currentActivity
            this.setTitle("Edit product details");
            currentActivity = EDIT_PRODUCT_ACTIVITY;

            // get details of the editing product and display them on screen
            String[] projection = {"*"};
            Cursor cursor = getContentResolver().query(productUri,
                    projection,
                    null,
                    null,
                    null);

            // we get just one product in cursor, so we can just go to the first element
            cursor.moveToFirst();
            productName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME)));

            // display price after converting it from cents to dollars
            String priceString = String.valueOf(0.01 * cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE)));
            price.setText(priceString);

            // display all information about the product
            quantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY))));
            suplierName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)));
            suplierPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)));
            String descriptionString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION));
            if (!descriptionString.equals("No description...")) {
                description.setText(descriptionString);
            }
            String currentImageUriString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE_URI_STRING));
            if (!currentImageUriString.equals("") || !currentImageUriString.isEmpty()) {

                // if this product has set image, display it asynchronously
                AsyncImageLoadingTask asyncLoadTask = new AsyncImageLoadingTask();
                asyncLoadTask.execute(Uri.parse(currentImageUriString));
            }
        } catch (NullPointerException e) {
            if (currentActivity != EDIT_PRODUCT_ACTIVITY) {
                // we are in Add new product activity so we don't have to load any information, just set tile
                currentActivity = ADD_PRODUCT_ACTIVITY;
                productUri = null;
                this.setTitle("Add new product");
            }
        }

        imageSrcOptionsOpen = false;
        final Animation cameraButtonOpen = AnimationUtils.loadAnimation(this, R.anim.image_from_camera_open);
        final Animation galleryButtonOpen = AnimationUtils.loadAnimation(this, R.anim.image_from_gallery_open);
        final Animation cameraButtonClose = AnimationUtils.loadAnimation(this, R.anim.image_from_camera_close);
        final Animation galleryButtonClose = AnimationUtils.loadAnimation(this, R.anim.image_from_gallery_close);
        final Animation chooseImageSrcOpen = AnimationUtils.loadAnimation(this, R.anim.choose_image_src_button_open);
        final Animation chooseImageSrcClose = AnimationUtils.loadAnimation(this, R.anim.choose_image_src_button_close);

        /*
        set on click listener on chooseImageSrc button to show options for sources from which we
        can choose
        */
        chooseImageSrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!imageSrcOptionsOpen){

                    chooseImageSrc.startAnimation(chooseImageSrcClose);
                    chooseImageFromCamera.startAnimation(cameraButtonOpen);
                    chooseImageFromGallery.startAnimation(galleryButtonOpen);
                    closeImageOptions.startAnimation(chooseImageSrcOpen);

                    chooseImageFromGallery.setVisibility(View.VISIBLE);
                    chooseImageFromCamera.setVisibility(View.VISIBLE);
                    closeImageOptions.setVisibility(View.VISIBLE);
                    chooseImageSrc.setVisibility(View.GONE);

                    imageSrcOptionsOpen = true;
                }
            }
        });

        // onClickListener for closing options for choosing image src
        closeImageOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageSrcOptionsOpen){
                    chooseImageFromGallery.startAnimation(galleryButtonClose);
                    chooseImageFromCamera.startAnimation(cameraButtonClose);
                    chooseImageSrc.startAnimation(chooseImageSrcOpen);
                    closeImageOptions.startAnimation(chooseImageSrcClose);

                    chooseImageFromCamera.setVisibility(View.GONE);
                    chooseImageFromGallery.setVisibility(View.GONE);
                    chooseImageSrc.setVisibility(View.VISIBLE);
                    closeImageOptions.setVisibility(View.GONE);
                    imageSrcOptionsOpen = false;
                }
            }
        });

        // set on click listener to choose image for the product from camera
        chooseImageFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if camera and/or any camera app is available
                if (hasCamera() && hasDefualtCameraApp(MediaStore.ACTION_IMAGE_CAPTURE)) {

                    // make intent for taking, and saving photo
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            photoPath = FileProvider.getUriForFile(AddProduct.this,
                                    "com.example.android.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoPath);
                            startActivityForResult(takePictureIntent, TAKE_PICTURE);
                        }
                    }
                } else {
                    Toast.makeText(AddProduct.this, getResources().getString(R.string.toast_message_no_camera_hardware_or_software)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set on click listener to choose image for the product from gallery
        chooseImageFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getImage;

                // make new intent, accordingly to what Android version user has
                if (Build.VERSION.SDK_INT < 19) {
                    getImage = new Intent(Intent.ACTION_GET_CONTENT);
                } else {

                    /*
                    there is problem in Android versions up from KitKat, where apps don't keep
                    permissions for displaying images, so I have to add flag with persistable permission
                    to every image Uri that I've got
                    */
                    getImage = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    getImage.addCategory(Intent.CATEGORY_OPENABLE);
                    getImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    getImage.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }

                /*
                put Uri in intent extra for checking in which activity we currently are, and set this
                intent to get all types of image extensions
                */
                getImage.putExtra("product_uri", productUri);
                getImage.setType("image/*");
                getImage.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                getImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(getImage, "Select Picture"), PICK_IMAGE);
            }
        });

        /*
        make on touch listener to listen for touching any of the Views associated with changing
        product info, and if they were touched, change productWasChanged to true
        */
        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                productWasChanged = true;
                return false;
            }
        };
        chooseImageFromGallery.setOnTouchListener(mTouchListener);
        chooseImageFromCamera.setOnTouchListener(mTouchListener);
        productName.setOnTouchListener(mTouchListener);
        price.setOnTouchListener(mTouchListener);
        quantity.setOnTouchListener(mTouchListener);
        description.setOnTouchListener(mTouchListener);
        suplierPhoneNumber.setOnTouchListener(mTouchListener);
        suplierName.setOnTouchListener(mTouchListener);
    }

    // method for adding taken photo to the gallery to make it available for the other apps
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath.toString());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // method for creating new Image file from photo taken by camera
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = Uri.parse(image.getAbsolutePath());
        return image;
    }

    // method to check if user have a Camera
    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // method to check user have Camera Apps
    private boolean hasDefualtCameraApp(String action) {
        final PackageManager packageManager = getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            // after choosing image via FileManager:
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    try {
                        /*
                        get permission for displaying the image (after device was restarted, and we
                        want to display image from before the restart)
                        */
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            ContentResolver resolver = this.getContentResolver();
                            if (data.getData() != null) {
                                resolver.takePersistableUriPermission(data.getData(), takeFlags);
                            }
                        }
                        imageUriString = data.getData().toString();
                        Log.d("imageUrifromgallery", imageUriString);

                        /*
                        if the image we picked is correct, we can assign it to the ImageView (but
                        not save it to the product yet)
                        */
                        AsyncImageLoadingTask asyncLoadTask = new AsyncImageLoadingTask();
                        asyncLoadTask.execute(Uri.parse(imageUriString));
                        pictureWasPicked = true;

                    } catch (NullPointerException e) {
                        Toast.makeText(this, getResources().getString(R.string.toast_message_problem_fetching_image), Toast.LENGTH_SHORT).show();
                        imageUriString = "";
                    }
                }
                break;

            /*
            if we taken picture with device camera, show it in the ImageView, and set imageUriString
            of the product
            */
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {

                    galleryAddPic();
                    AsyncImageLoadingTask asyncLoadTask = new AsyncImageLoadingTask();
                    asyncLoadTask.execute(photoPath);
                    imageUriString = photoPath.toString();
                    galleryAddPic();
                    pictureWasPicked = true;
                }
                break;
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

        /*
        make new ContentValues with all the product information that we got from user, to send it
        via query to the ContentProvider
        */
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, suplierNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceInt);
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, suplierPhoneNumberString);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);

        if (pictureWasPicked) {
            values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE_URI_STRING, imageUriString);
        }

        /*
        if we are in AddProductActivity, add new product, if we are in EditProductActivity, update
        old product
        */
        if (currentActivity == ADD_PRODUCT_ACTIVITY) {

            /*
            we have to make (grammatically correct) String, accordingly to which part of information user
            is not yet providing, to tell it to him in the Toast message
            */
            String whatIsMissing = "";
            int missingValues = 0;
            if (suplierNameString.equals("") || suplierNameString.isEmpty()) {

                whatIsMissing = whatIsMissing + " " + getResources().getString(R.string.supplier_name_lower_case);
                missingValues++;
            }
            if (productNameString.equals("") || productNameString.isEmpty()) {
                if (!whatIsMissing.equals("")) {
                    whatIsMissing = whatIsMissing + " " + getResources().getString(R.string.and_product_name);
                    missingValues++;
                } else {
                    whatIsMissing = whatIsMissing + " " + getResources().getString(R.string.product_name_lower_case);
                }
            }
            if (productPriceInt == 0) {
                if (!whatIsMissing.equals("")) {
                    whatIsMissing = whatIsMissing + " " + getResources().getString(R.string.and_product_price);
                    missingValues++;
                } else {
                    whatIsMissing = whatIsMissing + " " + getResources().getString(R.string.product_price_lower_case);
                }
            }
            if (missingValues == 3) {
                whatIsMissing = whatIsMissing.replaceFirst(" " + getResources().getString(R.string.and), ",");
            }

            /*
            make final instance of ContentValues (because we want to use it in the inner class, but
            but don't change its values there)
            */
            final ContentValues finalValues = values;

            /*
            if some information is missing, tell it to the user via Dialog, otherwise just insert
            product to the table
            */
            if (!whatIsMissing.equals("")) {


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.add_product_without_something_dialog_question) + whatIsMissing + "?");
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // User clicked the "Yes" button, so add the product without those values.
                        getContentResolver().insert(InventoryEntry.CONTENT_URI, finalValues);
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
            } else {
                getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                finish();
            }

            /*
            if we are in EditDetails activity, either update the product, or say to the user that nothing
            has changed
            */
        } else {

            if (productWasChanged) {
                getContentResolver().update(productUri, values, null, null);
                Toast.makeText(this, getResources().getString(R.string.toast_message_details_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.toast_message_no_info_updated), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    /*
    if info changed, and user clicks back button, ask him for confirmation of discarding of provided
    changes, else just do what back button usually does
    */
    @Override
    public void onBackPressed() {
        if (!productWasChanged) {
            super.onBackPressed();
            return;
        }

        showDiscardChangesConfirmationDialog();

    }

    /*
    if user clicks back button from AddProductActivity, send him to inventory, if he is in
    EditProductActivity, send him to details of the product being edited (or ask him for discard
    confirmation, if changes were provided)
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (!productWasChanged) {
                    if (currentActivity == ADD_PRODUCT_ACTIVITY) {
                        NavUtils.navigateUpFromSameTask(AddProduct.this);
                    } else {
                        finish();
                    }
                } else {
                    showDiscardChangesConfirmationDialog();
                }
                break;
        }
        return true;
    }

    // display dialog to discard changes, or cancel discardment
    private void showDiscardChangesConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.discard_confirmation_dialog_question));
        builder.setPositiveButton(getResources().getString(R.string.discard), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Discard" button, so discard the changes.
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

    /*
    Inner class extending AsyncTask, used for loading image 'in the background' and display it
    after load is completed
    */
    private class AsyncImageLoadingTask extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... uris) {
            if (uris[0] == null || uris.length == 0) {
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

        // get image from the image Uri and scale it down to fit in the boarders of the ImageView
        private Bitmap getBitmapFromUri(Uri uri) {

            if (uri == null || uri.toString().isEmpty()) {
                return null;
            }

            Log.d("getbitmapfromuri", uri.toString());
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
            } catch (IOException e) {
                Log.d("getBitmapFromUri", "problem loading image");
            }
            return scaled;
        }
    }
}