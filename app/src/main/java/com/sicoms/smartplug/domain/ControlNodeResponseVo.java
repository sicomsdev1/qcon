package com.sicoms.smartplug.domain;

import java.util.List;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public class ControlNodeResponseVo {
    private String msg;
    private String cmd;
    private String tr;
    private String ag;
    private String tm;
    private String from;
    private String to;
    private String len;
    private List<ControlNodeDataVo> dp;
    private String ret;

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

    public String getAg() {
        return ag;
    }

    public void setAg(String ag) {
        this.ag = ag;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = len;
    }

    public List<ControlNodeDataVo> getDp() {
        return dp;
    }

    public void setDp(List<ControlNodeDataVo> dp) {
        this.dp = dp;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }
}
