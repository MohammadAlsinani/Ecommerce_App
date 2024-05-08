package com.app.partyshopping;

import java.util.List;

public class ItemDetail {
    private String title;
    private String price;
    private String description;
    private List<String> imageUrls;

    public ItemDetail() {
        // Default constructor required for Firebase
    }

    public ItemDetail(String title, String price, String description, List<String> imageUrls) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrls = imageUrls;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
