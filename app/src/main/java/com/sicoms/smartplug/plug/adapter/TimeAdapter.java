package com.sicoms.smartplug.plug.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.ScheduleVo;

import java.util.ArrayList;

import antistatic.spinnerwheel.adapters.AbstractWheelTextAdapter;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class TimeAdapter extends AbstractWheelTextAdapter {
    // Count of days to be shown
    private ArrayList<String> mList;

    /**
     * Constructor
     */
    public TimeAdapter(Context context) {
        super(context, R.layout.adapter_time, NO_RESOURCE);
        mList = new ArrayList<>();
        setItemTextResource(R.id.tv_time);
    }

    @Override
    public View getItem(int position, View cachedView, ViewGroup parent) {
        View view = super.getItem(position, cachedView, parent);

        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        tv_time.setText(mList.get(position));

        return view;
    }

    @Override
    public int getItemsCount() {
        return mList.size();
    }

    @Override
    protected CharSequence getItemText(int index) {
        return "";
    }

    public void add(String item){
        mList.add(item);
    }

    public String get(int position){
        return mList.get(position);
    }
}