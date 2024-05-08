package com.app.partyshopping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ItemDetailAdapter extends RecyclerView.Adapter<ItemDetailAdapter.ItemViewHolder> {
    private Context context;
    private List<ItemDetail> itemList;

    public ItemDetailAdapter(Context context, List<ItemDetail> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemDetail item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView, priceTextView, descriptionTextView;
        private ImageView imageView1, imageView2, imageView3, imageView4;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            imageView1 = itemView.findViewById(R.id.imageView1);
            imageView2 = itemView.findViewById(R.id.imageView2);
            imageView3 = itemView.findViewById(R.id.imageView3);
            imageView4 = itemView.findViewById(R.id.imageView4);
        }

        public void bind(ItemDetail item) {
            titleTextView.setText("Item Name  :  "  +  item.getTitle());
            priceTextView.setText("Item Price  :  "  + item.getPrice() + "SR");
            descriptionTextView.setText(item.getDescription());

            // Load images using Glide library
            if (item.getImageUrls() != null && item.getImageUrls().size() >= 4) {
                Glide.with(context).load(item.getImageUrls().get(0)).into(imageView1);
                Glide.with(context).load(item.getImageUrls().get(1)).into(imageView2);
                Glide.with(context).load(item.getImageUrls().get(2)).into(imageView3);
                Glide.with(context).load(item.getImageUrls().get(3)).into(imageView4);
            }
        }
    }
}
