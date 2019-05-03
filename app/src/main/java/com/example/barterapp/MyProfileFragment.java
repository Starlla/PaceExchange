package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MyProfileFragment extends Fragment {



    interface MyProfileFragmentButtonClickHandler{

    }

    private MyProfileFragmentButtonClickHandler mClickHandler;
    String mUid;
    TextView mMyProfileNameView;
    TextView mMyProfileEmailView;
    Toolbar toolbar;

    public MyProfileFragment() {
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
        toolbar = view.findViewById(R.id.my_profile_toolbar);
        mMyProfileNameView = view.findViewById(R.id.my_profile_name);
        mMyProfileEmailView = view.findViewById(R.id.my_profile_email);
        setToolbar();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickHandler = null;
    }

    private void populateView(){
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString(ProfileFragment.ARG_UID);

        getUserInfo();

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
                        mMyProfileNameView.setText(getString(R.string.two_string_with_space,user.getFirst_name(),user.getLast_name()));
                        mMyProfileEmailView.setText(user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setToolbar(){

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle("My Profile");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }



}
