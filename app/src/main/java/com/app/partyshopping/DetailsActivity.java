package com.app.partyshopping;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    private ImageView imageView, arrowLeft, arrowRight;
    private TextView titleTextView, priceTextView, descriptionTextView;
    private List<String> imageUrls;
    private int currentImageIndex = 0;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        imageView = findViewById(R.id.image_view);
        titleTextView = findViewById(R.id.title_text_view);
        priceTextView = findViewById(R.id.price_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        arrowLeft = findViewById(R.id.arrow_left);
        arrowRight = findViewById(R.id.arrow_right);
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_buy_now).setOnClickListener(v -> buyNow());


        // Restore the currentImageIndex if the activity is recreated
        if (savedInstanceState != null) {
            currentImageIndex = savedInstanceState.getInt("currentImageIndex", 0);
        }

        // Get the item ID passed from HomeFragment
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
                                    priceTextView.setText("Price: " + item.getPrice() + "$");
                                    descriptionTextView.setText(item.getDescription());
                                    imageUrls = item.getImageUrls();

                                    if (imageUrls != null && !imageUrls.isEmpty()) {
                                        loadImage(imageUrls.get(currentImageIndex));
                                    }

                                    // Add click listeners to the left and right arrows
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
                        Toast.makeText(DetailsActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity if item not found
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailsActivity.this, "Failed to load item: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity if failed to load item
                }
            });
        } else {
            Toast.makeText(this, "Item ID not passed", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if item ID not passed
        }

        // Add click listener to the "Add to Cart" button
        findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> addToCart());
    }
    private void buyNow() {
        String itemId = getIntent().getStringExtra("itemId");
        if (itemId != null) {
            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("itemId", itemId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Item ID not passed", Toast.LENGTH_SHORT).show();
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

    // Method to show the next image in the list
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

    // Method to show the previous image in the list
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

    // Method to add item ID to cart using Firestore
    private void addToCart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            String itemId = getIntent().getStringExtra("itemId");
            if (itemId != null) {
                // Create a reference to the user's cart collection
                DocumentReference cartRef = db.collection("Cart")
                        .document(userEmail);

                // Create a map to store the item ID
                Map<String, Object> itemData = new HashMap<>();
                itemData.put(itemId, true);

                // Add the item ID to the user's cart
                cartRef.set(itemData, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DetailsActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DetailsActivity.this, "Failed to add item to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Item ID not passed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
