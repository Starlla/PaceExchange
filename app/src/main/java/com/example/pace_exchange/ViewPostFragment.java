package com.example.pace_exchange;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;


public class ViewPostFragment extends Fragment {

    Toolbar mToolbar;
    ImageView mLike;
    RatingBar mProfileRating;
    ImageView mImage;
    ImageView mProfileImage;
    TextView mTitle;
    TextView mDescription;
    TextView mPostStartOffer;
    TextView mPostUpdate;
    TextView mPostRemove;
    TextView mPostStatusView;
    RelativeLayout mButtonContainer;
    HashMap<String,String> mSenderItemUserIdMap;
    DatabaseReference databaseReference;
    DatabaseReference currentUserDBReference;

    private String mPostId;
    private String mPostStatus;
    private String mPostUserId;
    private Post mPost;
    private boolean mIsInMyLikes;
    private StartOfferButtonClickHandler mListener;
    private String currentUserId;

    public static final String NO_ACTION = "no_action";
    private static final String TAG = "ViewPostFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference= FirebaseDatabase.getInstance().getReference();
        currentUserDBReference = databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
        mProfileImage = view.findViewById(R.id.view_post_profile_image);
        mTitle = view.findViewById(R.id.post_title);
        mDescription = view.findViewById(R.id.post_description);
        mPostStartOffer = view.findViewById(R.id.post_start_offer);
        mPostUpdate = view.findViewById(R.id.post_update);
        mPostRemove = view.findViewById(R.id.post_remove);
        mButtonContainer = view.findViewById(R.id.view_post_fragment_button_container);
        mPostStatusView =view.findViewById(R.id.view_post_post_status);
        setToolbar();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments() !=null) {
            mPostId = (String) getArguments().get(MainActivity.ARG_POST_ID);
            mPostUserId = (String) getArguments().get(MainActivity.ARG_UID);
            mPostStatus = (String) getArguments().get(MainActivity.ARG_POST_STATUS);
        }
        init();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(isAdded()){
            try {
                mListener = (ViewPostFragment.StartOfferButtonClickHandler) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");

            }
        }
    }

    private void init() {
        getUserInfo();
        getPostInfo();
        mSenderItemUserIdMap = new HashMap<>();
        String specialCode = (String) getArguments().get(getString(R.string.arg_special_code));
        if (specialCode != null && specialCode == NO_ACTION) {
            // View post from offer fragment.
            mButtonContainer.setVisibility(View.GONE);
            if (!mPostUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                getLikeInfo();
                addLikeAndOfferClickListener();
            } else {
                mLike.setVisibility(View.GONE);
            }
        } else if (mPostUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // View my post.
            mLike.setVisibility(View.GONE);
            mPostStartOffer.setVisibility(View.GONE);
            if (mPostStatus.equals(MainActivity.STATUS_VALUE_LOCKED)){
                mPostStatusView.setVisibility(View.VISIBLE);
                mButtonContainer.setVisibility(View.GONE);
            } else if (mPostStatus.equals(MainActivity.STATUS_VALUE_ACTIVE)) {
                addUpdateClickListener();
                addRemoveClickListener();
            }else if(mPostStatus.equals(MainActivity.STATUS_VALUE_TRADED)){
                mPostStatusView.setVisibility(View.VISIBLE);
                mPostRemove.setVisibility(View.VISIBLE);
                addRemoveClickListener();
                mPostUpdate.setVisibility(View.GONE);

            }
        } else {
            // View other user's post.
            mPostUpdate.setVisibility(View.INVISIBLE);
            mPostRemove.setVisibility(View.INVISIBLE);
            getLikeInfo();
            addLikeAndOfferClickListener();
            if (mPostStatus.equals(MainActivity.STATUS_VALUE_LOCKED)) {
                mPostStatusView.setVisibility(View.VISIBLE);
                mButtonContainer.setVisibility(View.GONE);
            } else if (mPostStatus.equals(MainActivity.STATUS_VALUE_ACTIVE)) {
                mPostUpdate.setVisibility(View.INVISIBLE);
                mPostRemove.setVisibility(View.INVISIBLE);
            }
        }
//        hideSoftKeyboard();
    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_users)).orderByKey().equalTo(mPostUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener(){
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
                        Glide.with(getContext()).load(user.getProfile_photo()).into(mProfileImage);
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
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    if(singleSnapshot != null) {
                        mPost = singleSnapshot.getValue(Post.class);
                        mTitle.setText(mPost.getTitle());
                        mPostStatusView.setText(mPost.getStatus());
                        mDescription.setText(mPost.getDescription());
                        Glide.with(getActivity()).load(mPost.getImage()).into(mImage);
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
                Bundle args = new Bundle();
                args.putString(getString(R.string.extra_post_id),mPostId);
                args.putString(MainActivity.ARG_UID, mPostUserId);
                OfferInventoryFragment fragment = new OfferInventoryFragment();
                fragment.setArguments(args);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_offer_inventory));
                fragmentTransaction.addToBackStack(getString(R.string.fragment_offer_inventory));
                fragmentTransaction.commit();
                mListener.startOfferButtonClicked(fragment);
            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString(MainActivity.ARG_UID, mPostUserId);
                MyItemsFragment fragment = new MyItemsFragment();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_view_post));
                fragmentTransaction.addToBackStack(getString(R.string.fragment_view_post));
                fragmentTransaction.commit();
