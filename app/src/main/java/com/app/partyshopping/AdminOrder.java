package com.app.partyshopping;

import java.util.List;

public class AdminOrder {
    private String itemTitle;
    private double itemPrice;

    private String orderId;
    private String itemId;
    private List<String> imageUrls;
    private String dateTime;
    private String address;
    private String country;
    private String userEmail;
    private String username;
    private String state;
    private String contact;
    private String orderStatus; // New field for order status

    public AdminOrder() {
        // Default constructor required for Firebase
    }

    public AdminOrder(String itemTitle, double itemPrice, String itemId, List<String> imageUrls, String dateTime) {
        this.itemTitle = itemTitle;
        this.itemPrice = itemPrice;
        this.itemId = itemId;
        this.imageUrls = imageUrls;
        this.dateTime = dateTime;

    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public double getItemPrice() {
        return itemPrice;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getOrderId() {
        return orderId;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
