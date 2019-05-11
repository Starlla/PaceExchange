package com.example.barterapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    static final String ARG_UID = "UID";


    interface ProfileFragmentButtonClickHandler{
        void signOutButtonClicked();
        void myItemsTabClicked(MyItemsFragment fragment);
        void myLikesTabClicked(MyLikesFragment fragment);
        void myOfferReceivedTabClicked(OfferReceivedFragment fragment);
        void myProfileTabClicked(MyProfileFragment fragment);
    }

    View mSignOutTab;
    TextView mProfileNameView;
    TextView mProfileEmailView;
    RatingBar mProfileRating;
    ProfileFragmentButtonClickHandler mClickHandler;
    String mUid;
    View mMyItemsTab;
    View mMyProfileTab;
    View mMyLikesTab;
    View mOfferReceivedTab;
    FrameLayout mFrameLayout;
    User user;

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
        mSignOutTab = view.findViewById(R.id.relLayout_sign_out);
        mProfileNameView = view.findViewById(R.id.profile_name);
        mProfileEmailView = view.findViewById(R.id.profile_email);
        mProfileRating = view.findViewById(R.id.profile_rating_bar);
        mMyItemsTab = view.findViewById(R.id.relLayout_my_items);
        mMyLikesTab = view.findViewById(R.id.relLayout_my_likes);
        mOfferReceivedTab= view.findViewById(R.id.relLayout_offer_received);
        mMyProfileTab = view.findViewById(R.id.relLayout_my_profile);
        mFrameLayout = view.findViewById(R.id.fragment_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSignOutTab.setOnClickListener(v -> {
            mClickHandler.signOutButtonClicked();
        });

        populateView();
        setMyItemsTabOnClickListener();
        setMyProfileTabOnClickedListener();
        setMyLikesTabOnClickListener();
        setMyOfferReceivedTabOnClickListener();
    }

    private void populateView(){
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString("uid");

//        DatabaseReference current_user_db_rf = FirebaseDatabase.getInstance().getReference().child("Users").child(mUid);
//        current_user_db_rf.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                mProfileNameView.setText((String)dataSnapshot.child("first_name").getValue());
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        getUserInfo();

    }

    public void setMyItemsTabOnClickListener(){
        mMyItemsTab.setOnClickListener(v->{
            Bundle args = new Bundle();
            args.putString(ARG_UID,mUid);
            MyItemsFragment fragment = new MyItemsFragment();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_my_items));
            fragmentTransaction.addToBackStack(getString(R.string.fragment_my_items));
            fragmentTransaction.commit();
//            mFrameLayout.setVisibility(View.VISIBLE);
            mClickHandler.myItemsTabClicked(fragment);

        });
    }

    public void setMyLikesTabOnClickListener(){
        mMyLikesTab.setOnClickListener(v->{
            Bundle args = new Bundle();
            args.putString(ARG_UID,mUid);
            MyLikesFragment fragment = new MyLikesFragment();
            fragment.setArguments(args);
            mClickHandler.myLikesTabClicked(fragment);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_my_likes));
            fragmentTransaction.addToBackStack(getString(R.string.fragment_my_likes));
            fragmentTransaction.commit();

        });
    }

    public void setMyOfferReceivedTabOnClickListener(){
        mOfferReceivedTab.setOnClickListener(v->{
            Bundle args = new Bundle();
            args.putString(ARG_UID,mUid);
            OfferReceivedFragment fragment = new OfferReceivedFragment();
            fragment.setArguments(args);
            mClickHandler.myOfferReceivedTabClicked(fragment);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_offer_received));
            fragmentTransaction.addToBackStack(getString(R.string.fragment_offer_received));
            fragmentTransaction.commit();

        });
    }



    public void setMyProfileTabOnClickedListener(){
        mMyProfileTab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString(ARG_UID,mUid);
            MyProfileFragment fragment = new MyProfileFragment();
            fragment.setArguments(args);
            mClickHandler.myProfileTabClicked(fragment);

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_my_profile_2));
            fragmentTransaction.addToBackStack(getString(R.string.fragment_my_profile_2));
            fragmentTransaction.commit();


        });

    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_users)).orderByKey().equalTo(mUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    if(dataSnapshot != null){
                        User user = singleSnapshot.getValue(User.class);
                        mProfileNameView.setText(getString(R.string.two_string_with_space,user.getFirst_name(),user.getLast_name()));
                        mProfileEmailView.setText(user.getEmail());
                        mProfileRating.setRating(user.getRating() == 0.0f ? 5.0f : user.getRating());
                        Glide.with(getContext()).load(user.getProfile_photo()).into((ImageView)getView().findViewById(R.id.profile_image));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




}
