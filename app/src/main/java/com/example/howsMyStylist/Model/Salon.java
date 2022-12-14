package com.example.howsMyStylist.Model;

public class Salon {
    private String salonName;
    private String phone;
    private String address;
    private String country;
    private String state;
    private String city;
    private String zip;
    private String webLink;
    private String uriImage;
    private double avgRating;

    public Salon() {

    }

    public Salon(String salonName, String phone, String address, String country, String state, String city, String zip, String webLink, String uriImage, double avgRating) {
        this.salonName = salonName;
        this.phone = phone;
        this.address = address;
        this.country = country;
        this.state = state;
        this.city = city;
        this.zip = zip;
        this.webLink = webLink;
        this.uriImage = uriImage;
        this.avgRating = avgRating;
    }

    public Salon(String name){
        this.salonName = name;
    }



    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getUriImage() {
        return uriImage;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}
