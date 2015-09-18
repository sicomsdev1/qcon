package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class LEDResponseVo {
    private CommonDataVo commonData;
    private String ret;
    private LEDDataVo dp;

    public LEDResponseVo(CommonDataVo commonData, LEDDataVo dp){
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

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }
}
