package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-09.
 */
public class WifiModeData {
    String m; // "s" : station mode, "a" : ap mode
    String i; // ssid
    String a; // auth,password

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }

    public String getI() {
        return i;
    }

    public void setI(String i) {
        this.i = i;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }
}
