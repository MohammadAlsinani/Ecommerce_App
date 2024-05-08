package com.app.partyshopping;

public class User {
    private String username;
    private String email;
    private String imageUrl; // Add imageUrl field for storing the image URL

    public User() {
    }

    public User(String username, String email, String imageUrl) {
        this.username = username;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
