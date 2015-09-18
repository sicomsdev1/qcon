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

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.interfaces.OutCallbacks;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class OutDialogFragment extends DialogFragment {
    private static final String TAG = OutDialogFragment.class.getSimpleName();

    private Context mContext;
    private static OutCallbacks mCallbacks;

    private Button mBtnOut;

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

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_out, container, false);

        mBtnOut = (Button) view.findViewById(R.id.btn_out);

        mBtnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.outOutResult();
                dismiss();
            }
        });

        return view;
    }
}