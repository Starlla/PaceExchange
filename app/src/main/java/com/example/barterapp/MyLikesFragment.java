package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
//    private ArrayList<String> mPostsIds;

    private static final int NUM_GRID_COLUMNS = 2;
    private static final String TAG = "MyLikesFragment";

    public MyLikesFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_likes, container, false);
        mRecyclerView = view.findViewById(R.id.my_likes_recycler_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString(ProfileFragment.ARG_UID);
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
    }

    private void init(){
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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mMyAdapter);

//        Query query = mDatabaseReference.orderByChild("user_id").equalTo(mUid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.hasChildren()) {
//                    mItems.clear();
//                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
//                        final Post post = dss.getValue(Post.class);
//                        mItems.add(post);
//                    }
//                    mMyAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users watch lits node.");
            getLikesListIds();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void getLikesListIds(){
        Log.d(TAG, "getWatchListIds: getting users watch list.");
        if(mPosts != null){
            mPosts.clear();
        }
        if(mPostsIds != null){
            mPostsIds.clear();
        }

        mPostsIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_likes))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.child(getString(R.string.field_post_id)).getValue().toString();
                        Log.d(TAG, "onDataChange: found a post id: " + id);
                        mPostsIds.add(id);
                    }
                    getPosts();
                }else{
                    getPosts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPosts(){
        if(mPostsIds.size() > 0){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for(int i  = 0; i < mPostsIds.size(); i++){
                Log.d(TAG, "getPosts: getting post information for: " + mPostsIds.get(i));

                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mPostsIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Post post = singleSnapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                        mPosts.add(post);
                        mMyAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            mMyAdapter.notifyDataSetChanged(); //still need to notify the adapter if the list is empty
        }
    }


}
