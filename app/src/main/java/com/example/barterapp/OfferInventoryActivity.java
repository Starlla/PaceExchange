package com.example.barterapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barterapp.util.RecyclerViewMargin;
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

public class OfferInventoryActivity extends AppCompatActivity {

    // Widgets
    private ImageView mBackArrow;
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

    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = Util.dpToPx(14);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_inventory);
        mBackArrow = findViewById(R.id.offer_inventory_backArrow);
        mRecyclerView = findViewById(R.id.offer_inventory);
        mConfirmOffer = findViewById(R.id.confirm_offer_button);
        mCancelOffer = findViewById(R.id.cancel_offer_button);
        mReference = FirebaseDatabase.getInstance().getReference();
        mPostIdWant = getIntent().getExtras().getString(getString(R.string.extra_post_id));
        mWantPostUserId = getIntent().getStringExtra(ViewPostFragment.WANT_POST_USER_UID);
        Log.d("TAG","Target User Id:"+ mWantPostUserId);
        init();
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
        mReference.addValueEventListener(mListener);

        addClickListeners();
    }

    private void configureRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(this, mItems);
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
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mConfirmOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPostId = mMyAdapter.getSelectedPostId();
                if (selectedPostId == "") {
                    Toast.makeText(OfferInventoryActivity.this,
                            R.string.toast_please_select_an_item, Toast.LENGTH_SHORT).show();
                } else {
                    Map newValue = new HashMap();
                    newValue.put(getString(R.string.field_post_id), selectedPostId);
                    newValue.put("sender_id", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(getString(R.string.node_offers))
                            .child(mWantPostUserId)
                            .child(mPostIdWant)
                            .child(selectedPostId)
                            .setValue(newValue);


                    finish();
                    // Or redirect to other pages
                    Toast.makeText(OfferInventoryActivity.this,
                            R.string.toast_offer_created, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancelOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReference.removeEventListener(mListener);
    }

    ValueEventListener mListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            getInventory();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
