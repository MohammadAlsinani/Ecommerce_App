package com.app.partyshopping;

import java.util.ArrayList;

public class Order {
    private String userId;
    private String userEmail;
    private String username;
    private String itemId;
    private String address;
    private String state;
    private String country;
    private String contact;
    private String dateTime;
    private String orderId;
    private String orderStatus; // New field for order status

    // Default constructor required for Firebase
    public Order(String userId, String userEmail, String username, String orderId, String address, String state, String country, String contact, String dateTime, String id, String orderStatus, ArrayList<Item> orderedItems) {
    }

    public Order(String userId, String userEmail, String username, String itemId, String address, String state, String country, String contact, String dateTime, String orderId, String orderStatus) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.username = username;
        this.itemId = itemId;
        this.address = address;
        this.state = state;
        this.country = country;
        this.contact = contact;
        this.dateTime = dateTime;
        this.orderId = orderId;
        this.orderStatus = orderStatus; // Set order status
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
