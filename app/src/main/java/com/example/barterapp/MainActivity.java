package com.example.barterapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements ProfileFragment.ProfileFragmentButtonClickHandler, ShopFragment.ShopFragmentButtonClickHandler{
    private static final int REQUEST_CODE = 1;

    private TextView userTest;
    private ImageView currentTabView;
    private TextView profileTabText;
    ImageView profileIconView;
    ImageView shopIconView;
    ImageView postIconView;

    ProfileFragment profileFragment;
    ShopFragment shopFragment;
    PostFragment postFragment;
    Fragment currentFragment;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        userTest = findViewById(R.id.test_user);

        profileTabText = findViewById(R.id.profile_tab_text);
        profileIconView =findViewById(R.id.profile_tab_icon);
        shopIconView =findViewById(R.id.shop_tab_icon);
        postIconView =findViewById(R.id.post_tab_icon);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        final FirebaseUser user = (FirebaseUser) bd.get("user");

        if (user != null) {
            String email = user.getEmail();
            uid = user.getUid();

//            userTest.setText(email + "  " + uid);
        }

        verifyPermissions();

    }

    private  void configureFragment(){
        profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("uid",uid);
        profileFragment.setArguments(args);

        shopFragment = new ShopFragment();

        postFragment = new PostFragment();
        currentFragment = shopFragment;
        currentTabView = shopIconView;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();

    }

    private void addTabClickListeners() {

        View.OnClickListener tabListener = view -> {
            if(currentTabView != null){
                if(currentTabView.getId() == R.id.profile_tab_icon)
                    profileIconView.setImageResource(R.drawable.ic_person_outline_black_24dp);
                else
                    currentTabView.setColorFilter(getResources().getColor(R.color.colorUnSelect));
            }

            currentTabView = currentTabView == null ? shopIconView : (ImageView)view;
            int currentTabId = currentTabView == null ? 0 : currentTabView.getId();

            switch (currentTabId) {
                case R.id.profile_tab_icon:
                    currentFragment = profileFragment;
                    profileIconView.setImageResource(R.drawable.ic_person_black_24dp);
                    break;
                case R.id.shop_tab_icon:
                    currentFragment = shopFragment;
                    shopIconView.setColorFilter(getResources().getColor(R.color.colorSelect));
                    break;
                case R.id.post_tab_icon:
                    currentFragment = postFragment;
                    postIconView.setColorFilter(getResources().getColor(R.color.colorSelect));
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
        };
        //add above listener to tabs
        findViewById(R.id.profile_tab_icon).setOnClickListener(tabListener);
        findViewById(R.id.shop_tab_icon).setOnClickListener(tabListener);
        findViewById(R.id.post_tab_icon).setOnClickListener(tabListener);
    }


    @Override
    public void signOutButtonClicked() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
    }


    private void verifyPermissions(){
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            configureFragment();
            addTabClickListeners();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissions,
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }
}
