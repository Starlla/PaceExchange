package com.example.barterapp;

public class User {
    private String user_id;
    private String email;
    private String first_name;
    private String last_name;
    private String gender;
    private String profile_photo;
    private int graduation_year;
    private float rating;

    public User(String user_id, String email, String first_name, String last_name, String gender,
                String profile_photo, int graduation_year, float rating) {
        this.user_id = user_id;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.gender = gender;
        this.profile_photo = profile_photo;
        this.graduation_year = graduation_year;
        this.rating = rating;
    }

    public User() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getGraduation_year() {
        return graduation_year;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setGraduation_year(int graduation_year) {
        this.graduation_year = graduation_year;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", gender='" + gender + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", graduation_year=" + graduation_year +
                ", rating=" + rating +
                '}';
    }
}
