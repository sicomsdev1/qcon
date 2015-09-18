package com.sicoms.smartplug.login.popup;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPTerms;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class TermsDialogFragment extends DialogFragment {
    private static final String TAG = TermsDialogFragment.class.getSimpleName();

    private Activity mActivity;

    private TextView mTvTerms;

    public static TermsDialogFragment newInstance() {
        TermsDialogFragment fragment = new TermsDialogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_terms, container, false);

        mTvTerms = (TextView) view.findViewById(R.id.tv_terms);

        mTvTerms.setText(SPTerms.TERMS);

        return view;
    }
}