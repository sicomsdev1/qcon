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
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;
import com.sicoms.smartplug.plug.service.CutoffService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class CutoffAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater;
    private CutoffResultCallbacks mCallbacks;
    private CutoffService mService;
    private PlugVo mPlugVo;

    private List<CutoffVo> mVoList;

    public CutoffAdapter(Activity activity, CutoffResultCallbacks callbacks, PlugVo plugVo){
        mActivity = activity;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCallbacks = callbacks;
        mVoList = new ArrayList<>();
        mService = new CutoffService(mActivity);
        mService.setOnCutoffResultCallbacks(callbacks);
        mPlugVo = plugVo;
    }

    @Override
    public int getCount() {
        return mVoList.size();
    }

    @Override
    public CutoffVo getItem(int position) {
        return mVoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addAll(List<CutoffVo> voList){
        mVoList = voList;
    }
    public void add(CutoffVo vo){
        mVoList.add(0, vo);
    }

    public void removeItem(int position){
        mVoList.remove(position);
    }

    public void removeItem(CutoffVo vo){
        mVoList.remove(vo);
    }

    public void removeAll(){mVoList.clear();}

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = new ViewHolder();

        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_cutoff, parent, false);

            viewHolder.tv_power = (TextView) view.findViewById(R.id.tv_power);
            viewHolder.tv_time = (TextView) view.findViewById(R.id.tv_time);
            viewHolder.rl_off_bg = (RelativeLayout) view.findViewById(R.id.rl_off_bg);

            final ImageView iv_switch = (ImageView) view.findViewById(R.id.iv_switch);
            iv_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mService.setCutoffDeviceInDevice(mPlugVo, mVoList.get(position));
                    iv_switch.setSelected(!iv_switch.isSelected());
                    mVoList.get(position).setIsOn(iv_switch.isSelected());
                }
            });
            viewHolder.iv_switch = iv_switch;
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tv_power.setText(mVoList.get(position).getPower());
        viewHolder.tv_time.setText(mVoList.get(position).getMin());

        if( mVoList.get(position).isOn()){
            viewHolder.iv_switch.setSelected(true);
            viewHolder.rl_off_bg.setVisibility(View.GONE);
        } else {
            viewHolder.iv_switch.setSelected(false);
            viewHolder.rl_off_bg.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private class ViewHolder {
        private TextView tv_power;
        private TextView tv_time;
        private ImageView iv_switch;
        private RelativeLayout rl_off_bg;
    }
}
