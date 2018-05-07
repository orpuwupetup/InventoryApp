# InventoryApp
<p align="center">
<img src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/parcel_icon_small.png" width=400>
</p>
<p align="center">
Mobile application for inventory management (with SQL database)
</p>

<p>

## Main screen of the app

<img align="right" src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/empty_view_list.png" height=200> <img align="right" src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/list_with_items.png" height=200>

 Main screen consists of list build on the ListView, with info provided from SQL database via ContentProvider (to prevent SQL injections). 
It is showing all the products that we have in our database (just some of information about it: product name, price and quantity). It contains 
sale button for each of the products, with which we can decrement quantity of the product it corresponds to by one, and floating action button,
which is sending the user to the AddNewProductActivity.  
</p>

<p>

## AddProduct and EditProduct activities

<img align="left" src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/add_new_product.png" height=200> <img align="left" src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/edit_product_with_expanded_options.png" height=200>

EditProductActivity and AddNewProductActivity are both build on single layout.xml file, becouse they are quite similar, both in look and in functionality.
If user is in AddNewProduct activity (after clicking floating action button), all EditText views, are filled with default hints, specifying
what to write down inside of each. If user is in EditProductActivity, EditText hints are filled with already provided pieces of information about the products.
Layouts has floating action menu button which expands options from where to choose new (or update former) image, either from camera, or
from the gallery. User is prompted to check his input (if he forgot to provide any piece of information) via dialog.
</p>

<p>

## ProductDetailsActivity

<img align="right" src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/details_expanded_options.png" height=200> <img align="right" src="https://raw.githubusercontent.com/orpuwupetup/InventoryApp/master/Screenshots/details_folded_options.png" height=200> 

Details activity, as the name is implying, is showing details of the chosen product. It has buttons for calling supplier of our product 
(if we put in any number), incrementing and decrementing quantity of the product, and floating action button which, after click, expands into
two other buttons, one for deleting the product from the table, and other for going to the EditDetailsActivity. All the informations in the
layout has their default value if user haven't provided any of them, additionally, app hints the user (via red color of the value, and 
specific String) that the value was not provided at the initial creation of the product.

</p>


