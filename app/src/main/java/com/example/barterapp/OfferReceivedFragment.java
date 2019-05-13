package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
    String mUid;
    MyOfferAdapter mMyOfferAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<Offer> mOfferList;
    private ArrayList<Post> mReceivedOfferItems;
    private ArrayList<Post> mSendOfferItems;
    private ArrayList<String> mReceivedOfferItemIds;
    private ArrayList<String> mSendOfferItemIds;
    private Toolbar toolbar;
    private DatabaseReference reference;

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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_received, container, false);
        mRecyclerView = view.findViewById(R.id.offer_received_recycler_view);
        toolbar = view.findViewById(R.id.offer_received_toolbar);
        mOfferList = new ArrayList<>();
        mReceivedOfferItems =new ArrayList<>();
        mReceivedOfferItemIds = new ArrayList<>();
        mSendOfferItemIds = new ArrayList<>();
        mSendOfferItems = new ArrayList<>();
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
        setToolbar();

        setUpRecyclerView();
        reference = FirebaseDatabase.getInstance().getReference();
        //reference for listening when items are added or removed from the offer list
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_offers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReference.addValueEventListener(mValueEventListener);

    }

    private void setUpRecyclerView () {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mMyOfferAdapter = new MyOfferAdapter(getActivity(), mOfferList);
        mRecyclerView.setAdapter(mMyOfferAdapter);
    }

    private void readData(MyCallback myCallback) {

        Query query = reference.child(getString(R.string.node_offers))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String receivedOfferItemID = snapshot.getKey();
                        mReceivedOfferItemIds.add(receivedOfferItemID);
                    }
                    myCallback.onCallback(mReceivedOfferItemIds);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void readPostData(MyPostCallback myPostCallback){
        if (mReceivedOfferItemIds.size() > 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for (int i = 0; i < mReceivedOfferItemIds.size(); i++) {
                Log.d(TAG, "getPosts: getting post information for: " + mReceivedOfferItemIds.get(i));
                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mReceivedOfferItemIds.get(i));

                int finalI = i;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            Post post = singleSnapshot.getValue(Post.class);
                            Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                            mReceivedOfferItems.add(post);

                            if(mReceivedOfferItemIds.size() == finalI+1){
                                myPostCallback.onPostCallback(mReceivedOfferItems);
                            }
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }



    public interface MyCallback {
        void onCallback(ArrayList<String> postIDList);
    }

    public interface MyPostCallback {
        void onPostCallback(ArrayList<Post> postList);
    }


    private void getSingleItemOfferReceivedList(ArrayList<Post> mReceivedOfferItems){
        if(mReceivedOfferItems.size() > 0){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            for(int i  = 0; i < mReceivedOfferItems.size(); i++){
                mSendOfferItemIds.clear();
                Query query = reference.child(getString(R.string.node_offers))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderByKey()
                        .equalTo(mReceivedOfferItems.get(i).getPost_id());
                Post current = mReceivedOfferItems.get(i);
                int finalI = i;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildren().iterator().hasNext()){
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            for(DataSnapshot snapshot: singleSnapshot.getChildren()) {
                                String id = snapshot.getKey().toString();
                                mSendOfferItemIds.add(id);
                            }
                            getSendOfferItemList(mReceivedOfferItems.get(finalI));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
    }

    private void getSendOfferItemList(Post current) {
        if (mSendOfferItemIds.size() > 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for (int i = 0; i < mSendOfferItemIds.size(); i++) {
                Log.d(TAG, "getPosts: getting post information for: " + mSendOfferItemIds.get(i));
                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mSendOfferItemIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            Post post = singleSnapshot.getValue(Post.class);
                            Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
//                          mSendOfferItems.add(post);
                            Offer offer = new Offer(current, post);
                            mOfferList.add(offer);
                            mMyOfferAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }



    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users offer received node.");
            readData(new MyCallback() {
                @Override
                public void onCallback(ArrayList<String> postIDList) {
                    System.out.println("1stCALLBACK"+postIDList.size());
                    System.out.println(postIDList.get(0));
                    System.out.println(postIDList.get(1));
                    mReceivedOfferItemIds = postIDList;
                    readPostData(new MyPostCallback() {
                        @Override
                        public void onPostCallback(ArrayList<Post> postList) {
                            System.out.println("2stCALLBACK"+postList.size());
                            getSingleItemOfferReceivedList(postList);

                        }
                    });
                }
            });

        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setToolbar(){

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle("Offer Recieved");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }


}




