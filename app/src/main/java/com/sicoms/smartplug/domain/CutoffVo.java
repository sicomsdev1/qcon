package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class CutoffVo {
    private String power;
    private String time;
    private boolean isOn;

    public CutoffVo(){}
    public CutoffVo(String power, String time, boolean isOn){
        this.power = power;
        this.time = time;
        this.isOn = isOn;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
    }
}
