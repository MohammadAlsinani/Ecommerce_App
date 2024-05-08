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

public class userHistoryAdapter extends RecyclerView.Adapter<userHistoryAdapter.OrderViewHolder> {
    private List<AdminOrder> orderList;
    private OrdersFragment activity;
    private Context context; // Add context field


    public userHistoryAdapter(List<AdminOrder> orderList, OrdersFragment activity,Context context) {
        this.orderList = orderList;
        this.activity = activity;
        this.context = context;


    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_order_item_layout, parent, false);
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
            }
        }
    }
}
