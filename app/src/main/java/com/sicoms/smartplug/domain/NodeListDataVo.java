package com.sicoms.smartplug.domain;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public class NodeListDataVo {
    private String ND_CODE;
    private String RT_CODE;
    private String PROTOCOL;
    private String ND_NAME;
    private String SERIAL_NO;
    private String LOCATION;
    private String REG_DATE;
    private String STATUS;
    private String ZNODEID;

    public String getND_CODE() {
        return ND_CODE;
    }

    public void setND_CODE(String ND_CODE) {
        this.ND_CODE = ND_CODE;
    }

    public String getRT_CODE() {
        return RT_CODE;
    }

    public void setRT_CODE(String RT_CODE) {
        this.RT_CODE = RT_CODE;
    }

    public String getPROTOCOL() {
        return PROTOCOL;
    }

    public void setPROTOCOL(String PROTOCOL) {
        this.PROTOCOL = PROTOCOL;
    }

    public String getND_NAME() {
        return ND_NAME;
    }

    public void setND_NAME(String ND_NAME) {
        this.ND_NAME = ND_NAME;
    }

    public String getSERIAL_NO() {
        return SERIAL_NO;
    }

    public void setSERIAL_NO(String SERIAL_NO) {
        this.SERIAL_NO = SERIAL_NO;
    }

    public String getLOCATION() {
        return LOCATION;
    }

    public void setLOCATION(String LOCATION) {
        this.LOCATION = LOCATION;
    }

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getZNODEID() {
        return ZNODEID;
    }

    public void setZNODEID(String ZNODEID) {
        this.ZNODEID = ZNODEID;
    }
}
