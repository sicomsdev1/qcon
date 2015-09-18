package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 5. 27..
 */
public class HomeMenuVo {
    private int menuNum = 0;
    private String menuName = "";
    private int menuIconImg = 0;

    public HomeMenuVo(int menuNum, String menuName, int menuIconImg){
        this.menuNum = menuNum;

        this.menuName = menuName;
        this.menuIconImg = menuIconImg;
    }

    public int getMenuNum() {
        return menuNum;
    }

    public void setMenuNum(int menuNum) {
        this.menuNum = menuNum;
    }
    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getMenuIconImg() {
        return menuIconImg;
    }

    public void setMenuIconImg(int menuIconImg) {
        this.menuIconImg = menuIconImg;
    }
}
