package com.example.pace_exchange;

public class Offer {
    private Post receiverPost;
    private Post senderPost;

    Offer(Post receiverPost,Post senderPost){
        this.receiverPost = receiverPost;
        this.senderPost = senderPost;
    }

    public Post getReceiverPost() {
        return receiverPost;
    }

    public Post getSenderPost() {
        return senderPost;
    }
}
