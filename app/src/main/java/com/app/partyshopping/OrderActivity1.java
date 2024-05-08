package com.app.partyshopping;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fevziomurtekin.payview.Payview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class OrderActivity1 extends AppCompatActivity {

    private TextView orderIdTextView, price, title;
    private TextInputLayout addressEditText, stateEditText, countryEditText, contactEditText;
    private Button confirmButton;
    private int itemCount = 0;

    private Payview payview;


    private DatabaseReference ordersRef;
    private FirebaseAuth mAuth;

    private ArrayList<String> itemIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        orderIdTextView = findViewById(R.id.order_id_text_view);
        price = findViewById(R.id.price_text_view);
        title = findViewById(R.id.title);
        addressEditText = findViewById(R.id.address_edit_text);
        stateEditText = findViewById(R.id.state_edit_text);
        countryEditText = findViewById(R.id.country_edit_text);
        contactEditText = findViewById(R.id.contact_edit_text);
        confirmButton = findViewById(R.id.btn_confirm);

        mAuth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        itemIds = new ArrayList<>();

        itemIds = getIntent().getStringArrayListExtra("itemId");
        if (itemIds != null && !itemIds.isEmpty()) {
            fetchItemDetails(itemIds); // Call the method to fetch item details
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentOptionsPopup();
            }
        });
    }

    private void fetchItemDetails(ArrayList<String> itemIds) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

        // Reset itemCount to 0 before fetching details for new items
        itemCount = 0;

        for (String itemId : itemIds) {
            // Check each node (Cakes, Flowers, Stages design) for the item ID
            itemsRef.child("Fashion").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Item found in Cakes node
                        processItemDetails(snapshot);
                    } else {
                        // Item not found in Cakes node, check Flowers node
                        itemsRef.child("Apple Products").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Item found in Flowers node
                                    processItemDetails(snapshot);
                                } else {
                                    // Item not found in Flowers node, check Stages design node
                                    itemsRef.child("Household Items").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                // Item found in Stages design node
                                                processItemDetails(snapshot);
                                            } else {
                                                Toast.makeText(OrderActivity1.this, "Item not found for ID: " + itemId, Toast.LENGTH_SHORT).show();
                                                itemCount++; // Increment counter even if item not found
                                                checkItemCount(); // Check if all items have been fetched
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(OrderActivity1.this, "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(OrderActivity1.this, "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(OrderActivity1.this, "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void processItemDetails(DataSnapshot snapshot) {
        String itemName = snapshot.child("title").getValue(String.class);
        String priceStr = snapshot.child("price").getValue(String.class);

        if (itemName != null && priceStr != null) {
            try {
                Double itemPrice = Double.parseDouble(priceStr);
                title.append("\nName: " + itemName);
                price.append("\nPrice: SR" + itemPrice);
            } catch (NumberFormatException e) {
                Toast.makeText(OrderActivity1.this, "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(OrderActivity1.this, "Item details not found", Toast.LENGTH_SHORT).show();
        }

        itemCount++; // Increment counter after fetching item details
        checkItemCount(); // Check if all items have been fetched
    }

    private void checkItemCount() {
        if (itemCount == itemIds.size()) {
            updateUI();
        }
    }

    private void updateUI() {
              confirmButton.setVisibility(View.VISIBLE);
    }

    private void showPaymentOptionsPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_payment_options, null);

        // Initialize popup window components
        Button cashOnDeliveryButton = popupView.findViewById(R.id.btn_cash_on_delivery);
        Button cardPaymentButton = popupView.findViewById(R.id.btn_card_payment);

        // Create the popup window
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Set click listeners for buttons
        cashOnDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss(); // Dismiss the popup
                placeOrder(); // Place order with Cash on Delivery option
            }
        });

        cardPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss(); // Dismiss the popup
                showCardPaymentPopup(); // Show card payment popup
            }
        });

        // Show the popup window
        popupWindow.showAsDropDown(confirmButton);
    }

    private void showCardPaymentPopup() {
        // Inflate the layout for the card payment popup
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_card_payment, null);

        // Initialize views from the popup layout
        payview = popupView.findViewById(R.id.payview);

        // Create an AlertDialog builder and set the view to the inflated popup layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(popupView)
                .setCancelable(false); // Optional: Set whether the dialog is cancelable

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Set a click listener for the payment button
        payview.setPayOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });

        // Show the AlertDialog
        dialog.show();
    }

    private void placeOrder() {
        String address = addressEditText.getEditText().getText().toString().trim();
        String state = stateEditText.getEditText().getText().toString().trim();
        String country = countryEditText.getEditText().getText().toString().trim();
        String contact = contactEditText.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(state) || TextUtils.isEmpty(country) || TextUtils.isEmpty(contact)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                String userEmail = user.getEmail();
                String username = user.getDisplayName();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String dateTime = dateFormat.format(calendar.getTime());

                // Generate a single orderId for all itemIds
                String orderId = ordersRef.push().getKey();
                if (orderId != null) {
                    String orderStatus = "In Queue";

                    // Combine all itemIds into a single string separated by commas
                    String allItemIds = TextUtils.join(",", itemIds);

                    Order order = new Order(userId, userEmail, username, allItemIds, address, state, country, contact, dateTime, orderId, orderStatus);

                    ordersRef.child(orderId).setValue(order)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");
                                    historyRef.child(orderId).setValue(order)
                                            .addOnCompleteListener(historyTask -> {
                                                if (historyTask.isSuccessful()) {
                                                    Toast.makeText(OrderActivity1.this, "Order placed successfully", Toast.LENGTH_SHORT).show();

                                                    // Step 4: Delete all cart items after order is placed successfully
                                                    deleteAllCartItems(userEmail);

                                                    // Navigate to MainActivity
                                                    navigateToMainActivity();
                                                } else {
                                                    Toast.makeText(OrderActivity1.this, "Failed to save order in history", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(OrderActivity1.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }
    }

    private void deleteAllCartItems(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference cartRef = db.collection("Cart").document(userEmail);

        cartRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                     //   Toast.makeText(OrderActivity1.this, "All cart items deleted after order", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                   //     Toast.makeText(OrderActivity1.this, "Failed to delete cart items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void navigateToMainActivity() {
        Intent intent = new Intent(OrderActivity1.this, MainActivity.class);
        startActivity(intent);
        finish(); // Optional: Finish OrderActivity1 to prevent going back to it from MainActivity
    }
}