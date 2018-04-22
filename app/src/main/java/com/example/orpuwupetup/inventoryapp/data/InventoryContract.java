package com.example.orpuwupetup.inventoryapp.data;

import android.provider.BaseColumns;

/**
 * Created by cezar on 22.04.2018.
 */

// contract class for our shop database
public final class InventoryContract {

    // private constructor, so that no one will make instance of our class
    private InventoryContract(){}

    // inner class depicting single table in the database (this one containing inventory of the shop)
    public static final class InventoryEntry implements BaseColumns{

    // used in app constants associated with the inventory table
        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_NAME = "product_name";

        public final static String COLUMN_PRODUCT_PRICE = "price";

        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";

        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
    }
}
