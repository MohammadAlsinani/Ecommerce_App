package com.app.partyshopping;

public class CartItem {
    private String itemId;
    private String category; // Category can be "Flowers", "Cakes", or "Stage Designs"
    private String imageUrl; // URL of the item image
    private String title;
    private int price;

    public CartItem() {
        // Required empty public constructor
    }

    public CartItem(String itemId, String category, String imageUrl, String title, int price) {
        this.itemId = itemId;
        this.category = category;
        this.imageUrl = imageUrl;
        this.title = title;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
