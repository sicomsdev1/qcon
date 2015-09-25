package com.sicoms.smartplug.common.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.interfaces.ConfirmCallbacks;
import com.sicoms.smartplug.common.interfaces.OutCallbacks;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class ConfirmDialogFragment extends DialogFragment {
    private static final String TAG = ConfirmDialogFragment.class.getSimpleName();
    private static final String ARG_TOPIC_NAME = "topic_name";
    private static final String ARG_BUTTON_NAME = "button_name";

    private Context mContext;
    private static ConfirmCallbacks mCallbacks;
    private RegDeviceVo mRegDeviceVo;

    private TextView mTvTopicName;
    private Button mBtnReg;

    private String mTopicName = "";
    private String mBtnName = "";

    public static ConfirmDialogFragment newInstance(ConfirmCallbacks callbacks, RegDeviceVo regDeviceVo) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        mCallbacks = callbacks;
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(regDeviceVo));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Bundle args = getArguments();
        if (args != null) {
            mRegDeviceVo = new Gson().fromJson(getArguments().getString(TAG), RegDeviceVo.class);
        }
        if (savedInstanceState != null) {
            mRegDeviceVo = new Gson().fromJson(getArguments().getString(TAG), RegDeviceVo.class);
        }

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_confirm, container, false);

        mTvTopicName = (TextView) view.findViewById(R.id.tv_topic_name);
        mBtnReg = (Button) view.findViewById(R.id.btn_reg);

        if( mTopicName != null && mTopicName != ""){
            mTvTopicName.setText(mTopicName);
        }
        if( mBtnName != null && mBtnName != ""){
            mBtnReg.setText(mBtnName);
        }
        mBtnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onConfirmResult(mRegDeviceVo);
                dismiss();
            }
        });

        SPUtil.showDialog(mContext);
        return view;
    }
}