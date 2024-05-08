package com.app.partyshopping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class FetchAdminOrders extends AppCompatActivity {
    private RecyclerView recyclerView,recyclerView1;
    private AdminOrderAdapter orderAdapter;
    private List<AdminOrder> orderList;
    private List<AdminOrder> orderList1;

    private String order1;
    private AdminOrdersFragment.OrderAdapter adapter;



    private DatabaseReference ordersRef;
    private DatabaseReference itemsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_admin_orders);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderList1 = new ArrayList<>();

      //  orderAdapter = new AdminOrderAdapter(orderList,this,this);
        adapter = new AdminOrdersFragment.OrderAdapter(this, orderList1);

        recyclerView.setAdapter(orderAdapter);
        recyclerView1.setAdapter(adapter);


        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

        fetchOrders();
    }

    private void fetchOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
                orderList1.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AdminOrder order = snapshot.getValue(AdminOrder.class);
                    if (order != null && order.getItemId() != null && order.getItemId().contains(",")) {
                        orderList.add(order);
                    }
                    else {
                        String itemId = order.getItemId();
                        String dateTime = order.getDateTime();
                        order1 = order.getOrderId();
                        fetchItemDetails(itemId, dateTime, order);

                    }
                }
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FetchAdminOrders.this, "Failed to fetch orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onOrderItemClick(AdminOrder order) {
        Intent intent = new Intent(this, MyActivity.class);
        intent.putExtra("state", order.getState());
        intent.putExtra("country", order.getCountry());
        intent.putExtra("userEmail", order.getUserEmail());
        intent.putExtra("contact", order.getContact());
        intent.putExtra("itemId", order.getItemId());
        intent.putExtra("address", order.getAddress());
        intent.putExtra("username", order.getUsername());
        startActivity(intent);
    }
    private void fetchItemDetails(String itemId, String dateTime, AdminOrder order) {
        if (itemId == null) {
            Log.e("FetchItemDetails", "Item ID is null");
            return; // Exit the method if item ID is null
        }

        itemsRef.child("Apple Products").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Item found in Flowers node
                    processItemDetails(snapshot, dateTime, order);
                } else {
                    // Item not found in Flowers node, check Cakes node
                    itemsRef.child("Fashion").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Item found in Cakes node
                                processItemDetails(snapshot, dateTime, order);
                            } else {
                                // Item not found in Cakes node, check Stages Design node
                                itemsRef.child("Household Items").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // Item found in Stages Design node
                                            processItemDetails(snapshot, dateTime, order);
                                        } else {
                                            // Item not found in any node
                                            Log.d("ItemNotFound", "Item with ID " + itemId + " not found in any node.");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void processItemDetails(DataSnapshot snapshot, String dateTime, AdminOrder order) {
        List<String> imageUrlList = new ArrayList<>();
        for (DataSnapshot imageUrlSnapshot : snapshot.child("imageUrls").getChildren()) {
            String imageUrl = imageUrlSnapshot.getValue(String.class);
            if (imageUrl != null) {
                imageUrlList.add(imageUrl);
            }
        }
        String title = snapshot.child("title").getValue(String.class);
        String priceString = snapshot.child("price").getValue(String.class);
        // Handle data conversion for price
        double price;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            // Handle the case where priceString is not a valid double
            Log.e("DataConversionError", "Failed to convert price to double: " + priceString);
            price = 0.0; // Set a default value or handle the error as needed
        }

        // Create the order with details including additional data
        AdminOrder orderWithDetails = new AdminOrder(title, price, order.getItemId(), imageUrlList, dateTime);
        orderWithDetails.setAddress(order.getAddress());
        orderWithDetails.setCountry(order.getCountry());
        orderWithDetails.setUserEmail(order.getUserEmail());
        orderWithDetails.setUsername(order.getUsername());
        orderWithDetails.setState(order.getState());
        orderWithDetails.setContact(order.getContact());
        orderWithDetails.setOrderStatus(order.getOrderStatus()); // Set order status
        orderWithDetails.setOrderId(order.getOrderId()); // Set orderId

        orderList1.add(orderWithDetails);
        adapter.notifyDataSetChanged();
    }


}
