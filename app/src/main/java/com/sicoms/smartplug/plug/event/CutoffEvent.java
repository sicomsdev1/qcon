package com.sicoms.smartplug.plug.event;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class CutoffEvent implements View.OnClickListener {

    private Context mContext;
    private CutoffVo mCutoffVo;
    private CutoffResultCallbacks mCallbacks;
    private PlugVo mPlugVo;

    public CutoffEvent(Context context, CutoffResultCallbacks callbacks, PlugVo plugVo){
        mContext = context;
        mCallbacks = callbacks;
        mPlugVo = plugVo;
    }
    public void setCutoffVo(CutoffVo cutoffVo){
        mCutoffVo = cutoffVo;
    }

    @Override
    public void onClick(View v) {
        ViewGroup root = (ViewGroup) v.getRootView();
        switch (v.getId()) {
            // Cutoff
            case R.id.iv_curoff_switch :
                boolean isStartSelect = !v.isSelected();
                mCutoffVo.setIsOn(isStartSelect);
                RelativeLayout rl_cutoff_bg = (RelativeLayout) root.findViewById(R.id.rl_cutoff_bg);
                ImageView iv_curoff_switch = (ImageView) root.findViewById(R.id.iv_curoff_switch);
                iv_curoff_switch.setSelected(isStartSelect);
                if(isStartSelect){
                    rl_cutoff_bg.setVisibility(View.INVISIBLE);
                } else {
                    rl_cutoff_bg.setVisibility(View.VISIBLE);
                }
                v.setSelected(isStartSelect);
                break;
        }
    }
}
