package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ShopFragment extends Fragment {

    interface ShopFragmentButtonClickHandler{

    }

    EditText mSearchInput;
    RecyclerView mRecyclerView;
    FrameLayout mFrameLayout;

    DatabaseReference mDatabaseReference;
    MyAdapter mMyAdapter;
    List<Post> mItems;

    ShopFragmentButtonClickHandler mClickHandler;

    private static final int NUM_GRID_COLUMNS = 2;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Confirm that the activity this fragment attached to implements certain interface.
        try {
            mClickHandler = (ShopFragmentButtonClickHandler) context;
        } catch (ClassCastException e){
            throw new ClassCastException("The activity that this fragment is attached to must be a ShopFragmentButtonClickHandler.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        mSearchInput = view.findViewById(R.id.search_input);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mFrameLayout = view.findViewById(R.id.container);
        mItems = new ArrayList<>();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        configureFirebaseConnection();
        configureRecyclerView();
        configureSearchInput();
    }

    private void configureFirebaseConnection() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.node_posts));
    }

    private void configureRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getActivity(), mItems);
        mRecyclerView.setAdapter(mMyAdapter);
        search("");
    }

    private void configureSearchInput() {
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    search(s.toString());
                } else {
                    search("");
                }
            }
        });
    }

    private void search(String input) {
        Query query = mDatabaseReference.orderByChild(getString(R.string.field_title)).startAt(input).endAt(input + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mItems.clear();
                    String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                        final Post post = dss.getValue(Post.class);
                        if (!post.getUser_id().equals(myId)) {
                            mItems.add(post);
                        }
                    }
                    mMyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void viewPost(String postId, String userId) {
        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        args.putString(getString(R.string.arg_user_id), userId);
        ViewPostFragment fragment = new ViewPostFragment();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, getString(R.string.fragment_view_post));
        fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
        fragmentTransaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }
}
