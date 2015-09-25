package com.sicoms.smartplug.plug.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.plug.adapter.ConditionAdapter;
import com.sicoms.smartplug.plug.event.CutoffEvent;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;
import com.sicoms.smartplug.plug.service.CutoffService;
import com.sicoms.smartplug.util.SPUtil;

import java.util.List;

import antistatic.spinnerwheel.AbstractWheel;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class CutoffFragment extends Fragment implements CutoffResultCallbacks {

    private static final String TAG = CutoffFragment.class.getSimpleName();
    private CharSequence mTitle = "전원 차단 설정";

    private Context mContext;
    private CutoffEvent mEvent;
    private CutoffService mService;
    private PlugVo mPlugVo;
    private CutoffVo mCutoffVo;

    private AbstractWheel mWvPower;
    private AbstractWheel mWvMin;
    private ImageView mIvCutoffSwitch;
    private RelativeLayout mRlCutoffBg;

    private ConditionAdapter mPowerdapter;
    private ConditionAdapter mMinAdapter;

    public static CutoffFragment newInstance(PlugVo vo) {
        CutoffFragment fragment = new CutoffFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(vo));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_cutoff, container, false);
        mContext = getActivity();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);

        Bundle args = getArguments();
        if (args != null)
            mPlugVo = new Gson().fromJson(getArguments().getString(TAG), PlugVo.class);
        if (savedInstanceState != null)
            mPlugVo = new Gson().fromJson(savedInstanceState.getString(TAG), PlugVo.class);

        mCutoffVo = new CutoffVo("5", "00", false);
        mEvent = new CutoffEvent(mContext, this, mPlugVo);
        mService = new CutoffService(mContext);
        mService.setOnCutoffResultCallbacks(this);

        mWvPower = (AbstractWheel) view.findViewById(R.id.wv_power);
        mWvMin = (AbstractWheel) view.findViewById(R.id.wv_time);
        mRlCutoffBg = (RelativeLayout) view.findViewById(R.id.rl_cutoff_bg);
        mRlCutoffBg.setVisibility(View.VISIBLE);

        mIvCutoffSwitch = (ImageView) view.findViewById(R.id.iv_curoff_switch);
        mIvCutoffSwitch.setOnClickListener(mEvent);

        mPowerdapter = new ConditionAdapter(mContext);
        mMinAdapter = new ConditionAdapter(mContext);

        setData();

        mWvPower.setViewAdapter(mPowerdapter);
        mWvMin.setViewAdapter(mMinAdapter);
        mWvPower.setCurrentItem(2);

        mService.requestGetCutoffInDevice(mPlugVo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapterData();
    }

    private void fillAdapterData(){
        List<CutoffVo> cutoffVoList = mService.selectDbCutoffList(mPlugVo);
        if( cutoffVoList != null && cutoffVoList.size() > 0){
            mCutoffVo = cutoffVoList.get(0);

            boolean isOn = mCutoffVo.isOn();
            mIvCutoffSwitch.setSelected(isOn);
            if( isOn){
                mRlCutoffBg.setVisibility(View.INVISIBLE);
            } else {
                mRlCutoffBg.setVisibility(View.VISIBLE);
            }

            try {
                String power = mCutoffVo.getPower();
                String min = mCutoffVo.getTime();

                mWvPower.setCurrentItem(Integer.parseInt(power) - 1);
                mWvMin.setCurrentItem(Integer.parseInt(min));
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        }
        mEvent.setCutoffVo(mCutoffVo);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_complete);
        item.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                getFragmentManager().popBackStack();
                break;
            case R.id.action_complete :
                if( mPlugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
                    if(!MainActivity.stBluetoothManager.isConnected()){
                        SPUtil.showToast(mContext, "블루투스에 연결되지 않았습니다.");
                        fillAdapterData();
                        return false;
                    }
                }
                int powerNum = mWvPower.getCurrentItem();
                String power = mPowerdapter.get(powerNum);
                int minNum = mWvMin.getCurrentItem();
                String min = mMinAdapter.get(minNum);
                boolean isOn = mIvCutoffSwitch.isSelected();

                CutoffVo cutoffVo = new CutoffVo(power, min, isOn);
                mService.setCutoffDeviceInDevice(mPlugVo, cutoffVo);

                SPUtil.showDialog(mContext);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCutoffResult(final CutoffVo cutoffVo) {
        if( cutoffVo != null) {
            if( mCutoffVo.getPower().equalsIgnoreCase(SPConfig.NO_CUTOFF)){
                cutoffVo.setPower("3");
                cutoffVo.setIsOn(false);
            }
            if( mService.updateDbCutoff(mPlugVo, cutoffVo)) {
                SPUtil.showToast(mContext, "전원 차단 설정을 동기화 하였습니다.");
            } else {
                SPUtil.showToast(mContext, "전원 차단 설정을 저장하지 못했습니다.");
            }
        } else {
            Toast.makeText(mContext, "전원 차단 설정에 실패하였습니다", Toast.LENGTH_SHORT).show();
        }
        fillAdapterData();
        SPUtil.dismissDialog();
    }
}
