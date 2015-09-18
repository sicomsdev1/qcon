package com.sicoms.smartplug.group.interfaces;

import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.domain.PlugVo;

import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface CreateGroupResultCallbacks {
    void onGroupAddPlugList(List<PlugVo> plugVoList);
    void onGroupAddMemberList(List<UserVo> userVoList);
    void onGroupEditMember(UserVo userVo);
    void onCompleteCreateBLGroup(int groupId, boolean isCreate);
}

//public interface AddScheduleResultCallbacks{
//    void onAddScheduleResult(DbScheduleVo vo);
//}
//    public void setOnAddScheduleResultCallbacks( final AddScheduleResultCallbacks callbacks){
//        mCallbacks = callbacks;
//    }
//
//}