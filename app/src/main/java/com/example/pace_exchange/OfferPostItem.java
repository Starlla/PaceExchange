package com.example.pace_exchange;

public class OfferPostItem {



    private String offerID;
    private Post receiverPost;
    private Post senderPost;

    OfferPostItem(String offerID,Post receiverPost, Post senderPost){
        this.offerID = offerID;
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
}
