package com.example.barterapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RatingBar;
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


public class ViewPostFragment extends Fragment {

    Toolbar mToolbar;
    ImageView mLike;
    RatingBar mProfileRating;
    ImageView mImage;
    TextView mTitle;
    TextView mDescription;
    TextView mPostStartOffer;
    TextView mPostUpdate;
    TextView mPostRemove;

    private String mPostId;
    private String mPostUserId;
    private Post mPost;
    private boolean mIsInMyLikes;

    public static final String WANT_POST_USER_UID = "want_post_user_uid";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostId = (String) getArguments().get(getString(R.string.arg_post_id));
        mPostUserId = (String) getArguments().get(getString(R.string.arg_user_id));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mToolbar = view.findViewById(R.id.view_post_toolbar);
        mLike = view.findViewById(R.id.add_watch_list);
        mProfileRating = view.findViewById(R.id.profile_rating_bar);
        mImage = view.findViewById(R.id.post_image);
        mTitle = view.findViewById(R.id.post_title);
        mDescription = view.findViewById(R.id.post_description);
        mPostStartOffer = view.findViewById(R.id.post_start_offer);
        mPostUpdate = view.findViewById(R.id.post_update);
        mPostRemove = view.findViewById(R.id.post_remove);
        setToolbar();
        init();
        return view;
    }

    private void init() {
        getUserInfo();
        getPostInfo();
        if (mPostUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            mLike.setVisibility(View.INVISIBLE);
            mPostStartOffer.setVisibility(View.GONE);
            addUpdateClickListener();
            addRemoveClickListener();
        } else {
            mPostUpdate.setVisibility(View.INVISIBLE);
            mPostRemove.setVisibility(View.INVISIBLE);
            getLikeInfo();
            addLikeAndOfferClickListener();
        }
//        hideSoftKeyboard();
    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_users)).orderByKey().equalTo(mPostUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    if(singleSnapshot != null){
                        User user = singleSnapshot.getValue(User.class);
                        ((TextView) getView().findViewById(R.id.profile_name)).setText(getString(
                                R.string.two_string_with_space, user.getFirst_name(), user.getLast_name()));
                        ((RatingBar) getView().findViewById(R.id.profile_rating_bar))
                                .setRating(user.getRating() == 0.0f ? 5.0f : user.getRating());
                        Glide.with(getContext()).load(user.getProfile_photo()).into((ImageView)getView().findViewById(R.id.view_post_profile_image));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void addLikeAndOfferClickListener() {
        mLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsInMyLikes) {
                    removeItemFromMyLikes();
                    mLike.setImageResource(R.drawable.ic_favorite_border_gray_24dp);
                } else {
                    addItemToMyLikes();
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
                intent.putExtra(WANT_POST_USER_UID,mPostUserId);
                startActivity(intent);
            }
        });
    }

    private void addUpdateClickListener() {
        mPostUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change to use Interface to pass args!!!

                Bundle args = new Bundle();
                args.putString(getString(R.string.arg_user_id), mPostId);
                PostFragment fragment = new PostFragment();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
            }
        });
    }

    private void addRemoveClickListener() {
        mPostRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete from posts table.
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(getString(R.string.node_posts))
                        .child(mPostId)
                        .removeValue();

                // Delete from offers table.
                databaseReference.child(getString(R.string.node_offers))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mPostId)
                        .removeValue();

                // Delete from inventories table.
                databaseReference.child(getString(R.string.node_inventories))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mPostId)
                        .removeValue();
                Toast.makeText(getActivity(), R.string.toast_post_removed, Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
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

    private void setToolbar(){
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        getActivity().setTitle("ViewPost");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
