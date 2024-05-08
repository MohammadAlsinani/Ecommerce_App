package com.app.partyshopping;

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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity {

    private TextView orderIdTextView, price, title;
    private TextInputLayout addressEditText, stateEditText, countryEditText, contactEditText;
    private Button confirmButton;
    private Payview payview;

    private DatabaseReference ordersRef;
    private FirebaseAuth mAuth;

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

        // Get the itemId passed from the previous activity
        String itemId = getIntent().getStringExtra("itemId");
        if (itemId != null) {
            orderIdTextView.setText(itemId);

            // Fetch item name and price from Firebase based on itemId
            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");
            itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        for (DataSnapshot itemSnapshot : categorySnapshot.getChildren()) {
                            if (itemSnapshot.getKey().equals(itemId)) {
                                Item item = itemSnapshot.getValue(Item.class);
                                if (item != null) {
                                    found = true;
                                    // Set the title and price in the TextViews
                                    title.setText("Name: " + item.getTitle());
                                    price.setText("Price: SR" + item.getPrice());
                                    break;
                                }
                            }
                        }
                        if (found) {
                            break;
                        }
                    }

                    if (!found) {
                        Toast.makeText(OrderActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(OrderActivity.this, "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentOptionsPopup();
            }
        });
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

                // Get current date and time
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String dateTime = dateFormat.format(calendar.getTime());

                // Generate a unique key for the order in the "Orders" node
                String orderId = ordersRef.push().getKey();

                if (orderId != null) {
                    // Set initial order status
                    String orderStatus = "In Queue";

                    // Create a new Order object with the required fields including date and time
                    Order order = new Order(userId, userEmail, username, orderIdTextView.getText().toString(), address, state, country, contact, dateTime, orderId, orderStatus);

                    // Set the order data in the "Orders" node under the generated orderId
                    ordersRef.child(orderId).setValue(order)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(OrderActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                                    DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");
                                    historyRef.child(orderId).setValue(order)
                                            .addOnCompleteListener(historyTask -> {
                                                if (historyTask.isSuccessful()) {
                                                    Toast.makeText(OrderActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();




                                                } else {
                                                    Toast.makeText(OrderActivity.this, "Failed to save order in history", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    finish();
                                } else {
                                    Toast.makeText(OrderActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Failed to generate orderId", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

