package com.app.partyshopping;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    private TextView totalPriceTextView;


    private FirebaseAuth mAuth;

    private Button orderAllButton; // Add the Order All button

    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            cartItems = new ArrayList<>();
            cartAdapter = new CartAdapter(getContext(), cartItems, totalPriceTextView); // Pass totalPriceTextView here
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            cartItems = new ArrayList<>();
            cartAdapter = new CartAdapter(getContext(), cartItems, totalPriceTextView); // Pass totalPriceTextView here
        }

        totalPriceTextView = view.findViewById(R.id.text_total_price);
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(cartAdapter);

        orderAllButton = view.findViewById(R.id.btn_order_all);
        orderAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderAll();
            }
        });

        fetchCartItems();

        return view;
    }
    private void orderAll() {
        ArrayList<String> itemIds = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            itemIds.add(cartItem.getItemId());
        }

        // Start OrderActivity and pass all itemIds as extras
        Intent intent = new Intent(getContext(), OrderActivity1.class);
        intent.putStringArrayListExtra("itemId", itemIds);
        startActivity(intent);
    }
    private void fetchCartItems() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            DocumentReference cartRef = db.collection("Cart")
                    .document(userEmail);

            cartRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> itemIds = new ArrayList<>();
                        for (String key : document.getData().keySet()) {
                            if (key.equals("userId")) {
                                continue;
                            }
                            itemIds.add(key);
                        }
                        fetchItemDetails(itemIds);
                    } else {
                        Toast.makeText(getContext(), "No items in cart", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch cart items: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchItemDetails(List<String> itemIds) {
        final int[] totalPrice = {0}; // Initialize total price

        // Check if getContext() returns null
        if (getContext() == null) {
            return;
        }

        for (String itemId : itemIds) {
            DatabaseReference flowersRef = FirebaseDatabase.getInstance().getReference()
                    .child("Items")
                    .child("Apple Products")
                    .child(itemId);

            DatabaseReference cakesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Items")
                    .child("Fashion")
                    .child(itemId);

            DatabaseReference stagesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Items")
                    .child("Household Items")
                    .child(itemId);

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Item item = snapshot.getValue(Item.class);
                        if (item != null) {
                            CartItem cartItem = new CartItem();
                            cartItem.setItemId(itemId);
                            cartItem.setTitle(item.getTitle());

                            // Ensure that item.getPrice() returns an integer
                            int price = Integer.parseInt(item.getPrice());
                            cartItem.setPrice(price);

                            cartItem.setImageUrl(item.getImageUrls().get(0));
                            cartItems.add(cartItem);
                            cartAdapter.notifyDataSetChanged();
                            totalPrice[0] += price; // Update total price with integer value
                            totalPriceTextView.setText("Total Price: SR " + totalPrice[0]);
                        }
                    } else {
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            };

            flowersRef.addListenerForSingleValueEvent(valueEventListener);
            cakesRef.addListenerForSingleValueEvent(valueEventListener);
            stagesRef.addListenerForSingleValueEvent(valueEventListener);
        }
    }
}
