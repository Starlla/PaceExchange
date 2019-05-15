package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

//Display all the offer current user has send; Current user could cancel the offer here
public class OfferSendFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    DatabaseReference mDatabaseReference;
    String mUid;
    MyOfferAdapter mMyOfferAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<OfferPostItem> mOfferList;
    private ArrayList<Post> mSendOfferItems;
    private ArrayList<String> mReceivedOfferItemIds;
    private ArrayList<String> mSendOfferItemIds;
    private DatabaseReference reference;
    private ArrayList<String> mOffersIds;
    private ArrayList<Offer> mOffers;

    protected static final String TAG = "OfferSendFragment";

    public OfferSendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString(ProfileFragment.ARG_UID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_send, container, false);
        mRecyclerView = view.findViewById(R.id.offer_send_recycler_view);
        mOfferList = new ArrayList<>();
        mSendOfferItems =new ArrayList<>();
        mReceivedOfferItemIds = new ArrayList<>();
        mSendOfferItemIds = new ArrayList<>();
        mOfferList = new ArrayList<>();
        mOffersIds = new ArrayList<>();
        mOffers = new ArrayList<>();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (OfferSendFragment.OnFragmentInteractionListener) context;
        } catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mDatabaseReference.removeEventListener(mValueEventListener);
    }

    public interface OnFragmentInteractionListener {
    }

    private void init () {
        setUpRecyclerView();
        reference = FirebaseDatabase.getInstance().getReference();
        //reference for listening when items are added or removed from the offer list
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_offer_send))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReference.addValueEventListener(mValueEventListener);
    }

    private void setUpRecyclerView () {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mMyOfferAdapter = new MyOfferAdapter(getActivity(), mOfferList);
        mMyOfferAdapter.setFragmentTag(TAG);
        mMyOfferAdapter.setSendOfferInteraction(new MyOfferAdapter.OnSendOfferInteractionListener() {
            @Override
            public void onCancelButtonClick(int position) {
                Log.d(TAG,"Cancel Button Clicked");
                String mReceiverUserId = mOfferList.get(position).getReceiverPost().getUser_id();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                String offerId = mOfferList.get(position).getOfferID();
                databaseReference.child(getString(R.string.node_offer_send))
                        .child(uid)
                        .child(offerId)
                        .removeValue();
                databaseReference.child(getString(R.string.node_offer_received))
                        .child(mReceiverUserId)
                        .child(offerId)
                        .removeValue();
                databaseReference.child(getString(R.string.node_offers))
                        .child(offerId).removeValue();
                mMyOfferAdapter.notifyDataSetChanged();
            }

            // change to use Interface!!!!
            @Override
            public void onImageClick(String postId, String userId) {
                Bundle args = new Bundle();
                args.putString(getString(R.string.arg_post_id), postId);
                args.putString(getString(R.string.arg_user_id), userId);
                args.putString(getString(R.string.arg_special_code), ViewPostFragment.NO_ACTION);
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

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users offer received node.");
            getOfferIds();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_offer_send))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.getKey().toString();
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
                            deleteOfferSendRecord(mOffersIds.get(mOffers.size()));
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

    private void deleteOfferSendRecord(String offerId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(getString(R.string.node_offer_send))
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

    private void getNextPost(Post[] twoPostArray,String postId, String offerID) {
        if (twoPostArray[0] != null) {
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
                        OfferPostItem mTwoPost = new OfferPostItem(offerID,twoPostArray[0], twoPostArray[1]);
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
