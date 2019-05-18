package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pace_exchange.util.RecyclerViewMargin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MyLikesFragment extends Fragment {
    public interface OnMyLikesFragmentInteractionListener {

    }

    private OnMyLikesFragmentInteractionListener mListener;
    DatabaseReference mDatabaseReference;
    List<Post> mItems;
    String mUid;
    MyAdapter mMyAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<Post> mPosts;
    private ArrayList<String> mPostsIds;
    Toolbar toolbar;

    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = Util.dpToPx(14);
    private static final String TAG = "MyLikesFragment";
    public MyLikesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_likes, container, false);
        mRecyclerView = view.findViewById(R.id.my_likes_recycler_view);
        toolbar = view.findViewById(R.id.my_likes_toolbar);
        setToolbar();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString(MainActivity.ARG_UID);
        init();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (MyLikesFragment.OnMyLikesFragmentInteractionListener) context;
        }catch(ClassCastException e){
             new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mDatabaseReference.removeEventListener(mValueEventListener);
    }

    private void init(){
        mPostsIds = new ArrayList<>();
        mPosts = new ArrayList<>();
        setUpRecyclerView();

        //reference for listening when items are added or removed from the watch list
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        //set the listener to the reference
        mDatabaseReference.addValueEventListener(mValueEventListener);
    }


    private void setUpRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getActivity(), mPosts,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                Post post = mMyAdapter.getItem(position);
                viewPost(post.getPost_id(), post.getUser_id(), post.getStatus());
            }
        });
//        mMyAdapter.setFragmentTag(getString(R.string.fragment_my_likes));
        mRecyclerView.setAdapter(mMyAdapter);
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users watch list node.");
            Util.getPostIdsThenGetPosts(mUid, mPostsIds,mPosts,mMyAdapter,getString(R.string.node_likes));
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setToolbar(){

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.removeEventListener(mValueEventListener);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void viewPost(String postId, String userId, String postStatus) {
        Bundle args = new Bundle();
        args.putString(MainActivity.ARG_POST_ID, postId);
        args.putString(MainActivity.ARG_UID, userId);
        args.putString(MainActivity.ARG_POST_STATUS, postStatus);
        ViewPostFragment fragment = new ViewPostFragment();
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
        fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
        fragmentTransaction.commit();
    }




}
