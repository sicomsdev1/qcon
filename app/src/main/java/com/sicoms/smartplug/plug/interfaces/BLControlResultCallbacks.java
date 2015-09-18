package com.sicoms.smartplug.plug.interfaces;

import com.sicoms.smartplug.dao.DbLastDataVo;

import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface BLControlResultCallbacks {
    void onBLControlOnOffResult(String deviceId, String onoff);
    void onBLAllControlOnOffResult(List<DbLastDataVo> dbLastDataVoList);
}