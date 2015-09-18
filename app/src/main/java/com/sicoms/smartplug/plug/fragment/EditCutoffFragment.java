package com.sicoms.smartplug.plug.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.plug.adapter.ConditionAdapter;
import com.sicoms.smartplug.plug.event.CutoffEvent;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;
import com.sicoms.smartplug.plug.service.CutoffService;

import antistatic.spinnerwheel.AbstractWheel;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class EditCutoffFragment extends Fragment {

    private static final String TAG = EditCutoffFragment.class.getSimpleName();
    private static final String ARG_PLUG_VO_NAME = "plug_vo";
    private static final String ARG_CUTOFF_VO_NAME = "cutoff_vo";
    private CharSequence mTitle = "전원 차단 설정";

    private Activity mActivity;
    private CutoffEvent mEvent;
    private CutoffService mService;
    private PlugVo mPlugVo;
    private static CutoffResultCallbacks mCallbacks;
    private CutoffVo mCutoffVo;


    private TextView mTvCompleteBtn;
    private AbstractWheel mWvPower;
    private AbstractWheel mWvMin;
    
    private ConditionAdapter mPowerdapter;
    private ConditionAdapter mMinAdapter;

    public static EditCutoffFragment newInstance(PlugVo plugVo, CutoffVo cutoffVo, CutoffResultCallbacks callbacks) {
        EditCutoffFragment fragment = new EditCutoffFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLUG_VO_NAME, new Gson().toJson(plugVo));
        args.putString(ARG_CUTOFF_VO_NAME, new Gson().toJson(cutoffVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_add_cutoff, container, false);
        mActivity = getActivity();
        ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);

        Bundle args = getArguments();
        if (args != null) {
            mPlugVo = new Gson().fromJson(getArguments().getString(ARG_PLUG_VO_NAME), PlugVo.class);
            mCutoffVo = new Gson().fromJson(getArguments().getString(ARG_CUTOFF_VO_NAME), CutoffVo.class);
        }
        if (savedInstanceState != null) {
            mPlugVo = new Gson().fromJson(savedInstanceState.getString(ARG_PLUG_VO_NAME), PlugVo.class);
            mCutoffVo = new Gson().fromJson(savedInstanceState.getString(ARG_CUTOFF_VO_NAME), CutoffVo.class);
        }

        //mEvent = new CutoffEvent(this);
        mService = new CutoffService(mActivity);
        mService.setOnCutoffResultCallbacks(mCallbacks);

        mTvCompleteBtn = (TextView) view.findViewById(R.id.tv_complete_btn);
        mWvPower = (AbstractWheel) view.findViewById(R.id.wv_power);
        mWvMin = (AbstractWheel) view.findViewById(R.id.wv_min);

        mTvCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int powerNum = mWvPower.getCurrentItem();
                String power = mPowerdapter.get(powerNum);
                int minNum = mWvMin.getCurrentItem();
                String min = mMinAdapter.get(minNum);

                CutoffVo cutoffVo = new CutoffVo(power, min, true);
                mService.setCutoffDeviceInDevice(mPlugVo, cutoffVo);
                ((ActionBarActivity)mActivity).getSupportFragmentManager().popBackStack();
            }
        });

        mPowerdapter = new ConditionAdapter(mActivity);
        mMinAdapter = new ConditionAdapter(mActivity);

        setData();

        mWvPower.setViewAdapter(mPowerdapter);
        mWvMin.setViewAdapter(mMinAdapter);

        fillAdapterData();

        return view;
    }

    private void fillAdapterData(){

        String power, min;
        power = mCutoffVo.getPower();
        min = mCutoffVo.getMin();

        try {
            mWvPower.setCurrentItem(Integer.parseInt(power)-1);
            mWvMin.setCurrentItem(Integer.parseInt(min));
        } catch (NumberFormatException ne){
            ne.printStackTrace();
        }
    }

    private void setData(){
        // Start
        for(int powerCnt=0; powerCnt<5; powerCnt++) {
            String power = "";
            power += String.valueOf(powerCnt+1);
            mPowerdapter.add(power);
        }

        for(int minCnt=0; minCnt<60; minCnt++) {
            String min = "";
            if( minCnt < 10){
                min = "0";
            }
            min += String.valueOf(minCnt);
            mMinAdapter.add(min);
        }
    }
    
    public void setOnCutoffResultCallbacks( final CutoffResultCallbacks callbacks){
        mCallbacks = callbacks;
    }

}
