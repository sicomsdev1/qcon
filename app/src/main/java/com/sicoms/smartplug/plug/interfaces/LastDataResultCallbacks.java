package com.sicoms.smartplug.plug.interfaces;

import com.sicoms.smartplug.dao.DbLastDataVo;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface LastDataResultCallbacks {
    void onGetLastData(String deviceId, String wh, String w, String onoff);
}