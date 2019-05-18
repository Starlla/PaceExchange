package com.example.pace_exchange;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {


//    public static ProfileFragment newInstance(String uid) {
//        ProfileFragment fragment = new ProfileFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_UID, uid);
//        fragment.setArguments(args);
//        return fragment;
//    }


    interface ProfileFragmentButtonClickHandler{
        void signOutButtonClicked();
        void myItemsTabClicked();
        void myLikesTabClicked();
        void myOfferTabClicked();
        void myProfileTabClicked();
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
    View mOfferTab;
    FrameLayout mFrameLayout;
    User user;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mClickHandler = (ProfileFragmentButtonClickHandler) context;
        }catch(ClassCastException e){
            throw new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");

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
        mOfferTab= view.findViewById(R.id.relLayout_offer);
        mMyProfileTab = view.findViewById(R.id.relLayout_my_profile);
        mFrameLayout = view.findViewById(R.id.fragment_container);
        mUid = FirebaseAuth.getInstance().getUid();
        mFrameLayout=(FrameLayout)view.findViewById(R.id.container);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSignOutTab.setOnClickListener(v -> {
            mClickHandler.signOutButtonClicked();
        });

        mMyItemsTab.setOnClickListener(v -> {
            mClickHandler.myItemsTabClicked();
        });
       mMyLikesTab.setOnClickListener(v->{
           mClickHandler.myLikesTabClicked();
       });
       mMyProfileTab.setOnClickListener(v->{
           mClickHandler.myProfileTabClicked();
       });
       mOfferTab.setOnClickListener(v -> {
           mClickHandler.myOfferTabClicked();
       });

        populateView();
    }

    private void populateView(){
        Bundle args = getArguments();
        if (args != null) {
            mUid = args.getString(MainActivity.ARG_UID);
        }
        getUserInfo();
    }



    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("Users").orderByKey().equalTo(FirebaseAuth.getInstance().getUid());
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
