package com.example.pace_exchange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyOfferAdapter extends RecyclerView.Adapter<MyOfferAdapter.RecyclerViewHolder>{
    Context mContext;

    private int mSelectedPosition = RecyclerView.NO_POSITION;
    private OnReceivedOfferInteractionListener mReceivedListener;
    private OnSendOfferInteractionListener mSendListener;
    private OnConfirmedOfferInteractionListener mConfirmedListener;
    private ArrayList<OfferPostItem> mOfferList;
    private String fragmentTag = OfferReceivedFragment.TAG;

    public interface OnReceivedOfferInteractionListener{
        void onAcceptButtonClick(int position);
        void onRejectButtonClick(int position);
        void onRemoveButtonClick(int position);
        void onImageClick(String postId, String userId);
    }
    public interface OnSendOfferInteractionListener{
        void onCancelButtonClick(int position);
        void onImageClick(String postId, String userId);
    }

    public interface OnConfirmedOfferInteractionListener{
        void onRemoveButtonClick(int position);
        void onImageClick(String postId, String userId);
    }

    public void setReceivedOfferInteraction(OnReceivedOfferInteractionListener listener){
        if(fragmentTag.equals(OfferReceivedFragment.TAG)){
            mReceivedListener = listener;
        }
    }
    public void setSendOfferInteraction(OnSendOfferInteractionListener listener){
        if(fragmentTag.equals(OfferSendFragment.TAG)) {
            mSendListener = listener;
        }
    }
    public void setConfirmedOfferInteraction(OnConfirmedOfferInteractionListener listener){
        if(fragmentTag.equals(OfferConfirmedFragment.TAG)){
            mConfirmedListener = listener;
        }
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        private ImageView mReceiverImage;
        private ImageView mSenderImage;
        private TextView mReceiverStatusText;
        private TextView mSenderStatusText;
        private Button mAcceptButton;
        private Button mRejectButton;
        private Button mCancelButton;
        private Button mRemoveButton;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mReceiverImage = itemView.findViewById(R.id.offer_receiver_image);
            mSenderImage = itemView.findViewById(R.id.offer_sender_image);
            mReceiverStatusText = itemView.findViewById(R.id.offer_receiver_status_text);
            mSenderStatusText = itemView.findViewById(R.id.offer_sender_status_text);
            mAcceptButton = itemView.findViewById(R.id.offer_accept_button);
            mRejectButton = itemView.findViewById(R.id.offer_reject_button);
            mCancelButton = itemView.findViewById(R.id.offer_cancel_button);
            mRemoveButton = itemView.findViewById(R.id.offer_remove_button);

//            mAcceptButton.setOnClickListener(view ->  {
//                if (listener != null){
//                    int position = getAdapterPosition();
//                    if(position != RecyclerView.NO_POSITION){
//                        listener.onAcceptButtonClick(position);
//                    }
//                }
//            });
//
//            mRejectButton.setOnClickListener(view ->  {
//                if (listener != null){
//                    int position = getAdapterPosition();
//                    if(position != RecyclerView.NO_POSITION){
//                        listener.onRejectButtonClick(position);
//                    }
//                }
//            });
        }
    }

    public MyOfferAdapter(Context context,ArrayList<OfferPostItem> offerList){
        mContext= context;
        mOfferList = offerList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_view_abstract, parent, false);
        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(v);
        return recyclerViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder viewHolder, int position) {
        OfferPostItem currentOffer = mOfferList.get(position);
        if(!currentOffer.getOfferStatus().equals(Post.STATUS_VALUE_ACTIVE)){
            viewHolder.mReceiverStatusText.setVisibility(View.VISIBLE);
            viewHolder.mReceiverStatusText.setText(currentOffer.getOfferStatus());
            viewHolder.mSenderStatusText.setVisibility(View.VISIBLE);
            viewHolder.mSenderStatusText.setText(currentOffer.getOfferStatus());
            viewHolder.mAcceptButton.setVisibility(View.GONE);
            viewHolder.mRejectButton.setVisibility(View.GONE);
            viewHolder.mRemoveButton.setVisibility(View.VISIBLE);
        }

        if (fragmentTag.equals(OfferSendFragment.TAG)) {
            viewHolder.mAcceptButton.setVisibility(View.GONE);
            viewHolder.mRejectButton.setVisibility(View.GONE);
            viewHolder.mCancelButton.setVisibility(View.VISIBLE);
            viewHolder.mCancelButton.setOnClickListener(v ->
                    mSendListener.onCancelButtonClick(position));

            viewHolder.mReceiverImage.setOnClickListener(v ->
                    mSendListener.onImageClick(currentOffer.getReceiverPost().getPost_id(),
                            currentOffer.getReceiverPost().getUser_id()));

            viewHolder.mSenderImage.setOnClickListener(v ->
                    mSendListener.onImageClick(currentOffer.getSenderPost().getPost_id(),
                            currentOffer.getSenderPost().getUser_id()));
        } else if (fragmentTag.equals(OfferReceivedFragment.TAG)) {
            viewHolder.mAcceptButton.setOnClickListener(v -> {
                mReceivedListener.onAcceptButtonClick(position);
            });

            viewHolder.mRejectButton.setOnClickListener(v -> {
                mReceivedListener.onRejectButtonClick(position);
            });

            viewHolder.mReceiverImage.setOnClickListener(v ->
                    mReceivedListener.onImageClick(currentOffer.getReceiverPost().getPost_id(),
                            currentOffer.getReceiverPost().getUser_id()));

            viewHolder.mSenderImage.setOnClickListener(v ->
                    mReceivedListener.onImageClick(currentOffer.getSenderPost().getPost_id(),
                            currentOffer.getSenderPost().getUser_id()));
        } else {
            viewHolder.mAcceptButton.setVisibility(View.GONE);
            viewHolder.mRejectButton.setVisibility(View.GONE);
            viewHolder.mCancelButton.setVisibility(View.GONE);
            viewHolder.mRemoveButton.setVisibility(View.VISIBLE);

            viewHolder.mRemoveButton.setOnClickListener(v -> {
                mConfirmedListener.onRemoveButtonClick(position);
            });

            viewHolder.mReceiverImage.setOnClickListener(v ->
                    mConfirmedListener.onImageClick(currentOffer.getReceiverPost().getPost_id(),
                            currentOffer.getReceiverPost().getUser_id()));

            viewHolder.mSenderImage.setOnClickListener(v ->
                    mConfirmedListener.onImageClick(currentOffer.getSenderPost().getPost_id(),
                            currentOffer.getSenderPost().getUser_id()));
        }
        Glide.with(mContext).load(currentOffer.getReceiverPost().getImage()).into(viewHolder.mReceiverImage);
        Glide.with(mContext).load(currentOffer.getSenderPost().getImage()).into(viewHolder.mSenderImage);
    }

    public OfferPostItem getItem(int position) {
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

    public void setFragmentTag(String fragmentTag){
        this.fragmentTag = fragmentTag;
    }


}
