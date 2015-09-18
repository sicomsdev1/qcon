package com.sicoms.smartplug.group.event;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class CreateGroupEvent implements View.OnClickListener {

    private Activity mActivity;
    private Fragment mFragment;
    private GroupVo mGroupVo;
    private CreateGroupResultCallbacks mCallbacks;
    private PictureMenuCallbacks mPictureCallbacks;
    private ImageSelectedResultCallbacks mImageCallbacks;

    public CreateGroupEvent(Fragment fragment, CreateGroupResultCallbacks callbacks){
        mActivity = fragment.getActivity();
        mFragment = fragment;
        mCallbacks = callbacks;
    }

    public void setGroupVo(GroupVo groupVo){
        mGroupVo = groupVo;
    }

    public void setOnPictureMenuCallbacks(PictureMenuCallbacks callbacks){
        mPictureCallbacks = callbacks;
    }
    public void setOnImageSelectedResultCallbacks(ImageSelectedResultCallbacks callbacks){
        mImageCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // Create Group
            case R.id.iv_camera_btn :
                SPFragment.intentPictureMenuFragmentDialog(mActivity, mPictureCallbacks, SPConfig.PICTURE_MENU_TYPE_PLACE);
                break;
            case R.id.ll_add_plug_btn :
                if( mCallbacks != null) {
                    SPFragment.intentPlugAddGroupListFragment(mActivity, mCallbacks, mGroupVo);
                }
                break;
            case R.id.ll_add_member_btn :
                SPFragment.intentMemberAddGroupListFragment(mActivity, mCallbacks, mGroupVo);
                break;
        }
    }
}
