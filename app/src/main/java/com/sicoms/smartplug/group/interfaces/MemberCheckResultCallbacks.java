package com.sicoms.smartplug.group.interfaces;

import com.sicoms.smartplug.domain.UserVo;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface MemberCheckResultCallbacks {
    void onCheckedMember(UserVo plugVo);
    void onUnCheckedMember(UserVo plugVo);
}

//public interface AddScheduleResultCallbacks{
//    void onAddScheduleResult(DbScheduleVo vo);
//}
//    public void setOnAddScheduleResultCallbacks( final AddScheduleResultCallbacks callbacks){
//        mCallbacks = callbacks;
//    }
//
//}