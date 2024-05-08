package com.app.partyshopping;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    private ImageView imageView, arrowLeft, arrowRight;
    private TextView titleTextView, priceTextView, descriptionTextView;
    private List<String> imageUrls;
    private int currentImageIndex = 0;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        imageView = findViewById(R.id.image_view);
        titleTextView = findViewById(R.id.title_text_view);
        priceTextView = findViewById(R.id.price_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        arrowLeft = findViewById(R.id.arrow_left);
        arrowRight = findViewById(R.id.arrow_right);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            currentImageIndex = savedInstanceState.getInt("currentImageIndex", 0);
        }

        String itemId = getIntent().getStringExtra("itemId");

        if (itemId != null) {
            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference()
                    .child("Items");

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
                                    titleTextView.setText("Name: " + item.getTitle());
                                    priceTextView.setText("Price: " + item.getPrice() + "SR");
                                    descriptionTextView.setText(item.getDescription());
                                    imageUrls = item.getImageUrls();

                                    if (imageUrls != null && !imageUrls.isEmpty()) {
                                        loadImage(imageUrls.get(currentImageIndex));
                                    }

                                    arrowLeft.setOnClickListener(v -> showPreviousImage());
                                    arrowRight.setOnClickListener(v -> showNextImage());
                                    break;
                                }
                            }
                        }
                        if (found) {
                            break;
                        }
                    }

                    if (!found) {
                        Toast.makeText(OrderDetailsActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(OrderDetailsActivity.this, "Failed to load item: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Item ID not passed", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initialize TextViews to display the data
        TextView addressTextView = findViewById(R.id.addressTextView);
        TextView countryTextView = findViewById(R.id.countryTextView);
        TextView userEmailTextView = findViewById(R.id.userEmailTextView);
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView stateTextView = findViewById(R.id.stateTextView);
        TextView contactTextView = findViewById(R.id.contactTextView);

        // Get data from Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String address = extras.getString("address");
            String country = extras.getString("country");
            String userEmail = extras.getString("userEmail");
            String username = extras.getString("username");
            String state = extras.getString("state");
            String contact = extras.getString("contact");

            // Set data to TextViews
            addressTextView.setText("Address : "+address);
            countryTextView.setText("Country : "+country);
            userEmailTextView.setText("Email : "+userEmail);
            usernameTextView.setText("Name : "+username);
            stateTextView.setText("State : "+state);
            contactTextView.setText("Contact : "+contact);
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt("currentImageIndex", currentImageIndex);
    }

    private void loadImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.your_image)
                .into(imageView);
    }

    private void showNextImage() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Load the next image first
            int nextIndex = currentImageIndex + 1;
            if (nextIndex >= imageUrls.size()) {
                nextIndex = 0; // Loop back to the first image
            }
            loadImage(imageUrls.get(nextIndex));

            // Update the currentImageIndex after loading the image
            currentImageIndex = nextIndex;
        }
    }

    private void showPreviousImage() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Load the previous image first
            int previousIndex = currentImageIndex - 1;
            if (previousIndex < 0) {
                previousIndex = imageUrls.size() - 1; // Loop back to the last image
            }
            loadImage(imageUrls.get(previousIndex));

            // Update the currentImageIndex after loading the image
            currentImageIndex = previousIndex;
        }
    }
}
