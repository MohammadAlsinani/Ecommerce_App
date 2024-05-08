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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView recyclerView1; // Second RecyclerView for Cakes category
    private RecyclerView recyclerView2; // Third RecyclerView for Stage Design category
    private DatabaseReference databaseReference;
    private List<Item> itemList;
    private List<Item> itemList1; // List for Cakes category
    private List<Item> itemList2; // List for Stage Design category
    private ItemAdapter itemAdapter;
    private ItemAdapter itemAdapter1; // Adapter for Cakes category
    private ItemAdapter itemAdapter2; // Adapter for Stage Design category

    private ProgressBar progressBar; // Add ProgressBar declaration

    private static final String CATEGORY_FLOWERS = "Apple Products";
    private static final String CATEGORY_CAKES = "Fashion"; // Define the category for Cakes
    private static final String CATEGORY_STAGE_DESIGN = "Household Items"; // Define the category for Stage Design

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView1 = view.findViewById(R.id.recycler_view1); // Initialize second RecyclerView
        recyclerView2 = view.findViewById(R.id.recycler_view2); // Initialize third RecyclerView
        progressBar = view.findViewById(R.id.progress_bar); // Initialize ProgressBar

        recyclerView.setHasFixedSize(true);
        recyclerView1.setHasFixedSize(true); // Set fixed size for better performance
        recyclerView2.setHasFixedSize(true); // Set fixed size for better performance

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView1.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)); // Set LinearLayoutManager for both RecyclerViews
        recyclerView2.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)); // Set LinearLayoutManager for recyclerView2

        itemList = new ArrayList<>();
        itemList1 = new ArrayList<>(); // Initialize itemList1
        itemList2 = new ArrayList<>(); // Initialize itemList2

        itemAdapter = new ItemAdapter(itemList, requireContext());
        itemAdapter1 = new ItemAdapter(itemList1, requireContext()); // Initialize itemAdapter1
        itemAdapter2 = new ItemAdapter(itemList2, requireContext()); // Initialize itemAdapter2

        recyclerView.setAdapter(itemAdapter);
        recyclerView1.setAdapter(itemAdapter1); // Set adapters for both RecyclerViews
        recyclerView2.setAdapter(itemAdapter2); // Set adapter for recyclerView2

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Items");

        fetchItems(CATEGORY_FLOWERS, itemAdapter, itemList); // Fetch items for Flowers category
        fetchItems(CATEGORY_CAKES, itemAdapter1, itemList1); // Fetch items for Cakes category
        fetchItems(CATEGORY_STAGE_DESIGN, itemAdapter2, itemList2); // Fetch items for Stage Design category
        CardView flowerCard = view.findViewById(R.id.flowers);
        flowerCard.setOnClickListener(v -> openFetchActivity(CATEGORY_FLOWERS));

        // CardView for Cake category
        CardView cakeCard = view.findViewById(R.id.cakes);
        cakeCard.setOnClickListener(v -> openFetchActivity(CATEGORY_CAKES));

        // CardView for Stage Design category
        CardView stageCard = view.findViewById(R.id.stage);
        stageCard.setOnClickListener(v -> openFetchActivity(CATEGORY_STAGE_DESIGN));

        TextView text = view.findViewById(R.id.see);
        text.setOnClickListener(v -> openFetchActivity(CATEGORY_FLOWERS));

        TextView text1 = view.findViewById(R.id.see1);
        text1.setOnClickListener(v -> openFetchActivity(CATEGORY_CAKES));

        TextView text2 = view.findViewById(R.id.see2);
        text2.setOnClickListener(v -> openFetchActivity(CATEGORY_STAGE_DESIGN));

        return view;
    }
    private void openFetchActivity(String category) {
        Intent intent = new Intent(requireContext(), FetchActivity.class);
        intent.putExtra("categoryText", category);
        startActivity(intent);
    }

    private void fetchItems(String category, ItemAdapter adapter, List<Item> list) {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar before fetching data
        DatabaseReference categoryRef = databaseReference.child(category);

        Query query = categoryRef.orderByKey(); // You can modify the query as needed

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        list.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
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
            TextView priceTextView; // Added TextView for price
            ImageView imageView;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.title_text_view);
                priceTextView = itemView.findViewById(R.id.price_text_view); // Initialize price TextView
                imageView = itemView.findViewById(R.id.item_image_view);
            }

            public void bind(Item item) {
                titleTextView.setText(item.getTitle());
                priceTextView.setText("$" + item.getPrice()); // Set item price with a dollar sign
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0)) // Assuming the first image is the main image
                        .centerCrop()
                        .placeholder(R.drawable.your_image)
                        .into(imageView);
            }
        }
    }
}
