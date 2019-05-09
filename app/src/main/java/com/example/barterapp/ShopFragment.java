package com.example.barterapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.example.barterapp.util.RecyclerViewMargin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    SearchView mSearchView;
    RecyclerView mRecyclerView;
    FrameLayout mFrameLayout;

    DatabaseReference mDatabaseReference;
    MyAdapter mMyAdapter;
    List<Post> mItems;

    ShopFragmentButtonClickHandler mClickHandler;

    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = Util.dpToPx(14);

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
        mSearchView = view.findViewById(R.id.shop_search_view);
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
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mMyAdapter = new MyAdapter(getActivity(), mItems);
        mRecyclerView.setAdapter(mMyAdapter);
        search("");
    }

    private void configureSearchInput() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
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
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String myId = currentUser.getUid();
                        for (DataSnapshot dss : dataSnapshot.getChildren()) {
                            final Post post = dss.getValue(Post.class);
                            if (!post.getUser_id().equals(myId)) {
                                mItems.add(post);
                            }
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
        fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
        fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
        fragmentTransaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }
}
