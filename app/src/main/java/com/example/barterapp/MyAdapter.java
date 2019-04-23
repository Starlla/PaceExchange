package com.example.barterapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    List<TradeItem> mList;

    private static final int NUM_GRID_COLUMNS = 2;

    public MyAdapter(Context context, List<TradeItem> list) {
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
        TradeItem tradeItem = mList.get(position);
        MyAdapterViewHolder holder = (MyAdapterViewHolder) viewHolder;
        holder.mTextView.setText(tradeItem.getTitle());
        Glide.with(mContext).load(tradeItem.getImage()).into(holder.mImageView);
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // to be filled
                ShopFragment shopFragment = (ShopFragment) ((MainActivity)mContext).getSupportFragmentManager()
                        .findFragmentByTag(mContext.getString(R.string.fragment_search));
                if (shopFragment != null && shopFragment.isVisible()) {
                    shopFragment.viewPost(mList.get(position).getPost_id());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;
        SquareImageView mImageView;

        public MyAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.item_name);
            mImageView = itemView.findViewById(R.id.item_image);

            int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            int imageWidth = gridWidth / NUM_GRID_COLUMNS;
            mImageView.setMaxHeight(imageWidth);
            mImageView.setMaxWidth(imageWidth);
        }
    }
}
