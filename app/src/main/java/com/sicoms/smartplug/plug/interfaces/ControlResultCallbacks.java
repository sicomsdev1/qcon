package com.sicoms.smartplug.plug.interfaces;

import com.sicoms.smartplug.domain.PlugVo;

import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface ControlResultCallbacks {
    void onControlOnOffResult(PlugVo plugVo, boolean isOn);
    void onGroupControlOnOffResult(List<PlugVo> plugVoList, boolean isOn);
}