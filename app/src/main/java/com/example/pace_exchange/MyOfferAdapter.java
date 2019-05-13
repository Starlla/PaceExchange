package com.example.pace_exchange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyOfferAdapter extends RecyclerView.Adapter<MyOfferAdapter.RecyclerViewHolder>{
    Context mContext;

    private int mSelectedPosition = RecyclerView.NO_POSITION;
    private OnItemClickListener mListener;
    private ArrayList<Offer> mOfferList;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener =listener;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        public ImageView mReceiverImage;
        public ImageView mSenderImage;


        public RecyclerViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mReceiverImage = itemView.findViewById(R.id.offer_receiver_image);
            mSenderImage = itemView.findViewById(R.id.offer_sender_image);

            itemView.setOnClickListener(view ->  {
                if (listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public MyOfferAdapter(Context context,ArrayList<Offer> offerList){
        mContext= context;
        mOfferList = offerList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_view_abstract, parent, false);
        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(v,  mListener);
        return recyclerViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder viewHolder, int position) {
        Offer currentOffer = mOfferList.get(position);
        Glide.with(mContext).load(currentOffer.getReceiverPost().getImage()).into(viewHolder.mReceiverImage);
        Glide.with(mContext).load(currentOffer.getSenderPost().getImage()).into(viewHolder.mSenderImage);

    }

    public Offer getItem(int position) {
        return mOfferList.get(position);
    }

    @Override
    public int getItemCount() {
        return mOfferList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public int getSelectedPosition(){return mSelectedPosition;}

    public void setSelectedPosition(int position) {
        mSelectedPosition =  position ==  mSelectedPosition ? Adapter.NO_SELECTION : position;
    }

    public boolean isPositionSelected() {
        return mSelectedPosition != Adapter.NO_SELECTION;
    }


}
