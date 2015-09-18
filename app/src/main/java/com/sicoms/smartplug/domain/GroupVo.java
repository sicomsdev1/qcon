package com.sicoms.smartplug.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 29..
 */
public class GroupVo {
    private String groupId = "";
    private String groupIconImg = "";
    private String groupName = "";
    private String usage = "";
    private boolean isOn = false;
    private List<PlugVo> plugVoList = null;
    private List<UserVo> userVoList = null;
    private boolean isCheck = false;

    public GroupVo(){}
    public GroupVo(String groupId, String groupIconImg, String groupName, String usage, boolean isOn){
        this.groupId = groupId;
        this.groupIconImg = groupIconImg;
        this.groupName = groupName;
        this.usage = usage;
        this.isOn = isOn;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupNum() {
        return groupId;
    }

    public void setGroupNum(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupIconImg() {
        return groupIconImg;
    }

    public void setGroupIconImg(String groupIconImg) {
        this.groupIconImg = groupIconImg;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setIsOnOff(boolean isOnOff) {
        this.isOn = isOnOff;
    }

    public List<PlugVo> getPlugVoList() {
        return plugVoList;
    }

    public void setPlugVoList(List<PlugVo> plugVoList) {
        this.plugVoList = plugVoList;
    }

    public List<UserVo> getUserVoList() {
        return userVoList;
    }

    public void setUserVoList(List<UserVo> userVoList) {
        this.userVoList = userVoList;
    }

    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
}
