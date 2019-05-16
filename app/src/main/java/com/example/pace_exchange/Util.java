package com.example.pace_exchange;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Util {
    final static String TAG = "Util";


    static int dpToPx(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }

    static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality,stream);
        return stream.toByteArray();
    }

    static void deleteOfferRecord(String offerId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("offers")
                .child(offerId)
                .removeValue();
    }



    static void getPostIdsThenGetPosts(ArrayList<String> mPostIDList, ArrayList<Post> mPostList,MyAdapter mAdapter, String node){
        Log.d(TAG, "Getting " +node +"list.");
        if(mPostList != null){
            mPostList.clear();
        }
        if(mPostIDList != null){
            mPostIDList.clear();
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(node)
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot: singleSnapshot.getChildren()){
                        String id = snapshot.child("post_id").getValue().toString();
                        Log.d(TAG, "onDataChange: found a post id: " + id);
                        mPostIDList.add(id);
                    }
                    getPostsfrommPostIDList(mPostIDList,mPostList,mAdapter,node);
                }else{
                    getPostsfrommPostIDList(mPostIDList,mPostList,mAdapter,node);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Use it to get Post from PostID list delete post id from node table if post do not exist; 
    static void getPostsfrommPostIDList(ArrayList<String> mPostIDList, ArrayList<Post> postList, MyAdapter mAdapter, String node){
        if(mPostIDList.size() > 0){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for(int i  = 0; i < mPostIDList.size(); i++){
                Log.d(TAG, "getPosts: getting post information for: " + mPostIDList.get(i));

                Query query = reference.child("posts")
                        .orderByKey()
                        .equalTo(mPostIDList.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            Post post = singleSnapshot.getValue(Post.class);
                            Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                            postList.add(post);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // Post is deleted by its author. Delete the record in table likes in DB.
                            deleteRecordFromNode(node, mPostIDList.get(postList.size()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }else{
            mAdapter.notifyDataSetChanged(); //still need to notify the adapter if the list is empty
        }
    }

    private static void deleteRecordFromNode(String node, String postId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(node)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(postId)
                .removeValue();
    }


}
