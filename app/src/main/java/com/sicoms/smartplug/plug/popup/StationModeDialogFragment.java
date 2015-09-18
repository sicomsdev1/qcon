package com.sicoms.smartplug.plug.popup;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.interfaces.DialogFinishCallbacks;
import com.sicoms.smartplug.plug.service.RegDeviceService;
import com.sicoms.smartplug.plug.service.RegRouterService;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class StationModeDialogFragment extends DialogFragment implements UDPClient.UDPResponseCallbacks {
    private static final String TAG = StationModeDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private static DialogFinishCallbacks mCallbacks;
    private WifiVo mCurrentWifiVo;
    private UDPClient mUdpClient;
    private RegRouterService mService;
    private WifiConnectionManager mWifiConnectionManager;

    private int mResult;

    public static StationModeDialogFragment newInstance(DialogFinishCallbacks callbacks, WifiVo wifiVo) {
        StationModeDialogFragment fragment = new StationModeDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(wifiVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_wifi_connect_wait, container, false);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Bundle args = getArguments();
        if( args != null){
            mCurrentWifiVo = new Gson().fromJson(getArguments().getString(TAG), WifiVo.class);
        }
        mService = new RegRouterService(mActivity);
        mWifiConnectionManager = new WifiConnectionManager(mActivity);
        RegDeviceService regDeviceService = new RegDeviceService(mActivity);
        mCurrentWifiVo.setPassword(regDeviceService.loadWifiPassword());

        mUdpClient = new UDPClient();
        mUdpClient.setOnUDPResponseCallbacks(this);

        ImageView okBtn = (ImageView) view.findViewById(R.id.iv_confirm_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. UDP 통신으로 AP 에 로컬망 Wifi 정보 전달
                String requestJsonMessage = mService.getWifiInfoJsonData(mCurrentWifiVo);

                mUdpClient.execute(requestJsonMessage);
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if( getDialog() != null) {
            getDialog().dismiss();
        }
    }

    @Override
    public void onUDPResponseResultStatus(int result, String response) {
        if( mCallbacks != null){
            mCallbacks.onDialogFinishCallbacks(TAG, null);
            if( getDialog() != null) {
                getDialog().dismiss();
            }
        }
    }
}