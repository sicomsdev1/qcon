package com.sicoms.smartplug.domain;

import java.util.List;

/**
 * Created by pc-11-user on 2015-04-09.
 */
public class WifiModeResponseVo {
    private String msg;
    private String cmd;
    private String tr;
    private String tm;
    private String len;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getTr() {
        return tr;
    }

    public void setTr(String tr) {
        this.tr = tr;
    }

    public String getTm() {
        return tm;
    }

    public void setTm(String tm) {
        this.tm = tm;
    }

    public String getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = len;
    }
}
