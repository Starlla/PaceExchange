package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class OfferReceivedFragment extends Fragment {

    public interface OnOfferReceivedFragmentInteractionListener {

    }

    private OfferReceivedFragment.OnOfferReceivedFragmentInteractionListener mListener;
    DatabaseReference mDatabaseReferenceReceiverSide;
//    DatabaseReference mDatabaseReferenceSenderSide;
    String mUid;
    MyOfferAdapter mMyOfferAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<OfferPostItem> mOfferList;
    private ArrayList<String> mOffersIds;
    private ArrayList<Offer> mOffers;
    private DatabaseReference mDatabaseReference;
    protected static final String TAG = "OfferReceivedFragment";
    public OfferReceivedFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (OfferReceivedFragment.OnOfferReceivedFragmentInteractionListener) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
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
        mUid = FirebaseAuth.getInstance().getUid();
        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null){}
        init();
    }

    @Override
    public void onDetach () {
        super.onDetach();
        mListener = null;
        mDatabaseReferenceReceiverSide.removeEventListener(mValueEventListener);
    }

    private void init () {
        setUpRecyclerView();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        //reference for listening when items are added or removed from the offer list
        mDatabaseReferenceReceiverSide = FirebaseDatabase.getInstance().getReference()
                .child("offer_received")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReferenceReceiverSide.addValueEventListener(mValueEventListener);
    }

    private void setUpRecyclerView () {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mMyOfferAdapter = new MyOfferAdapter(getActivity(), mOfferList);
        mMyOfferAdapter.setFragmentTag(TAG);
        mMyOfferAdapter.setReceivedOfferInteraction(new MyOfferAdapter.OnReceivedOfferInteractionListener() {
            @Override
            public void onAcceptButtonClick(int position) {
                OfferPostItem offerPostItem = mOfferList.get(position);
                String senderId = offerPostItem.getSenderPost().getUser_id();
                String receiverId = offerPostItem.getReceiverPost().getUser_id();
                String senderPostId = offerPostItem.getSenderPost().getPost_id();
                String receiverPostId = offerPostItem.getReceiverPost().getPost_id();
                String offerId = offerPostItem.getOfferID();

                // Change status of both posts.
                mDatabaseReference.child(getString(R.string.node_posts))
                        .child(senderPostId)
                        .child(getString(R.string.field_status))
                        .setValue(MainActivity.STATUS_VALUE_LOCKED);
                mDatabaseReference.child(getString(R.string.node_posts))
                        .child(receiverPostId)
                        .child(getString(R.string.field_status))
                        .setValue(MainActivity.STATUS_VALUE_LOCKED);

                mDatabaseReference.child("offer_received")
                        .child(receiverId)
                        .child(offerId)
                        .removeValue();
                //From sender side
                mDatabaseReference.child("offer_send")
                        .child(senderId)
                        .child(offerId)
                        .removeValue();

                //change offer status to INACTIVE related to this two Posts
                mDatabaseReference.child("offer_received").orderByKey().equalTo(receiverId).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getChildren().iterator().hasNext()){
                                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                                        Log.d(TAG,snapshot.getKey());
                                        if (snapshot.getKey().startsWith(receiverPostId)){
                                            String offer_id =snapshot.getKey();
                                            System.out.println(offer_id);
                                            mDatabaseReference.child(getString(R.string.node_offers))
                                                    .child(offer_id)
                                                    .child(getString(R.string.field_status)).setValue(MainActivity.STATUS_VALUE_INACTIVE);
                                            mMyOfferAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                                Log.d(TAG,"offer id Not Found");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                mDatabaseReference.child("offer_send").orderByKey().equalTo(receiverId).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getChildren().iterator().hasNext()){
                                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                                        Log.d(TAG,snapshot.getKey());
                                        if (snapshot.getKey().endsWith(receiverPostId)){
                                            String offer_id =snapshot.getKey();
                                            System.out.println(offer_id);
                                            mDatabaseReference.child("offers")
                                                    .child(offer_id)
                                                    .child("status").setValue(MainActivity.STATUS_VALUE_INACTIVE);
                                            mMyOfferAdapter.notifyDataSetChanged();

                                        }
                                    }
                                }
                                Log.d(TAG,"offer id Not Found");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                mDatabaseReference.child("offer_send").orderByKey().equalTo(senderId).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getChildren().iterator().hasNext()){
                                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                                        Log.d(TAG,snapshot.getKey());
                                        if (snapshot.getKey().endsWith(senderPostId)){
                                            String offer_id =snapshot.getKey();
                                            System.out.println(offer_id);
                                            mDatabaseReference.child("offers")
                                                    .child(offer_id)
                                                    .child("status").setValue(MainActivity.STATUS_VALUE_INACTIVE);
                                            mMyOfferAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                                Log.d(TAG,"offer id Not Found");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                mDatabaseReference.child("offer_received").orderByKey().equalTo(senderId).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getChildren().iterator().hasNext()){
                                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                                        Log.d(TAG,snapshot.getKey());
                                        if (snapshot.getKey().startsWith(senderPostId)){
                                            String offer_id =snapshot.getKey();
                                            System.out.println(offer_id);
                                            mDatabaseReference.child("offers")
                                                    .child(offer_id)
                                                    .child("status").setValue(MainActivity.STATUS_VALUE_INACTIVE);
                                            mMyOfferAdapter.notifyDataSetChanged();

                                        }
                                    }
                                }
                                Log.d(TAG,"offer id Not Found");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });


                // Add to offer_confirmed table.
                mDatabaseReference.child("offer_confirmed")
                        .child(senderId)
                        .child(offerId)
                        .setValue(receiverId);
                mDatabaseReference.child("offer_confirmed")
                        .child(receiverId)
                        .child(offerId)
                        .setValue(senderId);
                //Change status in offers.
                mDatabaseReference.child("offers")
                        .child(offerId)
                        .child("status")
                        .setValue(MainActivity.STATUS_VALUE_LOCKED);
                getOfferIds();
            }

            @Override
            public void onRejectButtonClick(int position) {
                String mSenderUserId = mOfferList.get(position).getSenderPost().getUser_id();
                String mReceiverUserId = mOfferList.get(position).getReceiverPost().getUser_id();
                String offerId = mOfferList.get(position).getOfferID();
                RemoveSingleOfferRecord(mReceiverUserId,mSenderUserId, offerId);
                mRecyclerView.setAdapter(mMyOfferAdapter);
            }
        });

        mMyOfferAdapter.setGeneralSituationInteration(new MyOfferAdapter.OnGeneralSituationInterationListener() {
            @Override
            public void onRemoveButtonClick(int position) {
                String mSenderUserId = mOfferList.get(position).getSenderPost().getUser_id();
                String mReceiverUserId = mOfferList.get(position).getReceiverPost().getUser_id();
                String offerId = mOfferList.get(position).getOfferID();
                RemoveSingleOfferRecord(mReceiverUserId,mSenderUserId,offerId);

            }

            @Override
            public void onImageClick(String postId, String userId,String postStatus) {
                Bundle args = new Bundle();
                args.putString(MainActivity.ARG_POST_ID, postId);
                args.putString(MainActivity.ARG_UID,userId);
                args.putString(MainActivity.ARG_SPECIAL_CODE, MainActivity.NO_ACTION);
                ViewPostFragment fragment = new ViewPostFragment();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
                fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
                fragmentTransaction.commit();

            }
        });



        mRecyclerView.setAdapter(mMyOfferAdapter);
    }

    private void RemoveSingleOfferRecord(String mReceiverUserId, String mSenderUserId, String offerId) {
        mDatabaseReference.child("offer_received")
                .child(mReceiverUserId)
                .child(offerId)
                .removeValue();
        mDatabaseReference.child("offer_send")
                .child(mSenderUserId)
                .child(offerId)
                .removeValue();
        Util.deleteOfferRecord(offerId);
        mMyOfferAdapter.notifyDataSetChanged();
    }

    //Delete post start with post id in offer id
    private void InActiveAsReceiverOffer(String relatedOfferReceiverPostId) {
        mDatabaseReference.child("offers").orderByKey().startAt(relatedOfferReceiverPostId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildren().iterator().hasNext()){
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            mDatabaseReference.child("offers").child(singleSnapshot.getKey()).child("status").setValue(MainActivity.STATUS_VALUE_INACTIVE);
                            Log.d(TAG, "InActivate Offer: " + singleSnapshot.getKey());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //Delete post end with post id in offer id
    private void InActiveAsSenderOffer(String relatedSenderOfferPostId) {
        mDatabaseReference.child("offers").orderByKey().endAt(relatedSenderOfferPostId).
            addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildren().iterator().hasNext()){
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        mDatabaseReference.child(getString(R.string.node_offers)).child(singleSnapshot.getKey()).child("status").setValue(MainActivity.STATUS_VALUE_INACTIVE);
                        Log.d(TAG, "InActivate Offer: " + singleSnapshot.getKey());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
        });
    }


    ValueEventListener mValueEventListener = new ValueEventListener(){
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users offer_received node.");
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
        Query query = mDatabaseReference.child("offer_received")
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
                mMyOfferAdapter.notifyDataSetChanged();

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

                Query query = mDatabaseReference.child("offers")
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
                                    offer.getOffer_id(), offer.getStatus());
