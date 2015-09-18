package com.sicoms.smartplug.domain;

/**
 * Created by wizardkyn on 2015. 3. 6..
 */
public class PlugWeekdayVo {
    private boolean isChedked;
    private String weekdayName;

    public PlugWeekdayVo(boolean isChedked, String weekdayName) {
        this.isChedked = isChedked;
        this.weekdayName = weekdayName;
    }

    public boolean isChedked() {
        return isChedked;
    }

    public void setChedked(boolean isChedked) {
        this.isChedked = isChedked;
    }

    public String getWeekdayName() {
        return weekdayName;
    }

    public void setWeekdayName(String weekdayName) {
        this.weekdayName = weekdayName;
    }
}
