package com.sicoms.smartplug.plug.interfaces;

import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.RegDeviceVo;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface RegPlugResultCallbacks {
    void onRegCompleteResult(RegDeviceVo regDeviceVo);
}