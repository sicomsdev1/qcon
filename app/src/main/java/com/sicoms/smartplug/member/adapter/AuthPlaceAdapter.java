package com.sicoms.smartplug.member.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;

import java.util.ArrayList;

import antistatic.spinnerwheel.adapters.AbstractWheelTextAdapter;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class AuthPlaceAdapter extends AbstractWheelTextAdapter {
    // Count of days to be shown
    private final int authCount = 2;
    private ArrayList<String> authList;

    /**
     * Constructor
     */
    public AuthPlaceAdapter(Context context) {
        super(context, R.layout.adapter_auth, NO_RESOURCE);
        authList = new ArrayList<>();
        authList.add(SPConfig.MEMBER_MASTER_NAME);
        authList.add(SPConfig.MEMBER_USER_NAME);
        authList.add(SPConfig.MEMBER_SETTER_NAME);
        setItemTextResource(R.id.tv_auth);
    }

    @Override
    public View getItem(int position, View cachedView, ViewGroup parent) {
        View view = super.getItem(position, cachedView, parent);

        TextView tv_auth = (TextView) view.findViewById(R.id.tv_auth);
        tv_auth.setText(authList.get(position));

        return view;
    }

    @Override
    public int getItemsCount() {
        return authCount + 1;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return "";
    }
}