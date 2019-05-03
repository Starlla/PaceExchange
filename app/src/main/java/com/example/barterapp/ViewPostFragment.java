package com.example.barterapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class ViewPostFragment extends Fragment {

    ImageView mImage;
    ImageView mPostClose;
    ImageView mLike;
    TextView mProfileName;
    TextView mTitle;
    TextView mDescription;
    TextView mPostStartOffer;

    private String mPostId;
    private String mUserId;
    private Post mPost;
    private boolean mIsInMyLikes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostId = (String) getArguments().get(getString(R.string.arg_post_id));
        mUserId = (String) getArguments().get(getString(R.string.arg_user_id));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mImage = view.findViewById(R.id.post_image);
        mPostClose = view.findViewById(R.id.post_close);
        mLike = view.findViewById(R.id.add_watch_list);
        mProfileName = view.findViewById(R.id.profile_name);
        mPostStartOffer = view.findViewById(R.id.post_start_offer);
        mTitle = view.findViewById(R.id.post_title);
        mDescription = view.findViewById(R.id.post_description);

        init();
        return view;
    }

    private void init() {
        getPostInfo();
        getUserInfo();
        getLikeInfo();
        addClickListeners();
        hideSoftKeyboard();
    }

    private void getPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_posts)).orderByKey().equalTo(mPostId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                if(singleSnapshot != null){
                    mPost = singleSnapshot.getValue(Post.class);

                    mTitle.setText(mPost.getTitle());
                    mDescription.setText(mPost.getDescription());
                    Glide.with(getActivity()).load(mPost.getImage()).into(mImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_users)).orderByKey().equalTo(mUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    if(singleSnapshot != null){
                        User user = singleSnapshot.getValue(User.class);
                        mProfileName.setText(getString(R.string.two_string_with_space,user.getFirst_name(),user.getLast_name()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikeInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_post_id)).equalTo(mPostId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mIsInMyLikes = true;
//                    ImageViewCompat.setImageTintList(mLike, ColorStateList.valueOf(ContextCompat
//                            .getColor(getActivity(), R.color.colorAccent)));
                    mLike.setImageResource(R.drawable.ic_favorite_red_24dp);
                } else {
                    mIsInMyLikes = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addClickListeners() {
        mPostClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsInMyLikes) {
                    removeItemFromMyLikes();
//                    ImageViewCompat.setImageTintList(mLike, ColorStateList.valueOf(ContextCompat
//                            .getColor(getActivity(), R.color.White)));
                    mLike.setImageResource(R.drawable.ic_favorite_border_gray_24dp);
                } else {
                    addItemToMyLikes();
//                    ImageViewCompat.setImageTintList(mLike, ColorStateList.valueOf(ContextCompat
//                            .getColor(getActivity(), R.color.colorAccent)));
                    mLike.setImageResource(R.drawable.ic_favorite_red_24dp);
                }
                mIsInMyLikes = !mIsInMyLikes;
            }
        });

        mPostStartOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OfferInventoryActivity.class);
                intent.putExtra(getString(R.string.extra_post_id), mPostId);
                startActivity(intent);
            }
        });
    }

    private void addItemToMyLikes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(getString(R.string.node_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .child(getString(R.string.field_post_id))
                .setValue(mPostId);

        Toast.makeText(getActivity(), R.string.toast_add_to_likes, Toast.LENGTH_SHORT).show();
    }

    private void removeItemFromMyLikes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(getString(R.string.node_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .removeValue();

        Toast.makeText(getActivity(), R.string.toast_remove_from_likes, Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyboard(){
        final Activity activity = getActivity();
        final InputMethodManager inputMethodManager = (InputMethodManager)activity.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
