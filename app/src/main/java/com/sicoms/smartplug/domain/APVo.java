package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class APVo {
    private String bssid;
    private String ssid;

    public APVo(String bssid, String ssid){
        this.bssid = bssid;
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
