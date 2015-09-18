package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class CutoffRequestVo {
    private CommonDataVo commonData;
    private CutoffDataVo dp;

    public CutoffRequestVo(CommonDataVo commonData, CutoffDataVo dp){
        this.commonData = commonData;
        this.dp = dp;
    }

    public CommonDataVo getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonDataVo commonData) {
        this.commonData = commonData;
    }

    public CutoffDataVo getDp() {
        return dp;
    }

    public void setDp(CutoffDataVo dp) {
        this.dp = dp;
    }
}
