package com.app.partyshopping;

import java.util.List;

public class Item1 {
    private String itemId;
    private String title;
    private String description;
    private String price;
    private List<String> imageUrls;
    private String dateTime; // Add dateTime field

    public Item1() {
        // Default constructor required for Firebase
    }

    public Item1(String itemId, String title, String description, String price, List<String> imageUrls, String dateTime) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;
        this.dateTime = dateTime; // Initialize dateTime
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
