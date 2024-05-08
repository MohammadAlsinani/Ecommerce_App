package com.app.partyshopping;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.OrderViewHolder> {
    private List<AdminOrder> orderList;
    private AdminHistoryFragment activity;
    private Context context; // Add context field


    public HistoryAdapter(List<AdminOrder> orderList, AdminHistoryFragment activity,Context context) {
        this.orderList = orderList;
        this.activity = activity;
        this.context = context;


    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        AdminOrder order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView dateTimeTextView;
        private TextView orderStatusTextView;
        private TextView viewDetailsTextView;


        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatusTextView);
            viewDetailsTextView = itemView.findViewById(R.id.viewDetailsTextView);
        }

        public void bind(AdminOrder order) {
            if (order.getItemId().contains(",")) {
                nameTextView.setText("Multiple items (" + order.getItemId().split(",").length + ")");
                dateTimeTextView.setText(order.getDateTime());
                orderStatusTextView.setText(order.getOrderStatus());
                viewDetailsTextView.setText("View All details");

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.onOrderItemClick(order);
                    }
                });
                orderStatusTextView.setOnClickListener(v -> {
                    showStatusPopup(order); // Call a method to show the status popup
                });
            }
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
    }
}
