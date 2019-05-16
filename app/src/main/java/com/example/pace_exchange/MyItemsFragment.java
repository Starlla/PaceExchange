package com.example.pace_exchange;

import android.content.Context;
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pace_exchange.util.RecyclerViewMargin;
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
    ImageView mMyItemsProfilePhoto;
    RatingBar mProfileRating;
    DatabaseReference mDatabaseReference;
    ArrayList <Post> mPosts;
    ArrayList <String> mPostIds;
    String mUid;
    MyAdapter mMyAdapter;
    RecyclerView mRecyclerView;
    Toolbar toolbar;
    String targetItemPageType;

    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = Util.dpToPx(14);
    private static final String OTHER_USER_ITEMS= "other_user_items";
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
        mProfileRating = view.findViewById(R.id.my_items_profile_rating_bar);
        mMyItemsProfilePhoto = view.findViewById(R.id.profile_image2);
        mPosts = new ArrayList<>();
        mPostIds = new ArrayList<>();
        toolbar =view.findViewById(R.id.my_items_toolbar);


        setToolbar();
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext();
        if (context.getClass() == MainActivity.class) {
            Fragment fragment = ((MainActivity) context).currentFragment;
            if (fragment.getTag().equals(context.getString(R.string.fragment_shop))
            ||fragment.getTag().equals(context.getString(R.string.fragment_my_likes)) ) {
                targetItemPageType = OTHER_USER_ITEMS;
            }
        }
        Bundle args = getArguments();
        if (args != null){
            if(targetItemPageType !=null){
                if(targetItemPageType.equals(OTHER_USER_ITEMS)) mUid = args.getString(ViewPostFragment.WANT_POST_USER_UID);
            }
            else{ mUid = args.getString(ProfileFragment.ARG_UID);}

        }

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
        mMyAdapter = new MyAdapter(getActivity(), mPosts, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                Post post = mMyAdapter.getItem(position);
                viewPost(post.getPost_id(), post.getUser_id());
            }
        });
//        mMyAdapter.setFragmentTag(getString(R.string.fragment_my_items));
        mRecyclerView.setAdapter(mMyAdapter);

        Query query = mDatabaseReference.orderByChild("user_id").equalTo(mUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Util.getPostIdsThenGetPosts(mPostIds,mPosts,mMyAdapter,getString(R.string.node_inventories));
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
                        mProfileRating.setRating(user.getRating() == 0.0f ? 5.0f : user.getRating());
//                        UniversalImageLoader.setImage(user.getProfile_photo(),mMyItemsProfilePhoto);
                        Glide.with(getContext()).load(user.getProfile_photo()).into(mMyItemsProfilePhoto);
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
        getActivity().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void viewPost(String postId, String userId, String postStatus) {
        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        args.putString(getString(R.string.arg_user_id), userId);
        args.putString(getString(R.string.arg_post_status), postStatus);

        ViewPostFragment fragment = new ViewPostFragment();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
        fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
        fragmentTransaction.commit();
//        mFrameLayout.setVisibility(View.VISIBLE);
    }

}
