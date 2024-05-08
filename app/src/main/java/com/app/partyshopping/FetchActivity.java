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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

public class FetchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private List<Item> itemList;
    private ItemAdapter itemAdapter;

    private ProgressBar progressBar; // Add ProgressBar declaration

    private static final String CATEGORY_FLOWERS = "Apple Products"; // Define the category for Flowers
    private static final String CATEGORY_CAKES = "Fashion"; // Define the category for Cakes
    private static final String CATEGORY_STAGE_DESIGN = "Household Items"; // Define the category for Stage Design

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar); // Initialize ProgressBar
        TextView categoryTextView = findViewById(R.id.category); // Initialize TextView for category

        // Get the category text passed from the previous activity
        String categoryText = getIntent().getStringExtra("categoryText");

        // Set the category text in the TextView
        if (categoryText != null) {
            categoryTextView.setText(categoryText);
        }

        // Set LinearLayoutManager with vertical orientation
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this);
        recyclerView.setAdapter(itemAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Items");

        if (categoryText != null) {
            switch (categoryText) {
                case "Apple Products":
                    fetchItems(CATEGORY_FLOWERS);
                    break;
                case "Fashion":
                    fetchItems(CATEGORY_CAKES);
                    break;
                case "Household Items":
                    fetchItems(CATEGORY_STAGE_DESIGN);
                    break;
                default:
                    // Handle default case or error condition
                    break;
            }
        }
    }

    private void fetchItems(String category) {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar before fetching data
        DatabaseReference categoryRef = databaseReference.child(category);

        Query query = categoryRef.orderByKey(); // You can modify the query as needed

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE); // Hide progress bar after data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide progress bar after data is loaded
            }
        });
    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
        private List<Item> itemList;
        private Context context;

        public ItemAdapter(List<Item> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout1, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            Item item = itemList.get(position);
            if (item != null) {
                holder.bind(item);

                holder.itemView.setOnClickListener(v -> {
                    // Pass the item ID to the details activity
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("itemId", item.getId()); // Adjust this line based on how you retrieve the item ID from the Item class
                    context.startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView priceTextView;
            ImageView imageView;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.title_text_view);
                priceTextView = itemView.findViewById(R.id.price_text_view);
                imageView = itemView.findViewById(R.id.item_image_view);

                // Set margin top for each item
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
                layoutParams.topMargin = (int) itemView.getContext().getResources().getDimension(R.dimen.item_margin_top);
                itemView.setLayoutParams(layoutParams);
            }

            public void bind(Item item) {
                titleTextView.setText(item.getTitle());
                priceTextView.setText("SR" + item.getPrice());
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.your_image)
                        .into(imageView);
            }
        }
    }
}
