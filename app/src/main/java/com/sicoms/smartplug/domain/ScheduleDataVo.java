package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class ScheduleDataVo {
    private String d;
    private String s;
    private String e;
    private String u;

    public ScheduleDataVo(String d, String s, String e, String u){
        this.d = d;
        this.s = s;
        this.e = e;
        this.u = u;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }
}
