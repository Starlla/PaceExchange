package com.example.pace_exchange;

public class Post {

    private String post_id;
    private String user_id;
    private String image;
    private String title;
    private String description;
    private String status;

    //when post first created,the status is "ACTIVE"
    public static final String STATUS_VALUE_ATIVE = "ACTIVE";
    public static final String STATUS_VALUE_INATIVE = "INACTIVE";
    public static final String STATUS_VALUE_LOCKED = "LOCKED";
    public static final String STATUS_VALUE_TRADED = "TRADED";

    public Post(String post_id, String user_id, String image, String title, String description, String status) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.image = image;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Post() {

    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Post{" +
                "post_id='" + post_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", image='" + image + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

