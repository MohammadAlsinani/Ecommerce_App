package com.app.partyshopping;

public class ComplaintResponse {
    private String userMessage;
    private String response;

    public ComplaintResponse() {
        // Default constructor required for Firestore
    }

    public ComplaintResponse(String userMessage, String response) {
        this.userMessage = userMessage;
        this.response = response;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
