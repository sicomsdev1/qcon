package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 7. 14..
 */
public class UserPlaceMappingVo {

    private UserVo userVo;
    private PlaceVo placeVo;

    public UserPlaceMappingVo(){}
    public UserPlaceMappingVo(UserVo userVo, PlaceVo placeVo){
        this.userVo = userVo;
        this.placeVo = placeVo;
    }

    public UserVo getUserVo() {
        return userVo;
    }

    public void setUserVo(UserVo userVo) {
        this.userVo = userVo;
    }

    public PlaceVo getPlaceVo() {
        return placeVo;
    }

    public void setPlaceVo(PlaceVo placeVo) {
        this.placeVo = placeVo;
    }
}
