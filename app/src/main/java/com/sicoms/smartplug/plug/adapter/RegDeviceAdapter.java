package com.sicoms.smartplug.plug.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.RegDeviceVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-03-04.
 */
public class RegDeviceAdapter extends BaseAdapter {

    private Activity mActivity = null;
    private LayoutInflater mInflater = null;

    private ArrayList<RegDeviceVo> mVoList;

    public RegDeviceAdapter(Activity activity){
        mActivity = activity;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mVoList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mVoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mVoList.get(position);
    }

    public List<RegDeviceVo> getAll(){
        return mVoList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    ViewHolder viewHolder;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = new ViewHolder();

        RegDeviceVo regDeviceVo = null;
        try {
            regDeviceVo = mVoList.get(position);
        } catch (IndexOutOfBoundsException ioobe){
            ioobe.printStackTrace();
            return null;
        }

        view = mInflater.inflate(R.layout.adapter_reg_device, parent, false);

        viewHolder.ll_reg_device = (LinearLayout) view.findViewById(R.id.ll_reg_device);
        viewHolder.iv_plug_type = (ImageView) view.findViewById(R.id.iv_plug_type);
        viewHolder.iv_plug = (ImageView) view.findViewById(R.id.iv_plug);
        viewHolder.tv_plug_id = (TextView) view.findViewById(R.id.tv_plug_id);
        viewHolder.iv_reg_device = (ImageView) view.findViewById(R.id.iv_reg_device);
        viewHolder.rl_select = (RelativeLayout) view.findViewById(R.id.rl_select);
        viewHolder.ll_reg_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv = (ImageView) v.findViewById(R.id.iv_reg_device);
                iv.setSelected(!iv.isSelected());
                RegDeviceVo vo = (RegDeviceVo) iv.getTag();
                vo.setIsRegDevice(iv.isSelected());
                if( mVoList.size() > position) {
                    mVoList.get(position).setIsRegDevice(iv.isSelected());
                }
            }
        });

        viewHolder.iv_plug_type.setImageResource(regDeviceVo.getPlugTypeIconImg());
        viewHolder.iv_plug.setImageResource(regDeviceVo.getPlugIconImg());
        viewHolder.tv_plug_id.setText(regDeviceVo.getPlugId());
        viewHolder.iv_reg_device.setSelected(regDeviceVo.isRegDevice());
        viewHolder.iv_reg_device.setTag(regDeviceVo);


        return view;
    }

    public void addAll(List<RegDeviceVo> voList) {
        mVoList.addAll(voList);
    }

    public void add(RegDeviceVo vo){
        // 중복 검사
        for( RegDeviceVo savedVo : mVoList){
            if( savedVo.getPlugId() == vo.getPlugId()){
                return;
            }
        }
        mVoList.add(0, vo);
    }
    public void addAll(ArrayList<RegDeviceVo> listVo){
        mVoList.addAll(listVo);
    }
    public void removeAll(){
        mVoList.clear();
    }
    public void removeItem(RegDeviceVo vo){
        mVoList.remove(vo);
        notifyDataSetChanged();
    }
    public void setSelected(int position){
        viewHolder.iv_reg_device.setSelected(mVoList.get(position).isRegDevice());
    }

    private class ViewHolder{
        private LinearLayout ll_reg_device;
        private ImageView iv_plug_type;
        private ImageView iv_plug;
        private TextView tv_plug_id;
        private ImageView iv_reg_device;
        private RelativeLayout rl_select;
    }
}