//            mFrameLayout.setVisibility(View.VISIBLE);
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
                //delete data from both offers, offer received, and offer send table
                deleteOfferData();
                // Delete from inventories table.
                databaseReference.child(getString(R.string.node_inventories))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mPostId)
                        .removeValue();
                // Delete image in Database.
                deleteImageFromDatabase();
                // Delete from posts table.
                databaseReference.child(getString(R.string.node_posts))
                        .child(mPostId)
                        .removeValue();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void deleteImageFromDatabase() {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(mPost.getImage());
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(TAG, "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // An error occurred!
                Log.d(TAG, "onFailure: did not delete file");
            }
        });
    }

    private void deleteOfferData() {
        Log.d(TAG,"start delete offer data");
        //delete all offer data where my item as sender
        Query query = databaseReference.child(getString(R.string.node_offer_send))
                .orderByKey().equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        Log.d(TAG,snapshot.getKey());
                        if (snapshot.getKey().endsWith(mPostId)){
                            String offer_id =snapshot.getKey();
                            String receiverUserID = (String)snapshot.getValue();
//
                            Log.d(TAG,"offer id" + offer_id);
                            Log.d(TAG,"target user_id" + receiverUserID);
                            databaseReference.child(getString(R.string.node_offer_send))
                                    .child(currentUserId)
                                    .child(offer_id).removeValue();
                            databaseReference.child(getString(R.string.node_offer_received))
                                    .child(receiverUserID)
                                    .child(offer_id).removeValue();
                            databaseReference.child(getString(R.string.node_offers))
                                    .child(offer_id).removeValue();

                        }
                    }
                }
                Log.d(TAG,"offer id Not Found");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //delete all offer data where my item as receiver
        Query query_2 = databaseReference.child(getString(R.string.node_offer_received)).orderByKey()
                .equalTo(currentUserId);
        query_2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        if(snapshot.getKey().startsWith(mPostId)) {
                            String offer_id = snapshot.getKey();
                            String senderUserID = (String)snapshot.getValue();
                            databaseReference.child(getString(R.string.node_offer_received))
                                    .child(currentUserId)
                                    .child(offer_id).removeValue();
                            databaseReference.child(getString(R.string.node_offer_send))
                                    .child(senderUserID)
                                    .child(offer_id).removeValue();
                            databaseReference.child(getString(R.string.node_offers))
                                    .child(offer_id).removeValue();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
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
        getActivity().setTitle("");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public interface StartOfferButtonClickHandler{
        void startOfferButtonClicked(OfferInventoryFragment fragment);
    }


//    private void initAndExecuteAsyncTask() {
//        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,Void> mAsyncTask = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
////                getActivity().getSupportFragmentManager().popBackStack();
//            }
//        };
//        mAsyncTask.execute();
//    }



}
