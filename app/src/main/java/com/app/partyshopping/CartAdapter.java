package com.app.partyshopping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private FirebaseAuth mAuth;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    private TextView totalPriceTextView; // Declare totalPriceTextView

    public CartAdapter(Context context, List<CartItem> cartItems, TextView totalPriceTextView) {
        this.context = context;
        this.cartItems = cartItems;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.totalPriceTextView = totalPriceTextView; // Initialize totalPriceTextView
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.titleTextView.setText(cartItem.getTitle());
        holder.priceTextView.setText("$" + cartItem.getPrice());
        Glide.with(context).load(cartItem.getImageUrl()).into(holder.imageView);

        holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(cartItem.getItemId());
            }
        });
        holder.itemView.setOnClickListener(v -> {
            // Pass the item ID to the details activity
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("itemId", cartItem.getItemId()); // Adjust this line based on how you retrieve the item ID from the Item class
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, deleteImageView;
        TextView titleTextView, priceTextView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cart_item_image);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            priceTextView = itemView.findViewById(R.id.cart_item_price);
            deleteImageView = itemView.findViewById(R.id.delete);
        }
    }

    private void showDeleteConfirmationDialog(String itemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(itemId);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void deleteItem(String itemId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            DocumentReference cartRef = db.collection("Cart")
                    .document(userEmail);

            // Remove the specific item from the cart using FieldValue.delete()
            cartRef.update(itemId, FieldValue.delete())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Remove the item from the list and notify the adapter
                            int position = getCartItemPosition(itemId);
                            if (position != -1) {
                                cartItems.remove(position);
                                notifyItemRemoved(position);
                                updateTotalPrice(); // Update total price after item deletion
                                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Item not found in cart", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalPrice() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice();
        }
        totalPriceTextView.setText("Total Price: SR " + total);
    }

    private int getCartItemPosition(String itemId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getItemId().equals(itemId)) {
                return i;
            }
        }
        return -1; // Item not found
    }
}
