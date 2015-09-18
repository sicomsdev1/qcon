package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 9. 2..
 */
public class PlaceSettingVo {
    private String placeId;
    private String setId;
    private String setVal;

    public PlaceSettingVo(String placeId, String setId, String setVal){
        this.placeId = placeId;
        this.setId = setId;
        this.setVal = setVal;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getSetVal() {
        return setVal;
    }

    public void setSetVal(String setVal) {
        this.setVal = setVal;
    }
}
