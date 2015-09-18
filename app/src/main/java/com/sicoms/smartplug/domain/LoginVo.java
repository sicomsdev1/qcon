package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 6. 5..
 */
public class LoginVo {
    private String userId;
    private String userName;
    private String password;
    private String profileImg;

    public LoginVo(){}
    public LoginVo(String userId, String userName, String password, String profileImg){
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.profileImg = profileImg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
