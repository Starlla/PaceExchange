package com.example.barterapp;

public class TradeItem {
    private String mItemId;
    private String mUserId;
    private String mImage;
    private String mTitle;
    private String mDescription;

    public TradeItem(String itemId, String userId, String image, String title, String description) {
        mItemId = itemId;
        mUserId = userId;
        mImage = image;
        mTitle = title;
        mDescription = description;
    }

    public TradeItem() {
    }

    public String getItemId() {
        return mItemId;
    }

    public void setItemId(String itemId) {
        mItemId = itemId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    @Override
    public String toString() {
        return "TradeItem{" +
                "mItemId='" + mItemId + '\'' +
                ", mUserId='" + mUserId + '\'' +
                ", mImage='" + mImage + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mDescription='" + mDescription + '\'' +
                '}';
    }
}
