package com.example.pace_exchange;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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


public class OfferSendFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    DatabaseReference mDatabaseReference;
    String mUid;
    MyOfferAdapter mMyOfferAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<Offer> mOfferList;
    private ArrayList<Post> mSendOfferItems;
    private ArrayList<String> mReceivedOfferItemIds;
    private ArrayList<String> mSendOfferItemIds;
    private DatabaseReference reference;
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
                String mReceiverPostID =mOfferList.get(position).getReceiverPost().getPost_id();
                String mSenderPostId = mOfferList.get(position).getSenderPost().getPost_id();
                String mReceiverUserId = mOfferList.get(position).getReceiverPost().getUser_id();
                System.out.println(mSenderPostId);
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(getString(R.string.node_offer_received))
                        .child(mReceiverUserId)
                        .child(mReceiverPostID)
                        .child(mSenderPostId)
                        .removeValue();
                databaseReference.child(getString(R.string.node_offer_send))
                        .child(uid)
                        .child(mSenderPostId)
                        .child(mReceiverPostID)
                        .removeValue();
                mMyOfferAdapter.notifyDataSetChanged();
            }
        });
        mRecyclerView.setAdapter(mMyOfferAdapter);
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users offer received node.");
            readData(new OfferSendFragment.MyCallback() {
                @Override
                public void onCallback(ArrayList<String> postIDList) {
                    Log.d(TAG,"1st CALLBACK"+postIDList.size());
                    mSendOfferItemIds = postIDList;
                    readPostData(new OfferSendFragment.MyPostCallback() {
                        @Override
                        public void onPostCallback(ArrayList<Post> postList) {
                            Log.d(TAG,"2rd CALLBACK"+postList.size());
                            for(int i =0; i< postList.size();i++) {
                                getSingleItemOfferSendList(postList.get(i));
                            }
                        }
                    });
                }
            });
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private void readData(OfferSendFragment.MyCallback myCallback) {

        if(mOfferList != null){
            mOfferList.clear();
        }
        if(mSendOfferItems != null){
            mSendOfferItems.clear();
        }
        if(mSendOfferItemIds != null){
            mSendOfferItemIds.clear();
        }

        Query query = reference.child(getString(R.string.node_offer_send))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String sendOfferItemID = snapshot.getKey();
                        System.out.println(sendOfferItemID);
                        mSendOfferItemIds.add(sendOfferItemID);
                    }
                    myCallback.onCallback(mSendOfferItemIds);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void readPostData(OfferSendFragment.MyPostCallback myPostCallback){
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
                            mSendOfferItems.add(post);
                            if(mSendOfferItemIds.size() == mSendOfferItems.size()){
                                myPostCallback.onPostCallback(mSendOfferItems);
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

    private void getSingleItemOfferSendList(Post current){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_offer_send))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByKey()
                .equalTo(current.getPost_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()) {
                        String id = snapshot.getKey().toString();
                        mReceivedOfferItemIds.add(id);
                    }
                    getReceivedOfferItemList(current);
                    mReceivedOfferItemIds.clear();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getReceivedOfferItemList(Post current) {
        if (mReceivedOfferItemIds.size() > 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for (int i = 0; i < mReceivedOfferItemIds.size(); i++) {
                Log.d(TAG, "getPosts: getting post information for: " + mReceivedOfferItemIds.get(i));
                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mReceivedOfferItemIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            Post post = singleSnapshot.getValue(Post.class);
                            Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                            Offer offer = new Offer(post,current );
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

}
