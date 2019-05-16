package com.example.pace_exchange;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pace_exchange.util.RecyclerViewMargin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfferInventoryFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    // Widgets
    private RecyclerView mRecyclerView;
    private TextView mConfirmOffer;
    private TextView mCancelOffer;

    // Vars
    private MyAdapter mMyAdapter;
    private List<Post> mItems;
    private List<String> mPostIds;
    private DatabaseReference mReference;
    private String mPostIdWant;
    private String mWantPostUserId;
    private Toolbar toolbar;

    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = Util.dpToPx(14);

    public OfferInventoryFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPostIdWant = getArguments().getString(getString(R.string.extra_post_id));
            mWantPostUserId = getArguments().getString(ViewPostFragment.WANT_POST_USER_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_inventory, container, false);
        mRecyclerView = view.findViewById(R.id.offer_inventory);
        mConfirmOffer = view.findViewById(R.id.confirm_offer_button);
        mCancelOffer = view.findViewById(R.id.cancel_offer_button);
        mReference = FirebaseDatabase.getInstance().getReference();
        toolbar = view.findViewById(R.id.offer_inventory_toolbar);
        setToolbar();
        Log.d("TAG","Target User Id:"+ mWantPostUserId);
        init();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (OfferInventoryFragment.OnFragmentInteractionListener) context;
        } catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {

    }
    private void init() {
        mItems = new ArrayList<>();
        mPostIds = new ArrayList<>();
        configureRecyclerView();

        // Reference for listening when data change in my inventory
        mReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_inventories))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Set listener to the reference
        mReference.addValueEventListener(mDataListener);
        addClickListeners();
    }

    private void configureRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getContext(), mItems);
        mRecyclerView.setAdapter(mMyAdapter);
    }

    // Get all of my posts
    private void getInventory() {
        if (mPostIds != null) {
            mPostIds.clear();
        }
        if (mItems != null) {
            mItems.clear();
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_inventories))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for (DataSnapshot snapshot : singleSnapshot.getChildren()) {
                        String postId = snapshot.child(getString(R.string.field_post_id)).getValue().toString();
                        mPostIds.add(postId);
                    }
                }
                getPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPosts() {
        if (mPostIds.size() > 0) {
            for (int i = 0; i < mPostIds.size(); i++) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mPostIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            if (singleSnapshot != null) {
                                Post post = singleSnapshot.getValue(Post.class);
                                mItems.add(post);
                                mMyAdapter.notifyDataSetChanged();
                            }
                        } else {
                            mMyAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } else {
            mMyAdapter.notifyDataSetChanged();
        }
    }

    private void addClickListeners() {
        mCancelOffer.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
        mConfirmOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPostId = mMyAdapter.getSelectedPostId();
                if (selectedPostId == "") {
                    Toast.makeText(getActivity(),
                            R.string.toast_please_select_an_item, Toast.LENGTH_SHORT).show();
                }
                else {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    //generate offer id by rule: receiverPostID*senderPostID;
                    String offer_id = getString(R.string.two_string_with_star,mPostIdWant,selectedPostId);
                    Map newOfferValue = new HashMap();
                    newOfferValue.put("offer_id",offer_id);
                    newOfferValue.put("receiver_post_id", mPostIdWant);
                    newOfferValue.put("sender_post_id",selectedPostId );
                    newOfferValue.put("receiver_uid", mWantPostUserId);
                    newOfferValue.put("sender_uid",uid );
                    newOfferValue.put(getString(R.string.field_status),Post.STATUS_VALUE_ACTIVE );

                    reference.child("offers")
                            .child(offer_id)
                            .setValue(newOfferValue);
                    reference.child("offer_send")
                            .child(uid)
                            .child(offer_id).setValue(mWantPostUserId);
                    reference.child("offer_received")
                            .child(mWantPostUserId)
                            .child(offer_id).setValue(uid);

                    Toast.makeText(getActivity(),
                            R.string.toast_offer_created, Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

    }

    ValueEventListener mDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            getInventory();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void setToolbar(){

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

//    public boolean offerAlreadyExist(){
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//
//    }

}
