package com.sicoms.smartplug.plug.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.plug.interfaces.DialogFinishCallbacks;
import com.sicoms.smartplug.plug.service.RegDeviceService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class WifiSecurityDialogFragment extends DialogFragment {
    private static final String TAG = WifiSecurityDialogFragment.class.getSimpleName();

    private Context mContext;
    private RegDeviceVo mRegDeviceVo;
    private static DialogFinishCallbacks mListener;

    public static WifiSecurityDialogFragment newInstance(RegDeviceVo regDeviceVo, DialogFinishCallbacks listener) {
        WifiSecurityDialogFragment fragment = new WifiSecurityDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(regDeviceVo));
        fragment.setArguments(args);
        mListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
            mRegDeviceVo = new Gson().fromJson(getArguments().getString(TAG), RegDeviceVo.class);
        if (savedInstanceState != null)
            mRegDeviceVo = new Gson().fromJson(savedInstanceState.getString(TAG), RegDeviceVo.class);

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_wifi_security, container, false);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView okBtn = (ImageView) view.findViewById(R.id.network_assocoiation_ok);
        final EditText et_password = (EditText) view.findViewById(R.id.et_password);
        okBtn.setClickable(true);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtil.showDialog(mContext);
                new RegDeviceService(mContext).saveWifiPassword(et_password.getText().toString());
                mListener.onDialogFinishCallbacks(TAG, mRegDeviceVo);
                getDialog().dismiss();
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


}