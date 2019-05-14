package com.example.pace_exchange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    List<Post> mList;
    int mSelectedPosition = -1;

    public MyAdapter(Context context, List<Post> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = View.inflate(mContext, R.layout.item_view_abstract, null);
        return new MyAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final int position = i;
        Post post = mList.get(position);
        MyAdapterViewHolder holder = (MyAdapterViewHolder) viewHolder;
        holder.mTextView.setText(post.getTitle());
        Glide.with(mContext).load(post.getImage()).into(holder.mImageView);
        holder.mChecked.setVisibility(mSelectedPosition == position ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post post = mList.get(position);
                if (mContext.getClass() == MainActivity.class) {
                    Fragment fragment = ((MainActivity) mContext).currentFragment;
                    System.out.println(fragment.getTag());

                    if(fragment.getTag().equals(mContext.getString(R.string.fragment_shop))){
                        try {
                            ShopFragment mFragment = (ShopFragment) fragment;
                            mFragment.viewPost(post.getPost_id(), post.getUser_id());
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }

                    if(fragment.getTag().equals(mContext.getString(R.string.fragment_my_items))){
                        try {
                            MyItemsFragment mFragment = (MyItemsFragment) fragment;
                            mFragment.viewPost(post.getPost_id(),post.getUser_id());
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }

                    if(fragment.getTag().equals(mContext.getString(R.string.fragment_my_likes))){
                        try {
                            MyLikesFragment mFragment = (MyLikesFragment) fragment;
                            mFragment.viewPost(post.getPost_id(),post.getUser_id());
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fragment.getTag().equals(mContext.getString(R.string.fragment_offer_inventory))){
                        try {
                            OfferInventoryFragment mFragment = (OfferInventoryFragment) fragment;
                            mSelectedPosition = mSelectedPosition == position ? -1 : position;
                            notifyDataSetChanged();
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public String getSelectedPostId() {
        return mSelectedPosition == -1 ? "" : mList.get(mSelectedPosition).getPost_id();
    }

    public class MyAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;
        SquareImageView mImageView;
        ImageView mChecked;

        public MyAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.item_name);
            mImageView = itemView.findViewById(R.id.item_image);
            mChecked = itemView.findViewById(R.id.item_checked);

            // speed up performance
//            int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
//            int imageWidth = gridWidth / NUM_GRID_COLUMNS;
//            mImageView.setMaxHeight(imageWidth);
//            mImageView.setMaxWidth(imageWidth);
        }
    }
}