package com.example.howsMyStylist.Model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;

public class Review {
    private String stylistName;
    private String salonName;
    private String service;
    private double price;
    private String date;
    private String review;
    private double rating;
    private String userId;
    private String username;
    private String userProfilePic;
    private ArrayList<String> images;

    public Review(){};
    public Review(String stylistName, String service, double price, String date, double rating, String userId, String username, String userProfilePic) {
        this.stylistName = stylistName;
        this.service = service;
        this.price = price;
        this.date = date;
        this.rating = rating;
        this.userId = userId;
        this.username = username;
        this.userProfilePic = userProfilePic;
    }

    public Review(String stylistName, String salonName, String serviceName, double price, String date,
                  String review, double rating, String userId) {
        this.stylistName = stylistName;
        this.salonName = salonName;
        this.service = serviceName;
        this.price = price;
        this.date = date;
        this.rating = rating;
        this.review = review;
        this.userId = userId;

    }

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date.toString();
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getUserProfilePic() { return userProfilePic; }

    public void setUserProfilePic(String userProfilePic) { this.userProfilePic = userProfilePic; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getUserId() {
        return userId;
    }

    public ArrayList<String> getImages() { return images;}
}
