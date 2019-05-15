package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
    private DatabaseReference reference;
    protected static final String TAG = "OfferReceivedFragment";

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
        mOfferList = new ArrayList<>();
        mOffersIds = new ArrayList<>();
        mOffers = new ArrayList<>();
        return view;
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
        mDatabaseReferenceReceiverSide.removeEventListener(mValueEventListener);
    }

    private void init () {
        setUpRecyclerView();
        reference = FirebaseDatabase.getInstance().getReference();
        //reference for listening when items are added or removed from the offer list
        mDatabaseReferenceReceiverSide = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_offer_received))
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

            }

            @Override
            public void onRejectButtonClick(int position) {
//                String mReceiverPostID =mOfferList.get(position).getReceiverPost().getPost_id();
//                String mSenderPostId = mOfferList.get(position).getSenderPost().getPost_id();
//                String mSenderUserId = mOfferList.get(position).getSenderPost().getUser_id();
//                System.out.println(mSenderPostId);
//                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//                databaseReference.child(getString(R.string.node_offer_received))
//                        .child(uid)
//                        .child(mReceiverPostID)
//                        .child(mSenderPostId)
//                        .removeValue();
//                databaseReference.child(getString(R.string.node_offer_send))
//                        .child(mSenderUserId)
//                        .child(mSenderPostId)
//                        .child(mReceiverPostID)
//                        .removeValue();
                mMyOfferAdapter.notifyDataSetChanged();

            }
        });

        mRecyclerView.setAdapter(mMyOfferAdapter);
    }


    ValueEventListener mValueEventListener = new ValueEventListener(){

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users offer received node.");
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_offer_received))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.getValue().toString();
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
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            for(int i  = 0; i < mOffersIds.size(); i++){
                Log.d(TAG, "getOffers: getting offer information for: " + mOffersIds.get(i));

                Query query = reference.child(getString(R.string.node_offers))
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
                            getTwoPostsFromOffer(offer.getReceiver_post_id(),offer.getSender_post_id(),offer.getOffer_id());
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
//            mMyOfferAdapter.notifyDataSetChanged(); //still need to notify the adapter if the list is empty
        }

    }
    private void deleteOfferReceivedRecord(String offerId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(getString(R.string.node_offer_received))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(offerId)
                .removeValue();
    }

    private void  getTwoPostsFromOffer(String receiverPostId,String senderPostId, String offerId){
            Post[] twoPostArray = new Post[2];
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Log.d(TAG, "getPosts: getting post information for: receiver post: "
                        + receiverPostId +" sender post " + senderPostId);

                Query query = reference.child(getString(R.string.node_posts))
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
                            getNextPost(twoPostArray,senderPostId, offerId);
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

    private void getNextPost(Post[] twoPostArray,String postId, String offerID){
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
                        OfferPostItem mTwoPost = new OfferPostItem(twoPostArray[0], twoPostArray[1]);
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




