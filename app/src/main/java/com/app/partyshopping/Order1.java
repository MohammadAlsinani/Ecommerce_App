package com.app.partyshopping;

import java.util.List;

public class Order1 {
    private String itemTitle;
    private String itemPrice;
    private String itemId;
    private List<String> itemImageUrls;
    private String dateTime;
    private String orderId;
    private String orderStatus;

    public Order1() {
        // Default constructor required for Firebase
    }

    public Order1(String itemTitle, String itemPrice, String itemId, List<String> itemImageUrls, String dateTime, String orderId, String orderStatus) {
        this.itemTitle = itemTitle;
        this.itemPrice = itemPrice;
        this.itemId = itemId;
        this.itemImageUrls = itemImageUrls;
        this.dateTime = dateTime;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<String> getItemImageUrls() {
        return itemImageUrls;
    }

    public void setItemImageUrls(List<String> itemImageUrls) {
        this.itemImageUrls = itemImageUrls;
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

    @Override
    public String toString() {
        return "Order1{" +
                "itemTitle='" + itemTitle + '\'' +
                ", itemPrice='" + itemPrice + '\'' +
                ", itemId='" + itemId + '\'' +
                ", itemImageUrls=" + itemImageUrls +
                ", dateTime='" + dateTime + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}
