package com.example.barterapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements ProfileFragment.ProfileFragmentButtonClickHandler{

    private TextView userTest;
    private View currentTabView;
    private TextView profileTab;
    ImageView profileIconView;
    ProfileFragment profileFragment;
    Fragment currentFragment;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userTest = findViewById(R.id.test_user);

        profileTab = findViewById(R.id.profile_tab);
        profileIconView =findViewById(R.id.profile_tab_icon);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        final FirebaseUser user = (FirebaseUser) bd.get("user");

        if (user != null) {
            String email = user.getEmail();
            uid = user.getUid();

            userTest.setText(email + "  " + uid);
        }
        configureFragment();
        addTabClickListeners();
    }



    private  void configureFragment(){
        profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("uid",uid);
        profileFragment.setArguments(args);
    }


    private void addTabClickListeners() {

        View.OnClickListener tabListener = view -> {

//            if(currentTabView != null)
//                currentTabView.setBackgroundColor(Color.WHITE);
            currentTabView = currentTabView == null ? profileIconView : view;
//            currentTabView .setBackgroundColor(getResources().getColor(R.color.colorSelect));


            int currentTabId = currentTabView == null ? 0 : currentTabView.getId();
            switch (currentTabId) {
                case R.id.profile_tab_icon:
                    currentFragment = profileFragment;
                    profileIconView.setImageResource(R.drawable.ic_person_black_24dp);
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
        };
        //add above listener to tabs
        findViewById(R.id.profile_tab_icon).setOnClickListener(tabListener);
    }


    @Override
    public void signOutButtonClicked() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
    }
}
