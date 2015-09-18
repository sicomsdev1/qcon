package com.sicoms.smartplug.plug.interfaces;

import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface AssociatedDevicesResultCallbacks {
    void onGetAssociatedDevice(String plugId, List<String> groupIdList);
}