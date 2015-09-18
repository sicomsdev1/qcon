package com.sicoms.smartplug.main.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.NavigationDrawerVo;

import java.util.ArrayList;
import java.util.List;

public class NavigationDrawerAdapter extends BaseAdapter{

	private Activity mActivity = null;
	private LayoutInflater mInflater = null;
	
	private List<NavigationDrawerVo> mList;
	
	private int mContentColor = 0;
	private int mPriceColor = 0;
	private int mContentSize = 0;
	private int mPriceSize = 0;
	
	public NavigationDrawerAdapter(Activity activity){
		mActivity = activity;
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<NavigationDrawerVo>();
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final int pos = position;
		View view = convertView;
		ViewHolder holder = new ViewHolder();

		TextView tv_group_name = null;
        
        if ( view == null ) {
        	view = mInflater.inflate(R.layout.adapter_navigation_drawer, parent, false);

            tv_group_name = (TextView) view.findViewById(R.id.tv_group_name);
            
            holder.tv_group_name = tv_group_name;
            
            view.setTag(holder);
        } 
        else{
        	holder = (ViewHolder) view.getTag();
            tv_group_name = holder.tv_group_name;
		}

        tv_group_name.setText(mList.get(pos).getGroupName());
        
		return view;
	}
	
	public void add(NavigationDrawerVo vo){
		mList.add(vo);
	}

    public void addAll(List<NavigationDrawerVo> voList){
        mList.addAll(voList);
    }
	// 0 = default
	public void setColor(int content, int price){
		mContentColor = content;
		mPriceColor = price;
	}
	
	// 0 = default
	public void setSize(int content, int price){
		mContentSize = content;
		mPriceSize = price;
	}

	private class ViewHolder {
    	TextView tv_group_name;
    }
}
