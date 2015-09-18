package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 5. 27..
 */
public class PlugVo {
    private String plugName = "";
    private String plugId = "";
    private String networkType = "";
    private String bssid = "";
    private String routerIp = "";
    private String gatewayIp = "";
    private String uuid = "";
    private String plugIconImg = "";
    private boolean isOn = false;
    private boolean isCheck = false;
    private String wh = "0";
    private String w = "0";

    public PlugVo(){}
    public PlugVo(String plugName, String plugId, String networkType, String plugIconImg, boolean isOn){
        this.plugName = plugName;
        this.plugId = plugId;
        this.networkType = networkType;
        this.plugIconImg = plugIconImg;
        this.isOn = isOn;
    }

    public String getPlugName() {
        return plugName;
    }

    public void setPlugName(String plugName) {
        this.plugName = plugName;
    }

    public String getPlugIconImg() {
        return plugIconImg;
    }

    public void setPlugIconImg(String plugIconImg) {
        this.plugIconImg = plugIconImg;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
    }

    public String getPlugId() {
        return plugId;
    }

    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getRouterIp() {
        return routerIp;
    }

    public void setRouterIp(String routerIp) {
        this.routerIp = routerIp;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public String getWh() {
        return wh;
    }

    public void setWh(String wh) {
        this.wh = wh;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }
}
