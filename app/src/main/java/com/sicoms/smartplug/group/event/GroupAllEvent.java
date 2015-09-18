package com.sicoms.smartplug.group.event;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.group.adapter.GroupAdapter;
import com.sicoms.smartplug.group.service.GroupAllService;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class GroupAllEvent implements View.OnClickListener, GroupAdapter.OnItemClickListener {

    private Context mContext;
    private GroupAllService mService;

    public GroupAllEvent(Context context) {
        mContext = context;
    }

    public void setService(GroupAllService service){
        mService = service;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Group Main
            case R.id.iv_group_add_btn :
                SPActivity.intentGroupCreatorActivity((Activity) mContext);
                break;
            case R.id.iv_sync_btn :
                // Cloud Server에 그룹 정보 요청
                if( mService != null) {
                    SPUtil.showDialog(mContext);
                    mService.requestSelectGroupList();
                }
                break;
        }
    }

    @Override
    public void onItemClick(View view, GroupVo theVo) {
        GroupService groupService = new GroupService(mContext);
        try {
            theVo = new GroupAllService(mContext).selectDbGroup(Long.parseLong(theVo.getGroupId()));
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        groupService.saveLastGroupVo(theVo);
        SPActivity.intentGroupActivity((Activity) mContext, theVo);
    }
}
