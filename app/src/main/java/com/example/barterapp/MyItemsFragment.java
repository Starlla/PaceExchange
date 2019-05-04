package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.barterapp.util.RecyclerViewMargin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MyItemsFragment extends Fragment {

    TextView mMyItemsProfileNameView;
    TextView mMyItemsEmailView;
    DatabaseReference mDatabaseReference;
    List<Post> mItems;
    String mUid;
    MyAdapter mMyAdapter;
    RecyclerView mRecyclerView;
    Toolbar toolbar;

    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = Util.dpToPx(10);
    static final String ARG_UID = "UID";

    interface MyItemsFragmentButtonClickHandler{
        void signOutButtonClicked();
        void myItemsTabClicked();
    }

    private MyItemsFragmentButtonClickHandler mClickHandler;

    public MyItemsFragment() {

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

        View view = inflater.inflate(R.layout.fragment_my_items, container, false);
        mRecyclerView = view.findViewById(R.id.my_items_recycler_view);
        mMyItemsProfileNameView = view.findViewById(R.id.my_items_profile_name);
        mMyItemsEmailView = view.findViewById(R.id.my_items_profile_email);
        mItems = new ArrayList<>();
        toolbar =view.findViewById(R.id.my_items_toolbar);
        setToolbar();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString(ProfileFragment.ARG_UID);

        getUserInfo();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.node_posts));
        configureRecyclerView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mClickHandler = (MyItemsFragment.MyItemsFragmentButtonClickHandler) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickHandler = null;
    }

    private void configureRecyclerView() {

        mRecyclerView.setHasFixedSize(true);
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getActivity(), mItems);
        mRecyclerView.setAdapter(mMyAdapter);

        Query query = mDatabaseReference.orderByChild("user_id").equalTo(mUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mItems.clear();
                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                        final Post post = dss.getValue(Post.class);
                        mItems.add(post);
                    }
                    mMyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
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
                        mMyItemsProfileNameView.setText(getString(R.string.two_string_with_space,user.getFirst_name(),user.getLast_name()));
                        mMyItemsEmailView.setText(user.getEmail());
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
        getActivity().setTitle("My Items");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void viewPost(String postId, String userId) {
        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        args.putString(getString(R.string.arg_user_id), userId);
        ViewPostFragment fragment = new ViewPostFragment();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
        fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
        fragmentTransaction.commit();
//        mFrameLayout.setVisibility(View.VISIBLE);
    }




}
