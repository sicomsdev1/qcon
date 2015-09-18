package com.sicoms.smartplug.login.event;

import android.app.Activity;
import android.view.View;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.login.interfaces.LoginResultCallbacks;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class LoginEvent implements View.OnClickListener {


    private Activity mActivity;
    private LoginResultCallbacks mCallbacks;

    public LoginEvent(Activity activity, LoginResultCallbacks callbacks){
        this.mActivity = activity;
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tv_login_btn :
                SPConfig.IS_SKIP = false;
                SPActivity.intentMainActivity(mActivity);
                break;
            case R.id.iv_membership_btn :
                SPFragment.intentMembershipFragmentDialog(mActivity, mCallbacks);
                break;
            case R.id.iv_skip_btn :
                SPConfig.IS_SKIP = true;
                SPActivity.intentMainActivity(mActivity);
                break;
        }
    }
}
