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


public class OfferFragment extends Fragment {

    Toolbar toolbar;

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
        mTabLayout = (TabLayout) view.findViewById(R.id.offer_tabs);
        mViewPager  = (ViewPager) view.findViewById(R.id.viewpager_container);
        toolbar = view.findViewById(R.id.offer_toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbar();
        setupViewPager();
    }

    private void setupViewPager(){
        mPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(new OfferReceivedFragment());
        mPagerAdapter.addFragment(new OfferSendFragment());
        mPagerAdapter.addFragment(new OfferConfirmedFragment());
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
