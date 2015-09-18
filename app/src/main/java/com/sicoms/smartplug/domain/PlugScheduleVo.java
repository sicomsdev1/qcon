package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class PlugScheduleVo {

    private int firstDayType; // 0 : 없음, 1 : 오전, 2 : 오후, 3 : 하루
    private int secondDayType; // 0 : 없음, 1 : 오전, 2 : 오후, 3 : 하루
    private String firstTime;
    private String secondTime;
    private String dayofweek;
    private String status;
    private String plugId;


    public PlugScheduleVo(String plugId,int firstDayType, int secondDayType, String firstTime, String secondTime, String dayofweek, String status){
        this.plugId = plugId;
        this.firstDayType = firstDayType;
        this.secondDayType = secondDayType;
        this.firstTime = firstTime;
        this.secondTime = secondTime;
        this.dayofweek = dayofweek;
        this.status = status;
    }
    public PlugScheduleVo() {};

    public int getFirstDayType() {
        return firstDayType;
    }

    public void setFirstDayType(int firstDayType) {
        this.firstDayType = firstDayType;
    }

    public int getSecondDayType() {
        return secondDayType;
    }

    public void setSecondDayType(int secondDayType) {
        this.secondDayType = secondDayType;
    }

    public String getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(String firstTime) {
        this.firstTime = firstTime;
    }

    public String getSecondTime() {
        return secondTime;
    }

    public void setSecondTime(String secondTime) {
        this.secondTime = secondTime;
    }

    public String getDayofweek() {
        return dayofweek;
    }

    public void setDayofweek(String dayofweek) {
        this.dayofweek = dayofweek;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlugId() {
        return plugId;
    }

    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

}
