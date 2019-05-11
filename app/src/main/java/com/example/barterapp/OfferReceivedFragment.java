package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


public class OfferReceivedFragment extends Fragment {

    public interface OnOfferReceivedFragmentInteractionListener {

    }

    private MyLikesFragment.OnMyLikesFragmentInteractionListener mListener;
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
    private static final String TAG = "OfferReceivedFragment";


    private OnOfferReceivedFragmentInteractionListener mClickHandler;

    public OfferReceivedFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_received, container, false);
        mRecyclerView = view.findViewById(R.id.offer_received_recycler_view);
        toolbar = view.findViewById(R.id.my_likes_toolbar);
//        setToolbar();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mClickHandler = (OfferReceivedFragment.OnOfferReceivedFragmentInteractionListener) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
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
        public void onDetach () {
            super.onDetach();
            mListener = null;
        }


    private void init () {
        mPosts = new ArrayList<>();
        setUpRecyclerView();
        //reference for listening when items are added or removed from the watch list
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_offers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReference.addValueEventListener(mValueEventListener);

    }

    private void setUpRecyclerView () {
        mRecyclerView.setHasFixedSize(true);
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mMyAdapter);


    }

    private void getOfferReciedListIds(){
        if(mPosts != null){
            mPosts.clear();
        }
        if(mPostsIds != null){
            mPostsIds.clear();
        }

        mPostsIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_offers))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.getKey().toString();
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
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            Post post = singleSnapshot.getValue(Post.class);
                            Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                            mPosts.add(post);
                            mMyAdapter.notifyDataSetChanged();
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            mMyAdapter.notifyDataSetChanged();
        }
    }



    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users watch list node.");
            getOfferReciedListIds();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}




