package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class CutoffResponseVo {
    private CommonDataVo commonData;
    private String ret;
    private CutoffDataVo dp;

    public CutoffResponseVo(CommonDataVo commonData, String ret, CutoffDataVo dp){
        this.commonData = commonData;
        this. ret = ret;
        this.dp = dp;
    }

    public CommonDataVo getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonDataVo commonData) {
        this.commonData = commonData;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public CutoffDataVo getDp() {
        return dp;
    }

    public void setDp(CutoffDataVo dp) {
        this.dp = dp;
    }
}
