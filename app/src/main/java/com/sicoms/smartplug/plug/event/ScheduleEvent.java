package com.sicoms.smartplug.plug.event;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.ScheduleVo;
import com.sicoms.smartplug.plug.interfaces.ScheduleResultCallbacks;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class ScheduleEvent implements View.OnClickListener {

    private Activity mActivity;
    private Fragment mFragment;
    private ScheduleVo mScheduleVo;
    private ScheduleResultCallbacks mCallbacks;

    public ScheduleEvent(Fragment fragment){
        mActivity = fragment.getActivity();
        mFragment = fragment;
    }
    public ScheduleEvent(Fragment fragment, ScheduleResultCallbacks callbacks){
        mActivity = fragment.getActivity();
        mFragment = fragment;
        mCallbacks = callbacks;
    }
    public void setScheduleVo(ScheduleVo scheduleVo){
        mScheduleVo = scheduleVo;
    }

    @Override
    public void onClick(View v) {
        ViewGroup root = (ViewGroup) v.getRootView();

        switch (v.getId()) {
            // Schedule
            case R.id.iv_start_switch :
                boolean isStartSelect = !v.isSelected();
                mScheduleVo.setIsStartOn(isStartSelect);
                RelativeLayout rl_start_bg = (RelativeLayout) root.findViewById(R.id.rl_start_bg);
                LinearLayout ll_start_wv = (LinearLayout) root.findViewById(R.id.ll_start_wv);
                ImageView iv_start_switch = (ImageView) root.findViewById(R.id.iv_start_switch);
                iv_start_switch.setSelected(isStartSelect);
                if(isStartSelect){
                    rl_start_bg.setVisibility(View.INVISIBLE);
                } else {
                    rl_start_bg.setVisibility(View.VISIBLE);
                }
                v.setSelected(isStartSelect);
                break;
            case R.id.iv_end_switch :
                boolean isEndSelect = !v.isSelected();
                mScheduleVo.setIsEndOn(isEndSelect);
                RelativeLayout rl_end_bg = (RelativeLayout) root.findViewById(R.id.rl_end_bg);
                LinearLayout ll_end_wv = (LinearLayout) root.findViewById(R.id.ll_end_wv);
                ImageView iv_end_switch = (ImageView) root.findViewById(R.id.iv_end_switch);
                iv_end_switch.setSelected(isEndSelect);
                if(isEndSelect){
                    rl_end_bg.setVisibility(View.INVISIBLE);
                } else {
                    rl_end_bg.setVisibility(View.VISIBLE);
                }
                v.setSelected(isEndSelect);
                break;

            // Add Schedule
            case R.id.rl_start :
                LinearLayout ll_start_edit = (LinearLayout)root.findViewById(R.id.ll_start_edit);
                if( ll_start_edit.getVisibility() == View.VISIBLE) {
                    ll_start_edit.setVisibility(View.INVISIBLE);
                } else {
                    ll_start_edit.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rl_end :
                LinearLayout ll_end_edit = (LinearLayout)root.findViewById(R.id.ll_end_edit);
                if( ll_end_edit.getVisibility() == View.VISIBLE) {
                    ll_end_edit.setVisibility(View.INVISIBLE);
                } else {
                    ll_end_edit.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