//                            mMyOfferAdapter.notifyDataSetChanged();
                        } else {
                            // Offer is deleted . Delete the record in table offer_received in DB.
                            deleteOfferReceivedRecord(mOffersIds.get(mOffers.size()));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            // still need to notify the adapter if the list is empty
                mMyOfferAdapter.notifyDataSetChanged();

        }

    }
    private void deleteOfferReceivedRecord(String offerId) {
        mDatabaseReference.child(getString(R.string.node_offer_received))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(offerId)
                .removeValue();
    }

    private void  getTwoPostsFromOffer(String receiverPostId,String senderPostId, String offerId, String status){
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
//                            if(post.getStatus().equals(Post.STATUS_VALUE_ACTIVE)){
                                twoPostArray[0] = post;
                                getNextPost(twoPostArray,senderPostId, offerId, status);
//                            }
                            mMyOfferAdapter.notifyDataSetChanged();
                        } else {
                            // Post is deleted by its author. Delete the record in table offers in DB.
                            Util.deleteOfferRecord(offerId);

                            mMyOfferAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getNextPost(Post[] twoPostArray,String postId, String offerID, String status){
        if(twoPostArray[0] != null){
            Log.d(TAG, "getPosts: getting post information fo second post " + postId);

            Query query = mDatabaseReference.child(getString(R.string.node_posts))
                    .orderByKey()
                    .equalTo(postId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Post post = singleSnapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
//                        if(post.getStatus().equals(Post.STATUS_VALUE_ACTIVE)){
                        twoPostArray[1] = post;
                        OfferPostItem mTwoPost = new OfferPostItem(offerID,status,twoPostArray[0], twoPostArray[1]);
                        mOfferList.add(mTwoPost);
//                        }
                        mMyOfferAdapter.notifyDataSetChanged();
                        twoPostArray[0] = null;
                        twoPostArray[1] = null;
                    } else {
                        // Post is deleted by its author. Delete the record in table offers in DB.
                        Util.deleteOfferRecord(offerID);
                        mMyOfferAdapter.notifyDataSetChanged();
                        twoPostArray[0] = null;
                        twoPostArray[1] = null;
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }



}




