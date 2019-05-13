package com.example.pace_exchange.util;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecyclerViewMargin extends RecyclerView.ItemDecoration {
    private final int columns;
    private int margin;


    public RecyclerViewMargin(int margin, int columns) {
        this.columns = columns;
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);



        outRect.top = margin/2;
        outRect.bottom = margin/2;


        if(position % columns == 1){
            outRect.right = margin;
            outRect.left = margin/2;
        }

        if(position % columns == 0) {
            outRect.left = margin;
            outRect.right = margin/2;
        }


    }
}
