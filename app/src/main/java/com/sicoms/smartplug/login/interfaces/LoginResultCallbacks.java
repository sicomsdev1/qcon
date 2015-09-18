package com.sicoms.smartplug.login.interfaces;

import com.sicoms.smartplug.domain.UserVo;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface LoginResultCallbacks {
    void onMembershipResult(UserVo loginVo);
}