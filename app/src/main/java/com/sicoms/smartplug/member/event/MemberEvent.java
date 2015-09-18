package com.sicoms.smartplug.member.event;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.member.adapter.MemberAdapter;
import com.sicoms.smartplug.member.service.MemberService;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

import antistatic.spinnerwheel.AbstractWheel;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class MemberEvent implements View.OnClickListener, MemberAdapter.OnItemClickListener {

    private Context mContext;
    private UserVo mUserVo;
    private MemberService mService;
    private HttpResponseCallbacks mCallbacks;
    private AbstractWheel mWvAuth;

    public MemberEvent(Context contex){
        mContext = contex;
        mService = new MemberService(mContext);
    }
    public MemberEvent(Context context, UserVo userVo){
        mContext = context;
        mUserVo = userVo;
        mService = new MemberService(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
        mService.setOnHttpResponseCallbacks(mCallbacks);
    }
    public void setWheelAuth(AbstractWheel wvAuth){
        mWvAuth = wvAuth;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            // Member
            case R.id.iv_add_member_btn :
                SPFragment.intentAddMemberFragment((Activity) mContext);
                break;

            // Add Member
            case R.id.rl_auth_btn :
                ViewGroup root = (ViewGroup) v.getRootView();
                RelativeLayout rl_auth = (RelativeLayout)root.findViewById(R.id.rl_auth);
                ImageView iv_arrow_btn = (ImageView) root.findViewById(R.id.iv_arrow_btn);

                if( rl_auth.getVisibility() == View.VISIBLE) {
                    rl_auth.setVisibility(View.GONE);
                    iv_arrow_btn.setSelected(false);
                } else {
                    rl_auth.setVisibility(View.VISIBLE);
                    iv_arrow_btn.setSelected(true);
                }
                break;

            // Edit Member
            case R.id.iv_finish_btn :
                int auth = mWvAuth.getCurrentItem();
                mUserVo.setAuth(auth);
                mService.requestUpdateMember(mUserVo);
                break;
        }
    }

    @Override
    public void onItemClick(View view, UserVo theVo) {
        SPFragment.intentEditMemberFragment((Activity) mContext, theVo);
    }
}
