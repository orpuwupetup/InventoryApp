package com.example.orpuwupetup.inventoryapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;

/**
 * Created by cezar on 22.04.2018.
 */

// contract class for our shop database
public final class InventoryContract {

    // content authority (provide name) for our app
    public static final String CONTENT_AUTHORITY = "com.example.orpuwupetup.inventoryapp";

    // Base content URI for all the URIs associated with the app
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path for the inventory table
    public static final String PATH_INVENTORY = "inventory";



    // private constructor, so that no one will make instance of our class
    private InventoryContract(){}

    // inner class depicting single table in the database (this one containing inventory of the shop)
    public static final class InventoryEntry implements BaseColumns{

        // Full URI for the inventory table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        // MIME types of URIs for single product, and for whole list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

    // used in app constants associated with the inventory table
        // TODO: Add description and imageUriString columns to the table
        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_DESCRIPTION = "product_description";

        public final static String COLUMN_PRODUCT_IMAGE_URI_STRING = "image_uri_string";

        public final static String COLUMN_PRODUCT_NAME = "product_name";

        public final static String COLUMN_PRODUCT_PRICE = "price";

        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";

        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
    }
}
