package com.sicoms.smartplug.menu.event;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.menu.adapter.PlaceGalleryAdapter;
import com.sicoms.smartplug.menu.adapter.PlaceAdapter;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.interfaces.PlaceResultCallbacks;

/**
 * Created by gudnam on 2015. 6. 1..
 */
public class PlaceEvent implements PlaceAdapter.OnItemClickListener, View.OnClickListener, PlaceGalleryAdapter.OnItemClickListener {

    private Activity mActivity;
    private PlaceAdapter mAdapter;
    private PlaceResultCallbacks mCallbacks;
    private PictureMenuCallbacks mPictureCallbacks;
    private ImageSelectedResultCallbacks mImageCallbacks;

    public PlaceEvent(Context context){
        mActivity = (Activity) context;
    }
    public PlaceEvent(Context context, PlaceResultCallbacks callbacks){
        mActivity = (Activity) context;
        mCallbacks = callbacks;
    }
    public void setOnPictureMenuCallbacks(PictureMenuCallbacks callbacks){
        mPictureCallbacks = callbacks;
    }
    public void setOnImageSelectedResultCallbacks(ImageSelectedResultCallbacks callbacks){
        mImageCallbacks = callbacks;
    }
    public void setAdapter(PlaceAdapter adapter){
        mAdapter = adapter;
    }

    @Override
    public void onItemClick(View view, PlaceVo theVo) {
        mAdapter.setUnSelect();
        view.setSelected(true);
        theVo.setIsHere(true);
        mAdapter.notifyDataSetChanged();
        mCallbacks.onSelecteComplete(theVo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_add_place_btn :
                if( mCallbacks != null) {
                    SPFragment.intentAddPlaceFragment(mActivity, mCallbacks);
                }
                break;
            case R.id.rl_change_picture_btn :
                SPFragment.intentPictureMenuFragmentDialog(mActivity, mPictureCallbacks, SPConfig.PICTURE_MENU_TYPE_PLACE);
                break;
        }
    }

    @Override
    public void onItemClick(View view, String imageName) {
        if( mImageCallbacks != null){
            mImageCallbacks.onImageSelectedResult(imageName);
            ((ActionBarActivity) mActivity).getSupportFragmentManager().popBackStack();
        }
    }
}
