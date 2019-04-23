package com.example.barterapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PostFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener{

    private static final String TAG = "PostFragment";
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private ImageView mPostImage;

    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG, "getImagePath: setting the image to imageview");
        UniversalImageLoader.setImage(imagePath.toString(), mPostImage);

        mSelectedBitmap = null;
        mSelectedUri = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to imageview");
        mPostImage.setImageBitmap(bitmap);

        mSelectedUri = null;
        mSelectedBitmap = bitmap;

    }

    interface PostFragmentButtonClickHandler{
        void signOutButtonClicked();
    }

    PostFragmentButtonClickHandler mClickHandler;
    String mUid;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mClickHandler = (PostFragmentButtonClickHandler) context;
        }catch(ClassCastException e){
            new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        mPostImage = view.findViewById(R.id.post_image);

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(PostFragment.this, 1);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        populateView();
    }

    private void populateView(){

    }

}
