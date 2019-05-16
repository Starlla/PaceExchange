package com.example.pace_exchange;

public class Offer {

    private String offer_id;
    private String receiver_post_id;
    private String sender_post_id;
    private String receiver_uid;
    private String sender_uid;

    private String status;

    public Offer(String offer_id, String receiver_post_id, String sender_post_id, String receiver_uid, String sender_uid,String status) {
        this.offer_id = offer_id;
        this.receiver_post_id = receiver_post_id;
        this.sender_post_id = sender_post_id;
        this.receiver_uid = receiver_uid;
        this.sender_uid = sender_uid;
        this.status = status;
    }


    public Offer() {
    }

    public String getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(String offer_id) {
        this.offer_id = offer_id;
    }

    public String getReceiver_post_id() {
        return receiver_post_id;
    }

    public void setReceiver_post_id(String receiver_post_id) {
        this.receiver_post_id = receiver_post_id;
    }

    public String getSender_post_id() {
        return sender_post_id;
    }

    public void setSender_post_id(String sender_post_id) {
        this.sender_post_id = sender_post_id;
    }

    public String getReceiver_uid() {
        return receiver_uid;
    }

    public void setReceiver_uid(String receiver_uid) {
        this.receiver_uid = receiver_uid;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Offer{" +
                "offer_id='" + offer_id + '\'' +
                ", receiver_post_id='" + receiver_post_id + '\'' +
                ", sender_post_id='" + sender_post_id + '\'' +
                ", receiver_uid='" + receiver_uid + '\'' +
                ", sender_uid='" + sender_uid + '\'' +
                ", status='" + status + '\'' +
                '}';
    }


}
