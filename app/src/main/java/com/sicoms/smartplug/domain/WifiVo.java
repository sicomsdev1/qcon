package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-03-24.
 */
public class WifiVo {
    private String ssid = "";
    private String bssid = "";
    private String password = "";
    private String securityType = "";

    public WifiVo(){}
    public WifiVo(String ssid, String bssid){
        this.ssid = ssid;
        this.bssid = bssid;
    }
    public WifiVo(String ssid, String bssid, String securityType){
        this.ssid = ssid;
        this.bssid = bssid;
        this.securityType = securityType;
    }
    public WifiVo(String ssid, String bssid, String password, String securityType){
        this.ssid = ssid;
        this.bssid = bssid;
        this.password = password;
        this.securityType = securityType;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }
}
