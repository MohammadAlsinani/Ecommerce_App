package com.app.partyshopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminAllItemsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private List<ItemAdmin> itemList;
    private ItemAdapter itemAdapter;
    private ProgressBar progressBar; // Add ProgressBar declaration

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_all_items, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar); // Initialize ProgressBar

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, requireContext());
        recyclerView.setAdapter(itemAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Items");

        fetchAllItems(); // Fetch items from all categories

        return view;
    }

    private void fetchAllItems() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar before fetching data

        Query query = databaseReference.orderByKey(); // You can modify the query as needed

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.getKey();
                    if (category != null) {
                        for (DataSnapshot itemSnapshot : categorySnapshot.getChildren()) {
                            ItemAdmin item = itemSnapshot.getValue(ItemAdmin.class);
                            if (item != null) {
                                // Add category information to each item
                                item.setCategory(category);
                                itemList.add(item);
                            }
                        }
                    }
                }
                itemAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE); // Hide progress bar after data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide progress bar on error
            }
        });
    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
        private List<ItemAdmin> itemList;
        private Context context;

        public ItemAdapter(List<ItemAdmin> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_admin, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            ItemAdmin item = itemList.get(position);
            if (item != null) {
                holder.bind(item);

                holder.imageViewDelete.setOnClickListener(v -> {
                    // Show confirmation dialog before deleting
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Are you sure you want to delete this item?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        // Handle item deletion here
                        deleteItemFromFirebase(item.getItemId(), item.getCategory());
                    });
                    builder.setNegativeButton("No", (dialog, which) -> {
                        // Do nothing if the user clicks "No"
                        dialog.dismiss();
                    });
                    builder.show();
                });
            }
        }

        private void deleteItemFromFirebase(String itemId, String category) {
            DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference().child("Items").child(category).child(itemId);
            itemRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            });
        }


        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView priceTextView;
            TextView categoryTextView;
            ImageView imageView;
            ImageView imageViewDelete; // Add ImageView for delete

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.title_text_view);
                priceTextView = itemView.findViewById(R.id.price_text_view);
                categoryTextView = itemView.findViewById(R.id.category_text_view);
                imageView = itemView.findViewById(R.id.item_image_view);
                imageViewDelete = itemView.findViewById(R.id.delete); // Initialize delete ImageView
            }

            public void bind(ItemAdmin item) {
                titleTextView.setText(item.getTitle());
                priceTextView.setText("$" + item.getPrice());
                categoryTextView.setText(item.getCategory());
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.your_image)
                        .into(imageView);
            }
        }
    }
}