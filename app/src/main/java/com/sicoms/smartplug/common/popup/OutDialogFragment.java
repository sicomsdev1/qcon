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

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.interfaces.OutCallbacks;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class OutDialogFragment extends DialogFragment {
    private static final String TAG = OutDialogFragment.class.getSimpleName();
    private static final String ARG_TOPIC_NAME = "topic_name";
    private static final String ARG_BUTTON_NAME = "button_name";

    private Context mContext;
    private static OutCallbacks mCallbacks;

    private TextView mTvTopicName;
    private Button mBtnOut;

    private String mTopicName = "";
    private String mBtnName = "";

    public static OutDialogFragment newInstance(OutCallbacks callbacks, String topicName, String btnName) {
        OutDialogFragment fragment = new OutDialogFragment();
        mCallbacks = callbacks;
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC_NAME, topicName);
        args.putString(ARG_BUTTON_NAME, btnName);
        fragment.setArguments(args);
        return fragment;
    }
    public static OutDialogFragment newInstance(OutCallbacks callbacks) {
        OutDialogFragment fragment = new OutDialogFragment();
        mCallbacks = callbacks;
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
            mTopicName = getArguments().getString(ARG_TOPIC_NAME);
            mBtnName = getArguments().getString(ARG_BUTTON_NAME);
        }
        if (savedInstanceState != null) {
            mTopicName = getArguments().getString(ARG_TOPIC_NAME);
            mBtnName = getArguments().getString(ARG_BUTTON_NAME);
        }

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_out, container, false);

        mTvTopicName = (TextView) view.findViewById(R.id.tv_topic_name);
        mBtnOut = (Button) view.findViewById(R.id.btn_out);

        if( mTopicName != null && mTopicName != ""){
            mTvTopicName.setText(mTopicName);
        }
        if( mBtnName != null && mBtnName != ""){
            mBtnOut.setText(mBtnName);
        }
        mBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onOutResult();
                dismiss();
            }
        });

        return view;
    }
}