package com.example.pace_exchange;

public class OfferPostItem {
    //All information passed to MyOfferAdapter
    private String offerID;

    private String offerStatus;
    private Post receiverPost;
    private Post senderPost;

    OfferPostItem(String offerID,String offerStatus,Post receiverPost, Post senderPost){
        this.offerID = offerID;
        this.offerStatus = offerStatus;
        this.receiverPost = receiverPost;
        this.senderPost = senderPost;
    }

    public Post getReceiverPost() {
        return receiverPost;
    }

    public Post getSenderPost() {
        return senderPost;
    }

    public String getOfferID() {
        return offerID;
    }

    public String getOfferStatus() {
        return offerStatus;
    }
}
