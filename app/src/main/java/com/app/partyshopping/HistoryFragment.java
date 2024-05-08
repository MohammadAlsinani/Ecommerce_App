package com.app.partyshopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView,recyclerView1;
    private OrderAdapter adapter;
    private List<Order1> orderList;

    private historyUserAdapter orderAdapter;
    private List<AdminOrder> orderList1;
    private DatabaseReference ordersRef;
    private DatabaseReference itemsRef;
    private static SimpleDateFormat dateFormat;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = view.findViewById(R.id.orders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView1 = view.findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        orderList1 = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), orderList);
        orderAdapter = new historyUserAdapter(orderList1,this, requireContext());
        recyclerView.setAdapter(adapter);
        recyclerView1.setAdapter(orderAdapter);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            ordersRef = FirebaseDatabase.getInstance().getReference().child("History");
            itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

            Query query = ordersRef.orderByChild("userEmail").equalTo(userEmail);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    orderList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Order1 order = dataSnapshot.getValue(Order1.class);
                        if (order != null) {
                            String itemId = order.getItemId();
                            String orderStatus = order.getOrderStatus();

                            fetchItemDetails(itemId, order.getDateTime(), orderStatus);
                        }
                        AdminOrder order1 = dataSnapshot.getValue(AdminOrder.class);

                        if (order != null && order.getItemId() != null && order.getItemId().contains(",")) {
                            orderList1.add(order1);
                        }
                    }
                    orderAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User not logged in
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }
    public void onOrderItemClick(AdminOrder order) {
        Intent intent = new Intent(requireContext(), MyActivity1.class);
        intent.putExtra("state", order.getState());
        intent.putExtra("country", order.getCountry());
        intent.putExtra("userEmail", order.getUserEmail());
        intent.putExtra("contact", order.getContact());
        intent.putExtra("itemId", order.getItemId());
        intent.putExtra("address", order.getAddress());
        intent.putExtra("username", order.getUsername());
        startActivity(intent);
    }

    private void fetchItemDetails(String itemId, String dateTime, String orderStatus) {
        itemsRef.child("Apple Products").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Item found in Flowers node
                    Item1 item = snapshot.getValue(Item1.class);
                    if (item != null) {
                        Order1 orderWithDetails = new Order1(item.getTitle(), item.getPrice(), item.getItemId(), item.getImageUrls(), dateTime,"", orderStatus);
                        orderList.add(orderWithDetails);

                        adapter.notifyDataSetChanged();
                    }
                } else {
                    // Search for itemId in Cakes node
                    itemsRef.child("Fashion").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("OrdersFragment", "Number of orders fetched: " + snapshot.getChildrenCount());

                            if (snapshot.exists()) {
                                // Item found in Cakes node
                                Item1 item = snapshot.getValue(Item1.class);
                                if (item != null) {
                                    Order1 orderWithDetails = new Order1(item.getTitle(), item.getPrice(), item.getItemId(), item.getImageUrls(), dateTime,"",orderStatus);
                                    orderList.add(orderWithDetails);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                // Search for itemId in Stage Design node
                                itemsRef.child("Household Items").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // Item found in Stage Design node
                                            Item1 item = snapshot.getValue(Item1.class);
                                            if (item != null) {
                                                Order1 orderWithDetails = new Order1(item.getTitle(), item.getPrice(), item.getItemId(), item.getImageUrls(), dateTime,"",orderStatus);
                                                orderList.add(orderWithDetails);
                                                adapter.notifyDataSetChanged();
                                            }
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

    public static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order1> orderList;
        private Context context; // Add context field

        public OrderAdapter(Context context, List<Order1> orderList) {
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
            Order1 order = orderList.get(position);
            holder.bind(order);
            holder.itemView.setOnClickListener(v -> {
                // Pass the item ID to the details activity
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("itemId", order.getItemId());
                context.startActivity(intent);
            });
        }


        @Override
        public int getItemCount() {
            return orderList.size();
        }

        public class OrderViewHolder extends RecyclerView.ViewHolder {
            private TextView titleTextView, priceTextView, dateTimeTextView, orderStatusTextView;
            private ImageView imageView;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.order_item_title);
                priceTextView = itemView.findViewById(R.id.order_item_price);
                dateTimeTextView = itemView.findViewById(R.id.order_item_datetime);
                orderStatusTextView = itemView.findViewById(R.id.item_status); // Initialize orderStatusTextView
                imageView = itemView.findViewById(R.id.order_item_image);
            }

            public void bind(Order1 order) {
                titleTextView.setText(order.getItemTitle());
                priceTextView.setText("SR" + order.getItemPrice());
                dateTimeTextView.setText(order.getDateTime());
                orderStatusTextView.setText(order.getOrderStatus());

                // Load and display image using Picasso
                if (order.getItemImageUrls() != null && order.getItemImageUrls().size() > 0) {
                    Picasso.get()
                            .load(order.getItemImageUrls().get(0)) // Assuming the first URL is the main image URL
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
