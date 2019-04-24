package com.example.barterapp;

public class User {
    private String user_id;
    private String name;

    // change structure to first name, last name, email address, graduation year & reputation later

    public User(String user_id, String name) {
        this.user_id = user_id;
        this.name = name;
    }

    public User() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
