package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class OfferConfirmedFragment extends Fragment {

    public interface OnOfferConfirmedFragmentInteractionListener {

    }

    private OfferConfirmedFragment.OnOfferConfirmedFragmentInteractionListener mListener;
    DatabaseReference mDatabaseReferenceConfirmSide;
    String mUid;
    MyOfferAdapter mMyOfferAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<OfferPostItem> mOfferList;
    private ArrayList<String> mOffersIds;
    private ArrayList<Offer> mOffers;
    DatabaseReference mDatabaseReference;
    protected static final String TAG = "OfferConfirmedFragment";
    public OfferConfirmedFragment() {
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
        View view = inflater.inflate(R.layout.fragment_offer_page, container, false);
        mRecyclerView = view.findViewById(R.id.offer_page_recycler_view);
        mOfferList = new ArrayList<>();
        mOffersIds = new ArrayList<>();
        mOffers = new ArrayList<>();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (OfferConfirmedFragment.OnOfferConfirmedFragmentInteractionListener) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onDetach () {
        super.onDetach();
        mListener = null;
        mDatabaseReferenceConfirmSide.removeEventListener(mValueEventListener);
    }

    private void init () {
        setUpRecyclerView();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //reference for listening when items are added or removed from the offer list
        mDatabaseReferenceConfirmSide = mDatabaseReference
                .child(getString(R.string.node_offer_confirmed))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReferenceConfirmSide.addValueEventListener(mValueEventListener);
    }

    private void setUpRecyclerView () {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mMyOfferAdapter = new MyOfferAdapter(getActivity(), mOfferList);
        mMyOfferAdapter.setFragmentTag(TAG);
        mMyOfferAdapter.setGeneralSituationInteration(new MyOfferAdapter.OnGeneralSituationInterationListener() {
            @Override
            public void onRemoveButtonClick(int position) {
                OfferPostItem offerPostItem = mOfferList.get(position);
                String senderId = offerPostItem.getSenderPost().getUser_id();
                String receiverId = offerPostItem.getReceiverPost().getUser_id();
                String offerId = offerPostItem.getOfferID();
                String currentOfferStatus = offerPostItem.getOfferStatus();

                // Make Sure offer status is TRADED
                if(currentOfferStatus.equals(MainActivity.STATUS_VALUE_TRADED)){
                    mDatabaseReference.child(getString(R.string.node_offer_confirmed))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(offerId)
                            .removeValue();

                    // Check if offer confirmed record also remove by the other user, if so, remove offer
                    String otherUserId = senderId == FirebaseAuth.getInstance().getCurrentUser().getUid() ? receiverId : senderId;
                    Query query = mDatabaseReference.child(getString(R.string.node_offer_confirmed))
                            .child(otherUserId)
                            .equalTo(offerId);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                mDatabaseReference.child(getString(R.string.node_offers))
                                        .child(offerId)
                                        .removeValue();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                mMyOfferAdapter.notifyDataSetChanged();
            }

            // change to use Interface!!!!
            @Override
            public void onImageClick(String postId, String userId, String postStatus) {
                Bundle args = new Bundle();
                args.putString(MainActivity.ARG_POST_ID, postId);
                args.putString(MainActivity.ARG_UID, userId);
                args.putString(MainActivity.ARG_POST_STATUS, postStatus);
                args.putString(MainActivity.ARG_SPECIAL_CODE, MainActivity.NO_ACTION);
                ViewPostFragment fragment = new ViewPostFragment();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
                fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
                fragmentTransaction.commit();
            }
        });

        mMyOfferAdapter.setConfirmedOfferInteraction(new MyOfferAdapter.OnConfirmedOfferInteractionListener() {
            @Override
            public void onFinishedButtonClick(int position) {
                OfferPostItem offerPostItem = mOfferList.get(position);
                String senderPostId = offerPostItem.getSenderPost().getPost_id();
                String receiverPostId = offerPostItem.getReceiverPost().getPost_id();
                String senderUserId = offerPostItem.getSenderPost().getUser_id();
                String receiverUserId = offerPostItem.getReceiverPost().getUser_id();
                String offerId = offerPostItem.getOfferID();
                String currentOfferStatus = offerPostItem.getOfferStatus();
                String mUid = FirebaseAuth.getInstance().getUid();

                //Mark Offer status to PARTIAL_FINISHED(1 side trader finish) or TRADED(both confirmed finished)
                if(currentOfferStatus.equals(MainActivity.STATUS_VALUE_LOCKED)) {
                    //I'm the sender
                    if (mUid .equals( senderUserId)) {
                        mDatabaseReference.child(getString(R.string.node_offers))
                                .child(offerId)
                                .child(getString(R.string.field_status)).setValue(MainActivity.STATUS_VALUE_SENDER_FINISHED);
                        //I'm the receiver
                    }
                    if (mUid .equals(receiverUserId)) {
                        mDatabaseReference.child(getString(R.string.node_offers))
                                .child(offerId)
                                .child(getString(R.string.field_status)).setValue(MainActivity.STATUS_VALUE_RECEIVER_FINISHED); }
                    //Mark Offer Traded
                } else if (currentOfferStatus.equals(MainActivity.STATUS_VALUE_SENDER_FINISHED) ||
                        currentOfferStatus.equals(MainActivity.STATUS_VALUE_RECEIVER_FINISHED)) {
                    mDatabaseReference.child(getString(R.string.node_offers))
                            .child(offerId)
                            .child(getString(R.string.field_status)).setValue(MainActivity.STATUS_VALUE_TRADED);
                    //Change post status to TRADED
                    mDatabaseReference.child(getString(R.string.node_posts))
                            .child(senderPostId)
                            .child(getString(R.string.field_status))
                            .setValue(MainActivity.STATUS_VALUE_TRADED);
                    mDatabaseReference.child(getString(R.string.node_posts))
                            .child(receiverPostId)
                            .child(getString(R.string.field_status))
                            .setValue(MainActivity.STATUS_VALUE_TRADED);
                }
                getOfferIds();
            }


        });

        mRecyclerView.setAdapter(mMyOfferAdapter);
    }


    ValueEventListener mValueEventListener = new ValueEventListener(){

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users offer confirmed node.");
            getOfferIds();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void getOfferIds(){
        if(mOffersIds != null){
            mOffersIds.clear();
        }
        if(mOffers != null){
            mOffers.clear();
        }
        if(mOfferList != null){
            mOfferList.clear();
        }
        Query query = mDatabaseReference.child(getString(R.string.node_offer_confirmed))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.getKey();
                        Log.d(TAG, "onDataChange: found a offer id: " + id);
                        mOffersIds.add(id);
                    }
                    getOffers();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getOffers(){
        if(mOffersIds.size() > 0){
            for(int i  = 0; i < mOffersIds.size(); i++){
                Log.d(TAG, "getOffers: getting offer information for: " + mOffersIds.get(i));

                Query query = mDatabaseReference.child(getString(R.string.node_offers))
                        .orderByKey()
                        .equalTo(mOffersIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            Offer offer = singleSnapshot.getValue(Offer.class);
                            Log.d(TAG, "onDataChange: found a offer: " + offer.getOffer_id());
                            mOffers.add(offer);
                            getTwoPostsFromOffer(offer.getReceiver_post_id(),offer.getSender_post_id(),
                                    offer.getOffer_id(),offer.getStatus());
                            mMyOfferAdapter.notifyDataSetChanged();
                        } else {
                            // Offer is deleted. Delete the record in table offer_confirmed in DB.
                            // ?????
                            deleteOfferConfirmedRecord(mOffersIds.get(mOffers.size()));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            mMyOfferAdapter.notifyDataSetChanged(); //still need to notify the adapter if the list is empty
        }

    }
    private void deleteOfferConfirmedRecord(String offerId) {
        mDatabaseReference.child(getString(R.string.node_offer_confirmed))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(offerId)
                .removeValue();
    }

    private void getTwoPostsFromOffer(String receiverPostId,String senderPostId, String offerId, String status){
        Post[] twoPostArray = new Post[2];
        Log.d(TAG, "getPosts: getting post information for: receiver post: "
                + receiverPostId +" sender post " + senderPostId);

        Query query = mDatabaseReference.child(getString(R.string.node_posts))
                .orderByKey()
                .equalTo(receiverPostId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    Post post = singleSnapshot.getValue(Post.class);
                    Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                    twoPostArray[0] = post;
                    getNextPost(twoPostArray,senderPostId, offerId, status);
                } else {
                    // Post is deleted by its author. Delete the record in table offers in DB.
                    Util.deleteOfferRecord(offerId);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getNextPost(Post[] twoPostArray,String postId, String offerID, String status){
        if(twoPostArray[0] != null){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "getPosts: getting post information fo second post " + postId);

            Query query = reference.child(getString(R.string.node_posts))
                    .orderByKey()
                    .equalTo(postId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Post post = singleSnapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                        twoPostArray[1] = post;
                        OfferPostItem mTwoPost = new OfferPostItem(offerID,status,twoPostArray[0], twoPostArray[1]);
                        mOfferList.add(mTwoPost);
                        mMyOfferAdapter.notifyDataSetChanged();

                    } else {
                        // Post is deleted by its author. Delete the record in table offers in DB.
                        Util.deleteOfferRecord(offerID);
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