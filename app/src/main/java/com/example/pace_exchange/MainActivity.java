package com.example.pace_exchange;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements ProfileFragment.ProfileFragmentButtonClickHandler,
        ShopFragment.ShopFragmentButtonClickHandler,ViewPostFragment.StartOfferButtonClickHandler{
    private static final int REQUEST_CODE = 1;

    private ImageView currentTabView;
    private TextView profileTabText;
    private TextView shopTabText;
    private TextView postTabText;
    private ImageView profileIconView;
    private ImageView shopIconView;
    private ImageView postIconView;


    ProfileFragment profileFragment;
    ShopFragment shopFragment;
    PostFragment postFragment;
    Fragment currentFragment;
    String uid;
    String currentTag;
    TextView currentTabText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileTabText = findViewById(R.id.profile_tab_text);
        shopTabText = findViewById(R.id.shop_tab_text);
        postTabText = findViewById(R.id.post_tab_text);
        profileIconView =findViewById(R.id.profile_tab_icon);
        shopIconView =findViewById(R.id.shop_tab_icon);
        postIconView =findViewById(R.id.post_tab_icon);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        final FirebaseUser user = (FirebaseUser) bd.get(getString(R.string.extra_user));

        if (user != null) {
            String email = user.getEmail();
            uid = user.getUid();

        }
        verifyPermissions();
    }

    private void configureFragment(){
        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_user_id), uid);

        profileFragment = new ProfileFragment();
        profileFragment.setArguments(args);
        shopFragment = new ShopFragment();
        shopFragment.setArguments(args);
        postFragment = new PostFragment();
        currentFragment = shopFragment;
        currentTabView = shopIconView;
        currentTag = getString(R.string.fragment_shop);
        currentTabText = shopTabText;

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment,currentTag).commit();

    }

    private void addTabClickListeners() {

        View.OnClickListener tabListener = view -> {
            if(currentTabView != null){
                if(currentTabView.getId() == R.id.profile_tab_icon)
                    profileIconView.setImageResource(R.drawable.ic_person_outline_black_24dp);
                else
                    currentTabView.setColorFilter(getResources().getColor(R.color.colorUnSelect));
            }

            currentTabText.setTextColor(ContextCompat.getColor(this, R.color.colorUnSelect));

            currentTabView = currentTabView == null ? shopIconView : (ImageView)view;
            int currentTabId = currentTabView == null ? 0 : currentTabView.getId();

            switch (currentTabId) {
                case R.id.profile_tab_icon:
                    currentFragment = profileFragment;
                    currentTag = getString(R.string.fragment_profile);
                    currentTabText = profileTabText;
                    profileIconView.setImageResource(R.drawable.ic_person_black_24dp);
                    break;
                case R.id.shop_tab_icon:
                    currentFragment = shopFragment;
                    currentTabText = shopTabText;
                    currentTag = getString(R.string.fragment_shop);
                    shopIconView.setColorFilter(getResources().getColor(R.color.colorSelect));
                    break;
                case R.id.post_tab_icon:
                    currentFragment = postFragment;
                    currentTag = getString(R.string.fragment_post);
                    currentTabText = postTabText;
                    postIconView.setColorFilter(getResources().getColor(R.color.colorSelect));
                    break;
            }
            if(currentTabText != null) {
                currentTabText.setTextColor(ContextCompat.getColor(this, R.color.colorSelect));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment,currentTag).commit();
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

    @Override
    public void myItemsTabClicked(MyItemsFragment fragment) {
        currentFragment = fragment;
    }

    @Override
    public void myLikesTabClicked(MyLikesFragment fragment) {
        currentFragment = fragment;
    }

    @Override
    public void myOfferTabClicked(OfferFragment fragment) {

    }


    @Override
    public void myProfileTabClicked(MyProfileFragment fragment) {
        currentFragment = fragment;
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

    @Override
    public void startOfferButtonClicked(OfferInventoryFragment fragment) {
        currentFragment = fragment;

    }
}
