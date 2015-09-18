package com.sicoms.smartplug.plug.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.ScheduleVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.plug.adapter.TimeAdapter;
import com.sicoms.smartplug.plug.event.ScheduleEvent;
import com.sicoms.smartplug.plug.interfaces.ScheduleResultCallbacks;
import com.sicoms.smartplug.plug.service.ScheduleService;
import com.sicoms.smartplug.util.SPUtil;

import java.util.List;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelScrollListener;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class ScheduleFragment extends Fragment implements ScheduleResultCallbacks {

    private static final String TAG = ScheduleFragment.class.getSimpleName();
    private CharSequence mTitle = "스케쥴 시간대 설정";

    private Activity mActivity;
    private ScheduleEvent mEvent;
    private ScheduleService mService;
    private PlugVo mPlugVo;
    private ScheduleVo mScheduleVo;

    private ImageView mIvStartSwitch;
    private ImageView mIvEndSwitch;
    private RelativeLayout mRlStartBg;
    private RelativeLayout mRlEndBg;
    private TextView mTvStartTime;
    private TextView mTvEndTime;
    private RelativeLayout mRlStart;
    private RelativeLayout mRlEnd;
    private LinearLayout mLlStartWv;
    private LinearLayout mLlEndWv;
    private AbstractWheel mWvStartAmPm;
    private AbstractWheel mWvStartHour;
    private AbstractWheel mWvStartMin;
    private AbstractWheel mWvEndAmPm;
    private AbstractWheel mWvEndHour;
    private AbstractWheel mWvEndMin;

    private TimeAdapter mStartAmPmAdapter;
    private TimeAdapter mStartHourAdapter;
    private TimeAdapter mStartMinAdapter;
    private TimeAdapter mEndAmPmAdapter;
    private TimeAdapter mEndHourAdapter;
    private TimeAdapter mEndMinAdapter;

    private String mStartAmPm = SPConfig.AM;
    private String mStartHour = "01";
    private String mStartMin = "00";
    private String mEndAmPm = SPConfig.AM;
    private String mEndHour = "01";
    private String mEndMin = "00";

    /*private Handler mScheduleHandler = new Handler(Looper.getMainLooper());
    private Runnable mScheduleTimeout = new Runnable() {
        @Override
        public void run() {
            SPUtil.showToast(mActivity, "스케쥴을 설정하지 못했습니다.");
            SPUtil.dismissDialog();
        }
    };*/

    public static ScheduleFragment newInstance(PlugVo vo) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(vo));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        mActivity = getActivity();
        ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);

        Bundle args = getArguments();
        if (args != null)
            mPlugVo = new Gson().fromJson(getArguments().getString(TAG), PlugVo.class);
        if (savedInstanceState != null)
            mPlugVo = new Gson().fromJson(savedInstanceState.getString(TAG), PlugVo.class);

        mService = new ScheduleService(mActivity);
        mService.setOnScheduleResultCallbacks(this);
        mScheduleVo = new ScheduleVo(SPConfig.AM, SPConfig.AM, "01:00", "01:00", false, false);
        mEvent = new ScheduleEvent(this, this);

        mIvStartSwitch = (ImageView) view.findViewById(R.id.iv_start_switch);
        mIvEndSwitch = (ImageView) view.findViewById(R.id.iv_end_switch);
        mRlStartBg = (RelativeLayout) view.findViewById(R.id.rl_start_bg);
        mRlEndBg = (RelativeLayout) view.findViewById(R.id.rl_end_bg);
        mTvStartTime = (TextView) view.findViewById(R.id.tv_start_time);
        mTvEndTime = (TextView) view.findViewById(R.id.tv_end_time);
        mRlStart = (RelativeLayout) view.findViewById(R.id.rl_start);
        mRlEnd = (RelativeLayout) view.findViewById(R.id.rl_end);
        mLlStartWv = (LinearLayout) view.findViewById(R.id.ll_start_wv);
        mLlEndWv = (LinearLayout) view.findViewById(R.id.ll_end_wv);
        mWvStartAmPm = (AbstractWheel) view.findViewById(R.id.wv_start_ampm);
        mWvStartHour = (AbstractWheel) view.findViewById(R.id.wv_start_hour);
        mWvStartMin = (AbstractWheel) view.findViewById(R.id.wv_start_min);
        mWvEndAmPm = (AbstractWheel) view.findViewById(R.id.wv_end_ampm);
        mWvEndHour = (AbstractWheel) view.findViewById(R.id.wv_end_hour);
        mWvEndMin = (AbstractWheel) view.findViewById(R.id.wv_end_min);

        mRlStartBg.setVisibility(View.VISIBLE);
        mRlEndBg.setVisibility(View.VISIBLE);
        mLlStartWv.setVisibility(View.INVISIBLE);
        mLlEndWv.setVisibility(View.INVISIBLE);

        mRlStart.setOnClickListener(mEvent);
        mRlEnd.setOnClickListener(mEvent);
        mIvStartSwitch.setOnClickListener(mEvent);
        mIvEndSwitch.setOnClickListener(mEvent);

        mStartAmPmAdapter = new TimeAdapter(mActivity);
        mStartHourAdapter = new TimeAdapter(mActivity);
        mStartMinAdapter = new TimeAdapter(mActivity);
        mEndAmPmAdapter = new TimeAdapter(mActivity);
        mEndHourAdapter = new TimeAdapter(mActivity);
        mEndMinAdapter = new TimeAdapter(mActivity);

        setDateTime();

        mWvStartAmPm.setViewAdapter(mStartAmPmAdapter);
        mWvStartHour.setViewAdapter(mStartHourAdapter);
        mWvStartMin.setViewAdapter(mStartMinAdapter);
        mWvEndAmPm.setViewAdapter(mEndAmPmAdapter);
        mWvEndHour.setViewAdapter(mEndHourAdapter);
        mWvEndMin.setViewAdapter(mEndMinAdapter);

        mService.requestGetScheduleInDevice(mPlugVo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapterData();
    }

    private void fillAdapterData(){
        List<ScheduleVo> scheduleVoList = mService.selectDbScheduleList(mPlugVo);
        if( scheduleVoList != null && scheduleVoList.size() != 0){
            try {
                mScheduleVo = scheduleVoList.get(0);

                boolean isStartOn = mScheduleVo.isStartOn();
                mIvStartSwitch.setSelected(isStartOn);
                if (isStartOn){
                    mRlStartBg.setVisibility(View.INVISIBLE);
                    mLlStartWv.setVisibility(View.VISIBLE);
                } else {
                    mRlStartBg.setVisibility(View.VISIBLE);
                    mLlStartWv.setVisibility(View.INVISIBLE);
                }

                mStartAmPm = mScheduleVo.getStartAmPm();
                mStartHour = mScheduleVo.getStartTime().substring(0, 2);
                mStartMin = mScheduleVo.getStartTime().substring(3, 5);

                if (mStartAmPm.equalsIgnoreCase(SPConfig.AM)) {
                    mWvStartAmPm.setCurrentItem(0);
                } else {
                    mWvStartAmPm.setCurrentItem(1);
                }
                mWvStartHour.setCurrentItem(Integer.parseInt(mStartHour) - 1);
                mWvStartMin.setCurrentItem(Integer.parseInt(mStartMin));

                boolean isEndOn = mScheduleVo.isEndOn();
                mIvEndSwitch.setSelected(isEndOn);
                if (isEndOn){
                    mRlEndBg.setVisibility(View.GONE);
                    mLlEndWv.setVisibility(View.VISIBLE);
                } else {
                    mRlEndBg.setVisibility(View.VISIBLE);
                    mLlEndWv.setVisibility(View.INVISIBLE);
                }

                mEndAmPm = mScheduleVo.getEndAmPm();
                mEndHour = mScheduleVo.getEndTime().substring(0, 2);
                mEndMin = mScheduleVo.getEndTime().substring(3, 5);

                if (mEndAmPm.equalsIgnoreCase(SPConfig.AM)) {
                    mWvEndAmPm.setCurrentItem(0);
                } else {
                    mWvEndAmPm.setCurrentItem(1);
                }
                mWvEndHour.setCurrentItem(Integer.parseInt(mEndHour)-1);
                mWvEndMin.setCurrentItem(Integer.parseInt(mEndMin));
            } catch (NumberFormatException ne){
                ne.printStackTrace();
            }
        }
        mEvent.setScheduleVo(mScheduleVo);
        //mScheduleHandler.postDelayed(mScheduleTimeout, 10 * 1000);
        setWheel();
    }

    private void setDateTime(){
        // Start
        mStartAmPmAdapter.add(SPConfig.AM);
        mStartAmPmAdapter.add(SPConfig.PM);

        for(int hourCnt=0; hourCnt<12; hourCnt++) {
            String hour = "";
            if( hourCnt+1 < 10){
                hour = "0";
            }
            hour += String.valueOf(hourCnt+1);
            mStartHourAdapter.add(hour);
        }

        for(int minCnt=0; minCnt<60; minCnt++) {
            String min = "";
            if( minCnt < 10){
                min = "0";
            }
            min += String.valueOf(minCnt);
            mStartMinAdapter.add(min);
        }

        // End
        mEndAmPmAdapter.add(SPConfig.AM);
        mEndAmPmAdapter.add(SPConfig.PM);

        for(int hourCnt=0; hourCnt<12; hourCnt++) {
            String hour = "";
            if( hourCnt+1 < 10){
                hour = "0";
            }
            hour += String.valueOf(hourCnt+1);
            mEndHourAdapter.add(hour);
        }

        for(int minCnt=0; minCnt<60; minCnt++) {
            String min = "";
            if( minCnt < 10){
                min = "0";
            }
            min += String.valueOf(minCnt);
            mEndMinAdapter.add(min);
        }
    }

    private void setWheel(){
        mTvStartTime.setText(mStartAmPm + " " + mStartHour + ":" + mStartMin);
        mTvEndTime.setText(mEndAmPm + " " + mEndHour + ":" + mEndMin);

        mWvStartAmPm.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mStartAmPm = wheel.getCurrentItem() == 0 ? SPConfig.AM : SPConfig.PM;
                mScheduleVo.setStartAmPm(mStartAmPm);
                mTvStartTime.setText(mStartAmPm + " " + mStartHour + ":" + mStartMin);
            }
        });
        mWvEndAmPm.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mEndAmPm = wheel.getCurrentItem() == 0 ? SPConfig.AM : SPConfig.PM;
                mScheduleVo.setEndAmPm(mEndAmPm);
                mTvEndTime.setText(mEndAmPm + " " + mEndHour + ":" + mEndMin);
            }
        });
        mWvStartHour.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                int nHour = wheel.getCurrentItem() + 1;
                mStartHour = "";
                if (nHour < 10) {
                    mStartHour += "0";
                }
                mStartHour += String.valueOf(nHour);
                mScheduleVo.setStartTime(mStartHour + ":" + mStartMin);
                mTvStartTime.setText(mStartAmPm + " " + mStartHour + ":" + mStartMin);
            }
        });
        mWvEndHour.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                int nHour = wheel.getCurrentItem() + 1;
                mEndHour = "";
                if (nHour < 10) {
                    mEndHour += "0";
                }
                mEndHour += String.valueOf(nHour);
                mScheduleVo.setEndTime(mEndHour + ":" + mEndMin);
                mTvEndTime.setText(mEndAmPm + " " + mEndHour + ":" + mEndMin);
            }
        });
        mWvStartMin.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                int nMin = wheel.getCurrentItem();
                mStartMin = "";
                if (nMin < 10) {
                    mStartMin += "0";
                }
                mStartMin += String.valueOf(nMin);
                mScheduleVo.setStartTime(mStartHour + ":" + mStartMin);
                mTvStartTime.setText(mStartAmPm + " " + mStartHour + ":" + mStartMin);
            }
        });
        mWvEndMin.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                int nMin = wheel.getCurrentItem();
                mEndMin = "";
                if (nMin < 10) {
                    mEndMin += "0";
                }
                mEndMin += String.valueOf(nMin);
                mScheduleVo.setEndTime(mEndHour + ":" + mEndMin);
                mTvEndTime.setText(mEndAmPm + " " + mEndHour + ":" + mEndMin);
            }
        });
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
                        SPUtil.showToast(mActivity, "블루투스에 연결되지 않았습니다.");
                        fillAdapterData();
                        return false;
                    }
                }
                mService.setScheduleInDevice(mPlugVo, mScheduleVo);
                SPUtil.showDialog(mActivity);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScheduleResult(final ScheduleVo scheduleVo) {
        if( scheduleVo != null) {
            if( mScheduleVo.getStartTime().equalsIgnoreCase(SPConfig.NO_SCHEDULE)){
                scheduleVo.setStartTime("01:00");
            }
            if( mScheduleVo.getEndTime().equalsIgnoreCase(SPConfig.NO_SCHEDULE)){
                mScheduleVo.setEndTime("01:00");
            }
            Log.d(TAG, "Schedule Result Success (On/Off : " + scheduleVo.isStartOn() + ")");
            if( mService.updateDbSchedule(mPlugVo, scheduleVo)) {
                SPUtil.showToast(mActivity, "스케쥴을 저장하였습니다.");
            } else {
                SPUtil.showToast(mActivity, "스케쥴을 저장하지 못했습니다.");
            }
        } else {
            SPUtil.showToast(mActivity, "스케쥴 설정에 실패하였습니다");
        }
        fillAdapterData();
        SPUtil.dismissDialog();
    }
}
