package com.app.partyshopping;

public class Complaint {
    private String username;
    private String userEmail;
    private String complaint;
    private boolean responseReceived; // Add a boolean field for response status


    public Complaint() {
        // Default constructor required for Firestore
    }

    public Complaint(String username, String userEmail, String complaint) {
        this.username = username;
        this.userEmail = userEmail;
        this.complaint = complaint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public boolean isResponseReceived() {
        return responseReceived;
    }

    public void setResponseReceived(boolean responseReceived) {
        this.responseReceived = responseReceived;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }
}
