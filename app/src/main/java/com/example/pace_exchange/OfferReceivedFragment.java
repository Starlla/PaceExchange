package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
    DatabaseReference mDatabaseReference;
    String mUid;
    MyOfferAdapter mMyOfferAdapter;
    RecyclerView mRecyclerView;
    private ArrayList<Offer> mOfferList;
    private ArrayList<Post> mReceivedOfferItems;
    private ArrayList<String> mReceivedOfferItemIds;
    private ArrayList<String> mSendOfferItemIds;
    private Toolbar toolbar;
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
        mReceivedOfferItems =new ArrayList<>();
        mReceivedOfferItemIds = new ArrayList<>();
        mSendOfferItemIds = new ArrayList<>();
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
    }

    private void init () {
        setUpRecyclerView();
        reference = FirebaseDatabase.getInstance().getReference();
        //reference for listening when items are added or removed from the offer list
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_offer_received))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReference.addValueEventListener(mValueEventListener);

    }

    private void setUpRecyclerView () {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mMyOfferAdapter = new MyOfferAdapter(getActivity(), mOfferList);
        mMyOfferAdapter.setFragmentTag(TAG);
        mMyOfferAdapter.setOnItemClickListener(new MyOfferAdapter.OnItemClickListener() {

            @Override
            public void onAcceptButtonClick(int position) {

            }

            @Override
            public void onRejectButtonClick(int position) {
                String mReceiverPostID =mOfferList.get(position).getReceiverPost().getPost_id();
                String mSenderPostId = mOfferList.get(position).getSenderPost().getPost_id();
                String mSenderUserId = mOfferList.get(position).getSenderPost().getUser_id();
                System.out.println(mSenderPostId);
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(getString(R.string.node_offer_received))
                        .child(uid)
                        .child(mReceiverPostID)
                        .child(mSenderPostId)
                        .removeValue();
                databaseReference.child(getString(R.string.node_offer_send))
                        .child(mSenderUserId)
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
            readData(new MyCallback() {
                @Override
                public void onCallback(ArrayList<String> postIDList) {
                    Log.d(TAG,"1st CALLBACK"+postIDList.size());
                    mReceivedOfferItemIds = postIDList;
                    readPostData(new MyPostCallback() {
                        @Override
                        public void onPostCallback(ArrayList<Post> postList) {
                            System.out.println("2stCALLBACK"+postList.size());
                            for(int i =0; i< postList.size();i++) {
                                getSingleItemOfferReceivedList(postList.get(i));
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

    private void readData(MyCallback myCallback) {

        if(mOfferList != null){
            mOfferList.clear();
        }
        if(mReceivedOfferItems != null){
            mReceivedOfferItems.clear();
        }
        if(mReceivedOfferItemIds != null){
            mReceivedOfferItemIds.clear();
        }

        Query query = reference.child(getString(R.string.node_offer_received))
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

    private void getSingleItemOfferReceivedList(Post current){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.node_offer_received))
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
                            mSendOfferItemIds.add(id);
                        }
                        getSendOfferItemList(current);
                        mSendOfferItemIds.clear();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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



}




