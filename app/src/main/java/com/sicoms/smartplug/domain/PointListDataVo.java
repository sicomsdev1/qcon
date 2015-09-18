package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public class PointListDataVo {
    private String ND_CODE;
    private String s; // On/Off 상태 (1:On, 0:Off)
    private String v; // 전압
    private String a; // 전류 (100 나눠야 함)
    private String w; // 전력
    private String wh; // 누적 사용량

    public String getND_CODE() {
        return ND_CODE;
    }

    public void setND_CODE(String ND_CODE) {
        this.ND_CODE = ND_CODE;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public String getWh() {
        return wh;
    }

    public void setWh(String wh) {
        this.wh = wh;
    }
}
