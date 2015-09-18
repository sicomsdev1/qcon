package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class ScheduleRequestVo {
    private CommonDataVo commonData;
    private ScheduleDataVo dp;

    public ScheduleRequestVo(CommonDataVo commonData, ScheduleDataVo dp) {
        this.commonData = commonData;
        this.dp = dp;
    }

    public void setCommonData(CommonDataVo onData) {
        this.commonData = commonData;
    }

    public CommonDataVo getCommonData() {
        return commonData;
    }

    public ScheduleDataVo getDp() {
        return dp;
    }

    public void setDp(ScheduleDataVo dp) {
        this.dp = dp;
    }
}
