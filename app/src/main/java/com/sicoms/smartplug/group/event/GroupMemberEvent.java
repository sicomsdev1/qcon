package com.sicoms.smartplug.group.event;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.member.adapter.MemberAdapter;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

import java.util.List;

import antistatic.spinnerwheel.AbstractWheel;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class GroupMemberEvent implements View.OnClickListener, MemberAdapter.OnItemClickListener {

    private Activity mActivity;
    private UserVo mUserVo;
    private GroupService mService;
    private HttpResponseCallbacks mHttpCallbacks;
    private AbstractWheel mWvAuth;

    public GroupMemberEvent(Activity activity, UserVo userVo){
        mActivity = activity;
        mUserVo = userVo;
        mService = new GroupService(mActivity);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mHttpCallbacks = callbacks;
        mService.setOnHttpResponseCallbacks(mHttpCallbacks);
    }
    public void setWheelAuth(AbstractWheel wvAuth){
        mWvAuth = wvAuth;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            // Member
            case R.id.iv_add_member_btn :
                SPFragment.intentAddMemberFragment(mActivity);
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
                GroupService service = new GroupService(mActivity);
                GroupVo groupVo = service.loadLastGroup();
                List<UserVo> userVoList = groupVo.getUserVoList();
                for(int cnt=0; cnt<userVoList.size(); cnt++){
                    UserVo userVo = userVoList.get(cnt);
                    if( mUserVo.getUserId().equalsIgnoreCase(userVo.getUserId())){
                        userVo.setAuth(mUserVo.getAuth());
                    }
                }

                mService.requestUpdateGroupUserMapping(groupVo, mUserVo);
                break;
        }
    }

    @Override
    public void onItemClick(View view, UserVo theVo) {
        SPFragment.intentEditMemberFragment(mActivity, theVo);
    }
}
