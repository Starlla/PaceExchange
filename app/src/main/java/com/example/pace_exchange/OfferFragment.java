package com.example.pace_exchange;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pace_exchange.util.MyPagerAdapter;


public class OfferFragment extends Fragment {

    Toolbar toolbar;
    Button receivedView;
    Button sendView;
    Fragment currentFragment;
    OfferReceivedFragment receivedFragment;
    OfferSendFragment sendFragment;
    Button currentTabView;
    String currentString;

    //widgets
    private TabLayout mTabLayout;
    public ViewPager mViewPager;

    //vars
    public MyPagerAdapter mPagerAdapter;

    private OnFragmentInteractionListener mListener;

    public OfferFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer, container, false);
//        receivedView = view.findViewById(R.id.offer_received_button);
//        sendView = view.findViewById(R.id.offer_send_button);
        mTabLayout = (TabLayout) view.findViewById(R.id.offer_tabs);
        mViewPager  = (ViewPager) view.findViewById(R.id.viewpager_container);
        toolbar = view.findViewById(R.id.offer_toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbar();
//        setupFragment();
//        addTabClickListeners();
        setupViewPager();

    }

    private void setupViewPager(){
        mPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(new OfferReceivedFragment());
        mPagerAdapter.addFragment(new OfferSendFragment());

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setText("Received");
        mTabLayout.getTabAt(1).setText("Send");

    }


    private void setupFragment() {
//        receivedFragment = new OfferReceivedFragment();
//        sendFragment = new OfferSendFragment();
//        currentFragment = receivedFragment;
//        currentString =getString(R.string.fragment_offer_received);
//        currentTabView = receivedView;
//        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.offer_container, currentFragment, currentString);
//        fragmentTransaction.commit();

    }

//    private void addTabClickListeners() {
//
//        View.OnClickListener tabListener = view -> {
//            if(currentTabView != null){
//
//                    currentTabView.setBackground(getResources().getDrawable(R.drawable.bt_light_grey));
//            }
//
//            currentTabView = currentTabView == null ? receivedView : (Button) view;
//            int currentTabId = currentTabView == null ? 0 : currentTabView.getId();
//
//            switch (currentTabId) {
//                case R.id.offer_received_button:
//                    currentFragment = receivedFragment;
//                    currentString =getString(R.string.fragment_offer_received);
//                    break;
//                case R.id.offer_send_button:
//                    currentFragment = sendFragment;
//                    currentString =getString(R.string.fragment_offer_send);
//                    break;
//            }
//            if(currentTabView != null) {
//                currentTabView.setBackground(getResources().getDrawable(R.drawable.bt_grey));
//            }
//            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.offer_container, currentFragment, currentString);
//            fragmentTransaction.commit();
//
//        };
//        //add above listener to tabs
//        receivedView.setOnClickListener(tabListener);
//        sendView.setOnClickListener(tabListener);
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       try{
            mListener = (OfferFragment.OnFragmentInteractionListener) context;
        } catch(ClassCastException e){
           new ClassCastException("the activity that  this fragment is attached to must be a FirstFragmentButtonClickHandler");
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
