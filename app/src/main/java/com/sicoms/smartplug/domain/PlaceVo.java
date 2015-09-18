package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 6. 1..
 */
public class PlaceVo {
    private String placeId = "";
    private String placeName = "";
    private String address = "";
    private String coordinate = "";
    private String placeImg = "";
    private String auth = ""; // 자신이 플레이스 안에서의 권한
    private boolean isHere = false;
    private int plugCount = 0;
    private int memberCount = 0;

    public PlaceVo(){}
    public PlaceVo(String placeId, String placeName, String address, String coordinate, String placeImg, String auth, boolean isHere){
        this.placeId = placeId;
        this.placeName = placeName;
        this.address = address;
        this.coordinate = coordinate;
        this.placeImg = placeImg;
        this.auth = auth;
        this.isHere = isHere;
    }
    public PlaceVo(String placeId, String placeName, String address, String coordinate, String placeImg, String auth, boolean isHere, int plugCount, int memberCount){
        this.placeId = placeId;
        this.placeName = placeName;
        this.address = address;
        this.coordinate = coordinate;
        this.placeImg = placeImg;
        this.auth = auth;
        this.isHere = isHere;
        this.plugCount = plugCount;
        this.memberCount = memberCount;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public boolean isHere() {
        return isHere;
    }

    public void setIsHere(boolean isHere) {
        this.isHere = isHere;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getPlaceImg() {
        return placeImg;
    }

    public void setPlaceImg(String placeImg) {
        this.placeImg = placeImg;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public int getPlugCount() {
        return plugCount;
    }

    public void setPlugCount(int plugCount) {
        this.plugCount = plugCount;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
