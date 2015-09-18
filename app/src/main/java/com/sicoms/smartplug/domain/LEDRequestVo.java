package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class LEDRequestVo {
    private CommonDataVo commonData;
    private LEDDataVo dp;

    public LEDRequestVo(CommonDataVo commonData, LEDDataVo dp){
        this.commonData = commonData;
        this.dp = dp;
    }

    public CommonDataVo getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonDataVo commonData) {
        this.commonData = commonData;
    }

    public LEDDataVo getDp() {
        return dp;
    }

    public void setDp(LEDDataVo dp) {
        this.dp = dp;
    }
}
