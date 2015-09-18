package com.sicoms.smartplug.group.interfaces;

import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;

import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface EditGroupResultCallbacks {
    void onCompleteEditPlug();
    void onCompleteEditMember();
}