package com.example.barterapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SelectGenderDialog extends DialogFragment {
    private static final String TAG = "SelectGenderDialog";
    TextView femaleView;
    TextView maleView;
    TextView unspecifiedView;

    public interface OnGenderSelectedListener{
        void setGenderFemale();
        void setGenderMale();
        void setGenderUnspecified();
    }
    OnGenderSelectedListener mOnGenderSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_gender, container, false);
        femaleView = view.findViewById(R.id.dialog_female);
        maleView = view.findViewById(R.id.dialog_male);
        unspecifiedView= view.findViewById(R.id.dialog_unspecified);

        setGenderSelectionOnClickListener();
        return view;
    }

    private void setGenderSelectionOnClickListener() {
        femaleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Selected Female");
                mOnGenderSelectedListener.setGenderFemale();
                getDialog().dismiss();
            }
        });

        maleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Selected Male");
                mOnGenderSelectedListener.setGenderMale();
                getDialog().dismiss();
            }
        });

        unspecifiedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Selected Male");
                mOnGenderSelectedListener.setGenderUnspecified();
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnGenderSelectedListener = (SelectGenderDialog.OnGenderSelectedListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
        super.onAttach(context);
    }


}
