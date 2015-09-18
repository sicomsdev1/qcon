package com.sicoms.smartplug.plug.interfaces;

import com.sicoms.smartplug.domain.RegDeviceVo;

/**
 * Created by gudnam on 2015. 6. 13..
 */


public interface DialogFinishCallbacks {
    void onDialogFinishCallbacks(String dialogName, RegDeviceVo regDeviceVo);
}