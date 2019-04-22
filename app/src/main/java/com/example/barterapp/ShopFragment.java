package com.example.barterapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

    DatabaseReference mDatabaseReference;
    FirebaseRecyclerOptions<TradeItem> mOptions;
    FirebaseRecyclerAdapter<TradeItem, RecyclerView.ViewHolder> mAdapter;

    List<TradeItem> mItems;

    ShopFragmentButtonClickHandler mClickHandler;

    String mUid;

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
        // "TradeItem" need to comply with database structure
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("TradeItems");
        mOptions = new FirebaseRecyclerOptions.Builder<TradeItem>().setQuery(mDatabaseReference, TradeItem.class).build();
        mAdapter = new FirebaseRecyclerAdapter<TradeItem, RecyclerView.ViewHolder>(mOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull TradeItem model) {
                MyAdapter.MyAdapterViewHolder viewHolder = (MyAdapter.MyAdapterViewHolder) holder;
                // set text with data in model
                viewHolder.mTextView.setText("Item");
                // set image here
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = View.inflate(viewGroup.getContext(), R.layout.item_view_abstract, null);
                return new MyAdapter.MyAdapterViewHolder(view);
            }
        };
    }

    private void configureRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
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
        Query query = mDatabaseReference.orderByChild("name").startAt(input).endAt(input + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mItems.clear();
                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                        final TradeItem tradeItem = dss.getValue(TradeItem.class);
                        mItems.add(tradeItem);
                    }
                    MyAdapter myAdapter = new MyAdapter(getActivity(), mItems);
                    mRecyclerView.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
