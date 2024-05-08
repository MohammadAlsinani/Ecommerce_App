package com.app.partyshopping;

public class OrderItem {
    private String state;
    private String country;
    private String address;
    private String dateTime;
    private String userEmail;
    private String orderStatus;
    private String username;
    private String itemId;
    private String contact;

    public OrderItem() {
        // Default constructor required for Firebase
    }

    public OrderItem(String state, String country, String address, String dateTime, String userEmail, String orderStatus, String username, String itemId, String contact) {
        this.state = state;
        this.country = country;
        this.address = address;
        this.dateTime = dateTime;
        this.userEmail = userEmail;
        this.orderStatus = orderStatus;
        this.username = username;
        this.itemId = itemId;
        this.contact = contact;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
