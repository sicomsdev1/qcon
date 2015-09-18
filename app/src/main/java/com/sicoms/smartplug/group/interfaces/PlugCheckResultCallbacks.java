package com.sicoms.smartplug.group.interfaces;

import com.sicoms.smartplug.domain.PlugVo;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface PlugCheckResultCallbacks {
    void onCheckedPlug(PlugVo plugVo);
    void onUnCheckedPlug(PlugVo plugVo);
}

//public interface AddScheduleResultCallbacks{
//    void onAddScheduleResult(DbScheduleVo vo);
//}
//    public void setOnAddScheduleResultCallbacks( final AddScheduleResultCallbacks callbacks){
//        mCallbacks = callbacks;
//    }
//
//}