package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    interface ProfileFragmentButtonClickHandler{
        void signOutButtonClicked();
    }

    Button mSignOutButton;
    TextView mProfileNameView;
    ProfileFragmentButtonClickHandler mClickHandler;
    String mUid;
    View mMyItemsTab;
    FrameLayout mFrameLayout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mClickHandler = (ProfileFragmentButtonClickHandler) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mSignOutButton = view.findViewById(R.id.sign_out_test);
        mProfileNameView = view.findViewById(R.id.profile_name);
        mMyItemsTab = view.findViewById(R.id.relLayout_my_items);
        mFrameLayout = view.findViewById(R.id.fragment_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSignOutButton.setOnClickListener(v -> {
            mClickHandler.signOutButtonClicked();
        });

        populateView();
        setMyItemsTabOnClickListener();

    }

    private void populateView(){
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString("uid");

        DatabaseReference current_user_db_rf = FirebaseDatabase.getInstance().getReference().child("Users").child(mUid);
        current_user_db_rf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProfileNameView.setText((String)dataSnapshot.child("name").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setMyItemsTabOnClickListener(){
        mMyItemsTab.setOnClickListener(v->{
            Bundle args = new Bundle();
            args.putString("Uid",mUid);
            MyItemsFragment fragment = new MyItemsFragment();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_my_items));
            fragmentTransaction.addToBackStack(getString(R.string.fragment_my_items));
            fragmentTransaction.commit();

//            mFrameLayout.setVisibility(View.VISIBLE);
        });
    }



}
