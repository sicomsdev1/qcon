package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class PlugGroupVo {
    private String plugId;
    private String plugName;
    private String plugValue;
    private int statusIconImg;
    private String status;
    private int powerIconImg;
    private int stopwatchIconImg;

    public PlugGroupVo(String plugId, String plugName, String plugValue, int statusIconImg, String status){
        this.plugId = plugId;
        this.plugName = plugName;
        this.plugValue = plugValue;
        this.statusIconImg = statusIconImg;
        this.status = status;
    }

    public String getPlugId() {
        return plugId;
    }

    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

    public PlugGroupVo() {}

    public String getPlugName() {
        return plugName;
    }

    public void setPlugName(String plugName) {
        this.plugName = plugName;
    }

    public String getPlugValue() {
        return plugValue;
    }

    public void setPlugValue(String plugValue) {
        this.plugValue = plugValue;
    }

    public int getStatusIconImg() {
        return statusIconImg;
    }

    public void setStatusIconImg(int statusIconImg) {
        this.statusIconImg = statusIconImg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPowerIconImg() {
        return powerIconImg;
    }

    public void setPowerIconImg(int powerIconImg) {
        this.powerIconImg = powerIconImg;
    }

    public int getStopwatchIconImg() {
        return stopwatchIconImg;
    }

    public void setStopwatchIconImg(int stopwatchIconImg) {
        this.stopwatchIconImg = stopwatchIconImg;
    }
}
