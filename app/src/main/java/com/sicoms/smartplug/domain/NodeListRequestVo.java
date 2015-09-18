package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public class NodeListRequestVo {
    private String msg = "";
    private String cmd = "";
    private String tr = "";
    private String tm = "";
    private String len = "";

    public NodeListRequestVo(){}
    public NodeListRequestVo(CommonDataVo commonData){
        msg = commonData.getMsg();
        cmd = commonData.getCmd();
        tr = commonData.getTr();
        tm = commonData.getTm();
    }

}
