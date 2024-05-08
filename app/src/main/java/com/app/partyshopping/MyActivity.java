package com.app.partyshopping;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemDetailAdapter itemAdapter;
    private List<ItemDetail> itemList;
    private TextView totalPriceTextView; // TextView for displaying total price

    private DatabaseReference itemsRef;

    private TextView stateTextView, countryTextView, userEmailTextView, contactTextView, itemIdTextView, addressTextView, username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        totalPriceTextView = findViewById(R.id.totalPriceTextView); // Initialize TextView

        stateTextView = findViewById(R.id.stateTextView);
        countryTextView = findViewById(R.id.countryTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        contactTextView = findViewById(R.id.contactTextView);
        itemIdTextView = findViewById(R.id.itemIdTextView);
        addressTextView = findViewById(R.id.addressTextView);
        username = findViewById(R.id.usernameTextView);

        // Get data from intent
        String state = getIntent().getStringExtra("state");
        String country = getIntent().getStringExtra("country");
        String userEmail = getIntent().getStringExtra("userEmail");
        String contact = getIntent().getStringExtra("contact");
        String itemId = getIntent().getStringExtra("itemId");
        String address = getIntent().getStringExtra("address");
        String username1 = getIntent().getStringExtra("username");

        // Set data to TextViews
        stateTextView.setText("State : " + state);
        countryTextView.setText("Country :  " + country);
        userEmailTextView.setText("Email  :  " + userEmail);
        contactTextView.setText("Contact  : " + contact);
        itemIdTextView.setText(itemId);
        addressTextView.setText("Address  : " + address);
        username.setText("Name  :  " + username1);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        itemAdapter = new ItemDetailAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

        String[] itemIds = itemId.split(",");
        for (String id : itemIds) {
            searchItemInItemsNode(id);
        }
    }

    private void searchItemInItemsNode(String itemId) {
        DatabaseReference flowersRef = itemsRef.child("Apple Products").child(itemId);
        DatabaseReference cakesRef = itemsRef.child("Fashion").child(itemId);
        DatabaseReference stagesDesignRef = itemsRef.child("Household Items").child(itemId);

        flowersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    handleItemData(dataSnapshot);
                } else {
                    // Item not found in Flowers node, check Cakes node
                    searchInCakesNode(itemId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyActivity.this, "Failed to fetch item details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchInCakesNode(String itemId) {
        DatabaseReference cakesRef = itemsRef.child("Fashion").child(itemId);
        cakesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    handleItemData(dataSnapshot);
                } else {
                    // Item not found in Cakes node, check Stages design node
                    searchInStagesDesignNode(itemId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyActivity.this, "Failed to fetch item details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchInStagesDesignNode(String itemId) {
        DatabaseReference stagesDesignRef = itemsRef.child("Household Items").child(itemId);
        stagesDesignRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    handleItemData(dataSnapshot);
                } else {
                    // Item not found in Stages design node
                    Toast.makeText(MyActivity.this, "Item with ID " + itemId + " not found in any category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyActivity.this, "Failed to fetch item details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleItemData(DataSnapshot dataSnapshot) {
        String title = dataSnapshot.child("title").getValue(String.class);
        String price = dataSnapshot.child("price").getValue(String.class);
        String description = dataSnapshot.child("description").getValue(String.class);

        // Assuming image URLs are stored in the database under "imageUrls" node
        List<String> imageUrls = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.child("imageUrls").getChildren()) {
            String imageUrl = snapshot.getValue(String.class);
            imageUrls.add(imageUrl);
        }

        // Add item to itemList
        ItemDetail item = new ItemDetail(title, price, description, imageUrls);
        itemList.add(item);

        // Calculate total price
        double totalPrice = 0.0;
        for (ItemDetail itemDetail : itemList) {
            totalPrice += Double.parseDouble(itemDetail.getPrice());
        }

        // Set total price to TextView
        totalPriceTextView.setText("Total Price: SR " + totalPrice);

        // Notify adapter of data change
        itemAdapter.notifyDataSetChanged();
    }
}
