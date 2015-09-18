package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class CutoffVo {
    private String power;
    private String min;
    private boolean isOn;

    public CutoffVo(){}
    public CutoffVo(String power, String min, boolean isOn){
        this.power = power;
        this.min = min;
        this.isOn = isOn;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
    }
}
