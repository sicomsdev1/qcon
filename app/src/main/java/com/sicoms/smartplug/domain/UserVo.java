package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 5. 27..
 */
public class UserVo {
    private String userId = "";
    private String password = "";
    private String userName = "";
    private int auth = 1; // 기본은 User 권한, 내가 아닌 멤버들의 권한
    private String userProfileImg = "";
    private boolean isOn = true;
    private boolean isCheck = false;
    private String gcmId = "";

    public UserVo(){}
    public UserVo(String userId, String password){
        this.userId = userId;
        this.password = password;
    }

    // Phone
    public UserVo(String userId, String password, String userName, int auth, String userProfileImg, boolean isOn){
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.auth = auth;
        this.userProfileImg = userProfileImg;
        this.isOn = isOn;
    }

    // Member
    public UserVo(String userId, String userName, int auth, String userProfileImg, boolean isOn){
        this.userId = userId;
        this.userName = userName;
        this.auth = auth;
        this.userProfileImg = userProfileImg;
        this.isOn = isOn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAuth() {
        return auth;
    }

    public void setAuth(int auth) {
        this.auth = auth;
    }

    public String getUserProfileImg() {
        return userProfileImg;
    }

    public void setUserProfileImg(String userProfileImg) {
        this.userProfileImg = userProfileImg;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setIsOnOff(boolean isOnOff) {
        this.isOn = isOnOff;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }
}
