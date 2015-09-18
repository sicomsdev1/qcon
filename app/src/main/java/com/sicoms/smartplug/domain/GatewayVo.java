package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-03-24.
 */
public class GatewayVo {
    private String gatewayIp;
    private String ssid;

    public GatewayVo(String gatewayIp, String ssid){
        this.gatewayIp = gatewayIp;
        this.ssid = ssid;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
