package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MyProfileFragment extends Fragment {

    interface MyProfileFragmentButtonClickHandler{

    }

    private MyProfileFragmentButtonClickHandler mClickHandler;

    public MyProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mClickHandler = (MyProfileFragment.MyProfileFragmentButtonClickHandler) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickHandler = null;
    }


}
