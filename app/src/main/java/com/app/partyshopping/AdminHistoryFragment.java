package com.app.partyshopping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminHistoryFragment extends Fragment {
    private RecyclerView recyclerView, recyclerView1;
    private HistoryAdapter orderAdapter;
    private OrderAdapter adapter;
    private List<AdminOrder> orderList;
    private List<AdminOrder> orderList1;

    private String order1;


    private DatabaseReference ordersRef;
    private DatabaseReference itemsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView1 = view.findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView1.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderList = new ArrayList<>();
        orderList1 = new ArrayList<>();

        orderAdapter = new HistoryAdapter(orderList,this, requireContext());
        adapter = new OrderAdapter(requireContext(), orderList1);

        recyclerView.setAdapter(orderAdapter);
        recyclerView1.setAdapter(adapter);

        ordersRef = FirebaseDatabase.getInstance().getReference().child("History");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

        fetchOrders();

        return view;
    }

    private void fetchOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
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
                Toast.makeText(requireContext(), "Failed to fetch orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onOrderItemClick(AdminOrder order) {
        Intent intent = new Intent(requireContext(), MyActivity.class);
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
                                        Toast.makeText(getContext(), "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch item details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<AdminOrder> orderList;
        private Context context; // Add context field

        public OrderAdapter(Context context, List<AdminOrder> orderList) {
            this.context = context;
            this.orderList = orderList;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order1, parent, false);
            return new OrderViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            AdminOrder order = orderList.get(position);
            holder.bind(order);
            holder.itemView.setOnClickListener(v -> {
                // Pass the item ID and additional data to the details activity
                Intent intent = new Intent(context, OrderDetailsActivity.class);
                intent.putExtra("itemId", order.getItemId()); // Adjust this line based on how you retrieve the item ID from the Item class
                intent.putExtra("address", order.getAddress());
                intent.putExtra("country", order.getCountry());
                intent.putExtra("userEmail", order.getUserEmail());
                intent.putExtra("username", order.getUsername());
                intent.putExtra("state", order.getState());
                intent.putExtra("contact", order.getContact());
                context.startActivity(intent);
            });
            holder.orderStatusTextView.setOnClickListener(v -> {
                showStatusPopup(order); // Call a method to show the status popup
            });
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }
        private void showStatusPopup(AdminOrder order) {
            // Create a dialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Order Status");

            // Inflate the layout for the dialog
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_status, null);
            builder.setView(view);

            // Find the RadioGroup and RadioButtons
            RadioGroup radioGroupStatus = view.findViewById(R.id.radio_group_status);
            RadioButton radioInProgress = view.findViewById(R.id.radio_in_progress);
            RadioButton radioDelivered = view.findViewById(R.id.radio_delivered);

            // Set initial state based on order status
            if (order.getOrderStatus().equals("In Progress")) {
                radioInProgress.setChecked(true);
            } else if (order.getOrderStatus().equals("Delivered")) {
                radioDelivered.setChecked(true);
            }

            // Set click listener for OK button
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Check which radio button is checked and update order status accordingly
                int checkedId = radioGroupStatus.getCheckedRadioButtonId();
                if (checkedId == R.id.radio_in_progress) {
                    updateOrderStatus(order, "In Progress");
                } else if (checkedId == R.id.radio_delivered) {
                    updateOrderStatus(order, "Delivered");
                } else {
                    // No radio button selected, show error message
                    Toast.makeText(context, "Please select a status", Toast.LENGTH_SHORT).show();
                }
            });

            // Set click listener for Cancel button
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss(); // Dismiss the dialog
            });

            // Show the dialog
            builder.create().show();

        }

        private void updateOrderStatus(AdminOrder order, String status) {
            String orderId = order.getOrderId();
            Log.d("OrderIdDebug", "OrderId: " + orderId); // Add this line to log orderId

            if (orderId == null || orderId.isEmpty()) {
                Log.e("OrderIdError", "OrderId is null or empty");
                // Handle the case where orderId is null or empty
                return;
            }

            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
            ordersRef.child(orderId).child("orderStatus").setValue(status)
                    .addOnSuccessListener(aVoid -> {
                        // Update the order status locally and notify the adapter
                        order.setOrderStatus(status);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Order status updated successfully", Toast.LENGTH_SHORT).show();
                        if (status.equals("Delivered")) {
                            // Order is delivered, delete it from Firebase
                            deleteOrder(orderId);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update order status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        private void deleteOrder(String orderId) {
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
            ordersRef.child(orderId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Order deleted successfully
                        Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();

                    })
                    .addOnFailureListener(e -> {
                        // Failed to delete order
                        Toast.makeText(context, "Failed to delete order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }


        public class OrderViewHolder extends RecyclerView.ViewHolder {
            private TextView titleTextView, priceTextView, dateTimeTextView, orderStatusTextView; // Add orderStatusTextView
            private ImageView imageView;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.order_item_title);
                priceTextView = itemView.findViewById(R.id.order_item_price);
                dateTimeTextView = itemView.findViewById(R.id.order_item_datetime); // Initialize dateTimeTextView
                orderStatusTextView = itemView.findViewById(R.id.item_status); // Initialize orderStatusTextView
                imageView = itemView.findViewById(R.id.order_item_image); // Initialize imageView
            }

            public void bind(AdminOrder order) {
                titleTextView.setText(order.getItemTitle());
                priceTextView.setText("$" + order.getItemPrice());
                orderStatusTextView.setText(order.getOrderStatus()); // Set order status

                // Log the value of dateTimeString for debugging
                String dateTimeString = order.getDateTime(); // Assuming getDateTime returns a string
                Log.d("DateTimeDebug", "DateTimeString: " + dateTimeString);

                if (dateTimeString != null && !dateTimeString.isEmpty()) {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    // Example output format
                    try {
                        Date dateTime = inputFormat.parse(dateTimeString);
                        String formattedDateTime = outputFormat.format(dateTime);
                        dateTimeTextView.setText(formattedDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        dateTimeTextView.setText("Invalid Date");
                    }
                } else {
                    dateTimeTextView.setText("No Date Available");
                }

                // Load and display image using Picasso
                if (order.getImageUrls() != null && order.getImageUrls().size() > 0) {
                    Picasso.get()
                            .load(order.getImageUrls().get(0)) // Assuming the first URL is the main image URL
                            .placeholder(R.drawable.your_image) // Placeholder image while loading
                            .into(imageView);
                } else {
                    // Handle case when no image URL is available
                    imageView.setImageResource(R.drawable.your_image);
                }
            }
        }
    }
}
