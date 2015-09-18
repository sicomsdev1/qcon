package com.sicoms.smartplug.menu.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.event.PlaceSettingEvent;
import com.sicoms.smartplug.menu.interfaces.PlaceSettingResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.bluetooth.BLConfig;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class PlaceSettingFragment extends Fragment implements PlaceSettingResultCallbacks {

    private static final String TAG = PlaceSettingFragment.class.getSimpleName();

    private Context mContext;

    private PlaceSettingEvent mEvent;
    private PlaceSettingService mService;

    private LinearLayout mLlChangePassword;
    private RelativeLayout mRlChangePasswordBtn;
    private RelativeLayout mRlEditPassword;
    private TextView mTvBLPassword;

    private ImageView mIvFinishBtn;

    public static PlaceSettingFragment newInstance() {
        PlaceSettingFragment fragment = new PlaceSettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_place_setting, container, false);

        mContext = getActivity();
        mEvent = new PlaceSettingEvent(mContext);
        mEvent.setOnPlaceSettingResultCallbacks(this);
        mService = new PlaceSettingService(mContext);

        mLlChangePassword = (LinearLayout) view.findViewById(R.id.ll_change_password);
        mRlChangePasswordBtn = (RelativeLayout) view.findViewById(R.id.rl_change_password_btn);
        mRlEditPassword = (RelativeLayout) view.findViewById(R.id.rl_edit_password);
        mRlEditPassword.setVisibility(View.GONE);
        mIvFinishBtn = (ImageView) view.findViewById(R.id.iv_finish_btn);
        mTvBLPassword = (TextView) view.findViewById(R.id.tv_bl_password);

        int auth = 1;
        try {
            auth = Integer.parseInt(PlaceService.loadLastPlace(mContext).getAuth());
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        if(auth == SPConfig.MEMBER_MASTER) {
            mLlChangePassword.setVisibility(View.VISIBLE);
            mRlChangePasswordBtn.setOnClickListener(mEvent);
            mIvFinishBtn.setOnClickListener(mEvent);
        } else {
            mLlChangePassword.setVisibility(View.INVISIBLE);
        }

        PlaceSettingVo settingVo = mService.selectDbBLPassword();
        if( settingVo == null){
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            settingVo = new PlaceSettingVo(placeVo.getPlaceId(), SPConfig.PLACE_SETTING_BL_PASSWORD, BLConfig.BL_DEFAULT_SECURITY_PASSWORD);
            mService.updateDbBLPassword(settingVo);
            mService.requestUpdateBLPassword(settingVo);
        }
        mTvBLPassword.setText("(" + settingVo.getSetVal() + ")");

        return view;
    }

    @Override
    public void onPlaceSettingResult(String password) {
        mTvBLPassword.setText(password);
    }
}
