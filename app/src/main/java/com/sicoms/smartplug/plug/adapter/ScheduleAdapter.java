package com.sicoms.smartplug.plug.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.ScheduleVo;
import com.sicoms.smartplug.plug.interfaces.ScheduleResultCallbacks;
import com.sicoms.smartplug.plug.service.ScheduleService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class ScheduleAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater;
    private ScheduleResultCallbacks mCallbacks;
    private ScheduleService mService;
    private PlugVo mPlugVo;

    private List<ScheduleVo> mVoList;

    public ScheduleAdapter(Activity activity, ScheduleResultCallbacks callbacks, PlugVo plugVo){
        mActivity = activity;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCallbacks = callbacks;
        mVoList = new ArrayList<>();
        mService = new ScheduleService(mActivity);
        mService.setOnScheduleResultCallbacks(mCallbacks);
        mPlugVo = plugVo;
    }

    @Override
    public int getCount() {
        return mVoList.size();
    }

    @Override
    public ScheduleVo getItem(int position) {
        return mVoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addAll(List<ScheduleVo> voList){
        mVoList = voList;
    }
    public void add(ScheduleVo vo){
        mVoList.add(0, vo);
    }

    public void removeItem(int position){
        mVoList.remove(position);
    }

    public void removeItem(ScheduleVo vo){
        mVoList.remove(vo);
    }

    public void removeAll(){
        mVoList.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = new ViewHolder();

        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_schedule, parent, false);

            viewHolder.tv_start_ampm = (TextView) view.findViewById(R.id.tv_start_ampm);
            viewHolder.tv_end_ampm = (TextView) view.findViewById(R.id.tv_end_ampm);
            viewHolder.tv_start_time = (TextView) view.findViewById(R.id.tv_start_time);
            viewHolder.tv_end_time = (TextView) view.findViewById(R.id.tv_end_time);
            viewHolder.rl_off_bg = (RelativeLayout) view.findViewById(R.id.rl_off_bg);

            final ImageView iv_switch = (ImageView) view.findViewById(R.id.iv_switch);
            iv_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mService.setScheduleInDevice(mPlugVo, mVoList.get(position));
                    iv_switch.setSelected(!iv_switch.isSelected());
                    mVoList.get(position).setIsStartOn(iv_switch.isSelected());
                }
            });
            viewHolder.iv_switch = iv_switch;
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tv_start_ampm.setText(mVoList.get(position).getStartAmPm());
        viewHolder.tv_end_ampm.setText(mVoList.get(position).getEndAmPm());
        viewHolder.tv_start_time.setText(mVoList.get(position).getStartTime());
        viewHolder.tv_end_time.setText(mVoList.get(position).getEndTime());

        if( mVoList.get(position).isStartOn()){
            viewHolder.iv_switch.setSelected(true);
            viewHolder.rl_off_bg.setVisibility(View.GONE);
        } else {
            viewHolder.iv_switch.setSelected(false);
            viewHolder.rl_off_bg.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private class ViewHolder {
        private TextView tv_start_ampm;
        private TextView tv_end_ampm;
        private TextView tv_start_time;
        private TextView tv_end_time;
        private ImageView iv_switch;
        private RelativeLayout rl_off_bg;
    }
}
