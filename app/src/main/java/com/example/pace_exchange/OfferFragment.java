package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pace_exchange.util.MyPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;


public class OfferFragment extends Fragment {

    Toolbar toolbar;

    //widgets
    private TabLayout mTabLayout;
    public ViewPager mViewPager;

    //vars
    public MyPagerAdapter mPagerAdapter;
    String mUid;
    public static final String ARG_UID = "uid";

    private OnFragmentInteractionListener mListener;

    public OfferFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mUid = MainActivity.ARG_UID;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer, container, false);
        mTabLayout = (TabLayout) view.findViewById(R.id.offer_tabs);
        mViewPager  = (ViewPager) view.findViewById(R.id.viewpager_container);
        toolbar = view.findViewById(R.id.offer_toolbar);
        mUid = FirebaseAuth.getInstance().getUid();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbar();
        setupViewPager();
    }

    private void setupViewPager(){
        Bundle args = new Bundle();

        mPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        OfferReceivedFragment mOfferReceivedFragment = new OfferReceivedFragment();
        OfferSendFragment mOfferSendFragment = new OfferSendFragment();
        OfferConfirmedFragment mOfferConfirmedFragment = new OfferConfirmedFragment();
//        mOfferReceivedFragment.setArguments(args);
//        mOfferSendFragment.setArguments(args);
//        mOfferSendFragment.setArguments(args);

        mPagerAdapter.addFragment(mOfferReceivedFragment);
        mPagerAdapter.addFragment(mOfferSendFragment);
        mPagerAdapter.addFragment(mOfferConfirmedFragment);
        //Add Fragment to ViewPager
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setText(getString(R.string.offer_received));
        mTabLayout.getTabAt(1).setText(getString(R.string.offer_send));
        mTabLayout.getTabAt(2).setText(getString(R.string.offer_confirmed));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(isAdded()) {
            try {
                mListener = (OfferFragment.OnFragmentInteractionListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
    }

    private void setToolbar(){
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
