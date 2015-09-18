package com.sicoms.smartplug.plug.interfaces;

import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.plug.adapter.RegDeviceAdapter;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface DeleteRegedPlugCallbacks {
    RegDeviceAdapter getRegDeviceAdapter();
    void onDeleteDeviceResult(RegDeviceVo regDeviceVo);
}