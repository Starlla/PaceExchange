package com.example.pace_exchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;


public class MyProfileFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener,
        SelectGenderDialog.OnGenderSelectedListener,SelectYearDialog.OnYearSelectedListener{

    private static final String ARG_UID = "uid";
    private String mUid;


    @Override
    public void setGenderFemale() {
        Map newValue = new HashMap();
        newValue.put("gender", "Female");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.node_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(newValue);
        mMyProfileGenderView.setText(getString(R.string.female));
    }

    @Override
    public void setGenderMale() {
        Map newValue = new HashMap();
        newValue.put("gender", "Male");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.node_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(newValue);
        mMyProfileGenderView.setText(getString(R.string.male));
    }

    @Override
    public void setGenderUnspecified() {
        Map newValue = new HashMap();
        newValue.put("gender", "Unspecified");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.node_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(newValue);
        mMyProfileGenderView.setText(getString(R.string.unspecified));
    }

    @Override
    public void onDateSet(Context mContext, int year) {
        Map newValue = new HashMap();
        newValue.put("graduation_year", year);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.node_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(newValue);
        mMyProfileGraduationYearView.setText(String.valueOf(year));

    }

    interface MyProfileFragmentButtonClickHandler {

    }

    private MyProfileFragmentButtonClickHandler mClickHandler;
    TextView mMyProfileNameView;
    TextView mMyProfileEmailView;
    TextView mMyProfileGenderView;
    TextView mMyProfileGraduationYearView;
    Toolbar toolbar;
    ImageView mMyProfilePhoto;
    private byte[] mUploadBytes;
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private double mProgress = 0;
    public MyProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {mUid = args.getString(MainActivity.ARG_UID);}
        System.out.println(mUid+"~~~~");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        toolbar = view.findViewById(R.id.my_profile_toolbar);
        mMyProfileNameView = view.findViewById(R.id.my_profile_name);
        mMyProfileEmailView = view.findViewById(R.id.my_profile_email);
        mMyProfileGenderView = view.findViewById(R.id.my_profile_gender);
        mMyProfileGraduationYearView = view.findViewById(R.id.my_profile_graduation_year);
        mMyProfilePhoto = view.findViewById(R.id.my_profile_photo);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
        init();
        setToolbar();
        return view;
    }


    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG, "getImagePath: setting the image to imageview");
        UniversalImageLoader.setImage(imagePath.toString(), mMyProfilePhoto);
        mSelectedBitmap = null;
        mSelectedUri = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to imageview");
        mMyProfilePhoto.setImageBitmap(bitmap);
        mSelectedUri = null;
        mSelectedBitmap = bitmap;
    }

    @Override
    public void triggerImageUpload() {
        //we have a uri and no bitmap
        if(mSelectedBitmap != null && mSelectedUri == null){
            uploadNewPhoto(mSelectedBitmap);
        }
        //we have no bitmap and a uri
        else if(mSelectedBitmap == null && mSelectedUri != null){
            uploadNewPhoto(mSelectedUri);
        }
        mSelectedBitmap = null;
        mSelectedUri = null;
    }


    private void init() {
        mMyProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(MyProfileFragment.this, 2);
            }
        });

        mMyProfileGenderView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SelectGenderDialog dialog = new SelectGenderDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_gender));
                dialog.setTargetFragment(MyProfileFragment.this, 3);
            }
        });

        mMyProfileGraduationYearView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SelectYearDialog dialog = new SelectYearDialog();
                dialog.setTargetFragment(MyProfileFragment.this, 4);
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_graduation_year));
            }
        });


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(isAdded()) {
            try {
                mClickHandler = (MyProfileFragment.MyProfileFragmentButtonClickHandler) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
            }
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickHandler = null;
    }

    private void populateView() {
        Bundle args = getArguments();
        if (args != null)
            mUid = args.getString(MainActivity.ARG_UID);
        getUserInfo();
    }

    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        MyProfileFragment.BackgroundImageResize resize = new MyProfileFragment.BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        MyProfileFragment.BackgroundImageResize resize = new MyProfileFragment.BackgroundImageResize(null);
        resize.execute(imagePath);
    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.node_users)).orderByKey().equalTo(mUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    if (dataSnapshot != null) {
                        User user = singleSnapshot.getValue(User.class);
                        mMyProfileNameView.setText(getString(R.string.two_string_with_space, user.getFirst_name(), user.getLast_name()));
                        mMyProfileEmailView.setText(user.getEmail());
                        mMyProfileGenderView.setText(user.getGender());
                        mMyProfileGraduationYearView.setText(user.getGraduation_year() == 0 ? "" : String.valueOf(user.getGraduation_year()));
                        Glide.with(getContext()).load(user.getProfile_photo()).into((ImageView)getView().findViewById(R.id.my_profile_photo));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setToolbar() {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle("My Profile");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bitmap) {
            if(bitmap != null){
                this.mBitmap = bitmap;
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "compressing image", Toast.LENGTH_SHORT).show();
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){
                try{
                    RotateBitmap rotateBitmap = new RotateBitmap();
                    mBitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getActivity(), params[0]);
                }catch (IOException e){
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }
            byte[] bytes = null;
            Log.d(TAG, "doInBackground: megabytes before compression: " + mBitmap.getByteCount() / 1000000 );
            bytes = Util.getBytesFromBitmap(mBitmap, 100);
            Log.d(TAG, "doInBackground: megabytes before compression: " + bytes.length / 1000000 );
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            hideProgressBar();
            executeUploadTask();
        }
    }
    private void showProgressBar(){
//        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideProgressBar(){
//        if(mProgressBar.getVisibility() == View.VISIBLE){
//            mProgressBar.setVisibility(View.INVISIBLE);
//        }
    }

    private void executeUploadTask(){
        Toast.makeText(getActivity(), "uploading image", Toast.LENGTH_SHORT).show();

        final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_photo/profile_photo");

        UploadTask uploadTask = storageReference.putBytes(mUploadBytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(getActivity(), "Post Success", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri firebaseUri = urlTask.getResult();

                Log.d(TAG, "onSuccess: firebase download url: " + firebaseUri.toString());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                Map newValue = new HashMap();
                newValue.put("profile_photo", firebaseUri.toString());

                reference.child(getString(R.string.node_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(newValue);

//                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.getProfile_photo());

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "could not upload photo", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if( currentProgress > (mProgress + 15)){
                    mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                    Toast.makeText(getActivity(), mProgress + "%", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }








}
