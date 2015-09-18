package com.sicoms.smartplug.domain;

import com.sicoms.smartplug.common.SPConfig;

/**
 * Created by gudnam on 2015. 6. 3..
 */
public class ScheduleVo {
    private long schSeq = 0;
    private String startAmPm = SPConfig.AM;
    private String endAmPm = SPConfig.AM;
    private String startTime = "01:00";
    private String endTime = "01:00";
    private boolean isStartOn = false;
    private boolean isEndOn = false;

    public ScheduleVo(){}
    public ScheduleVo(String startAmPm, String endAmPm, String startTime, String endTime, boolean isStartOn, boolean isEndOn){
        this.startAmPm = startAmPm;
        this.endAmPm = endAmPm;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isStartOn = isStartOn;
        this.isEndOn = isEndOn;
    }
    public ScheduleVo(long schSeq, String startAmPm, String endAmPm, String startTime, String endTime, boolean isStartOn, boolean isEndOn){
        this.schSeq = schSeq;
        this.startAmPm = startAmPm;
        this.endAmPm = endAmPm;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isStartOn = isStartOn;
        this.isEndOn = isEndOn;
    }

    public long getSchSeq() {
        return schSeq;
    }

    public void setSchSeq(long schSeq) {
        this.schSeq = schSeq;
    }

    public String getStartAmPm() {
        return startAmPm;
    }

    public void setStartAmPm(String startAmPm) {
        this.startAmPm = startAmPm;
    }

    public String getEndAmPm() {
        return endAmPm;
    }

    public void setEndAmPm(String endAmPm) {
        this.endAmPm = endAmPm;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isStartOn() {
        return isStartOn;
    }

    public void setIsStartOn(boolean isStartOn) {
        this.isStartOn = isStartOn;
    }

    public boolean isEndOn() {
        return isEndOn;
    }

    public void setIsEndOn(boolean isEndOn) {
        this.isEndOn = isEndOn;
    }
}
