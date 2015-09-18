package com.sicoms.smartplug.domain;

public class NavigationDrawerVo {
    private int groupSeq;
	private String groupName;

    public NavigationDrawerVo() {}

    public int getGroupSeq() {
        return groupSeq;
    }

    public void setGroupSeq(int groupSeq) {
        this.groupSeq = groupSeq;
    }

    public NavigationDrawerVo(int groupSeq, String groupName) {
        this.groupSeq = groupSeq;
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
