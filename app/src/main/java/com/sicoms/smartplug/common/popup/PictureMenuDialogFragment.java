package com.sicoms.smartplug.common.popup;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PictureMenuDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = PictureMenuDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private static PictureMenuCallbacks mCallbacks;

    private LinearLayout mLlPictureAlbum;
    private LinearLayout mLlCamera;
    private LinearLayout mLlDefaultImage;

    public static PictureMenuDialogFragment newInstance(PictureMenuCallbacks callbacks, int type) {
        PictureMenuDialogFragment fragment = new PictureMenuDialogFragment();
        mCallbacks = callbacks;
        Bundle args = new Bundle();
        args.putInt(TAG, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_picture_menu, container, false);

        int type = SPConfig.PICTURE_MENU_TYPE_HOME;
        Bundle args = getArguments();
        if (args != null)
            type = getArguments().getInt(TAG);
        if (savedInstanceState != null)
            type = savedInstanceState.getInt(TAG);

        mLlPictureAlbum = (LinearLayout) view.findViewById(R.id.ll_picture_album);
        mLlCamera = (LinearLayout) view.findViewById(R.id.ll_camera);
        mLlDefaultImage = (LinearLayout) view.findViewById(R.id.ll_default_image);

        mLlPictureAlbum.setOnClickListener(this);
        mLlCamera.setOnClickListener(this);
        mLlDefaultImage.setOnClickListener(this);

        if( type == SPConfig.PICTURE_MENU_TYPE_HOME){
            mLlDefaultImage.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        getDialog().dismiss();
        switch (v.getId()){
            case R.id.ll_picture_album :
                mCallbacks.onPictureMenuResult(SPConfig.PICTURE_MENU_ALBUM);
                break;
            case R.id.ll_camera :
                mCallbacks.onPictureMenuResult(SPConfig.PICTURE_MENU_CAMERA);
                break;
            case R.id.ll_default_image :
                mCallbacks.onPictureMenuResult(SPConfig.PICTURE_MENU_DEFAULT_IMAGE);
                break;
        }
    }
}