package com.sicoms.smartplug.domain;

import java.util.List;

/**
 * Created by pc-11-user on 2015-04-09.
 */
public class WifiModeRequestVo {
    private String msg;
    private String cmd;
    private String tr;
    private String tm;
    private String len;
    private WifiModeData dp;

    public WifiModeRequestVo(CommonDataVo commonData){
        msg = commonData.getMsg();
        cmd = commonData.getCmd();
        tr = commonData.getTr();
        tm = commonData.getTm();
    }

    public WifiModeData getDp() {
        return dp;
    }

    public void setDp(WifiModeData dp) {
        this.dp = dp;
    }
}
