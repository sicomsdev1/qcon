package com.sicoms.smartplug.common;

import android.os.Environment;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class SPConfig {

    public static String SP_GCM_ID = "";

    public static final boolean IS_TEST = false;
    public static final boolean IS_DEBUG = true;

    public static boolean IS_FIRST = false;
    public static boolean IS_SKIP = false;

    public static final String AP_IP = "192.168.12.105";
    public static final int AP_PORT = 6070;

    public static final String SHOP_WEB = "http://www.sicoms.co.kr/ShopWebApp/web/shop/product.do";

    public static final String STATUS_ON = "1";
    public static final String STATUS_OFF = "0";

    public static final String ON_TEXT = "On";
    public static final String OFF_TEXT = "Off";

    public static final int MENU_GROUP_MEMBER = 0;
    public static final int MENU_SMART_PLUG = 1;
    public static final int MENU_SMART_SOCKET = 2;
    public static final int MENU_SMART_BULBS = 3;
    public static final int MENU_SMART_SWITCH = 4;

    public static final int TIME_TYPE_NONE = 0;
    public static final int TIME_TYPE_MORNING = 1;
    public static final int TIME_TYPE_AFTERNOON = 2;
    public static final int TIME_TYPE_ONEDAY = 3;

    public static final String PLUG_TYPE_WIFI_AP = "B_A"; // AP Mode
    public static final String PLUG_TYPE_WIFI_ROUTER = "B_A_S"; // Station Mode
    public static final String PLUG_TYPE_WIFI_GW = "B_G"; // Gateway Mode
    public static final String PLUG_TYPE_BLUETOOTH = "C"; // Bluetooth Mode

    public static final String NODE_TYPE_P = "p"; // Station Mode;

    public static final String PLUG_SSID_WIFI_NAME = "";
    public static final String PLUG_SSID_WIFI_GW_NAME = "sicoms"; // 임시
    public static final String PLUG_SSID_WIFI_AP_NAME = "sp_"; // 임시

    public static final String PLUG_AP_PASSWORD = "12345678";
    public static final String PLUG_AP_SECURITY_TYPE = "AP";

    public static final int REQUEST_PICTURE = 1;
    public static final int REQUEST_CROP_PICTURE = 2;
    public static final int REQUEST_CAMERA = 3;

    public static final int MEMBER_MASTER = 0;
    public static final int MEMBER_USER = 1;
    public static final int MEMBER_SETTER = 2;
    public static final String MEMBER_MASTER_NAME = "관리자";
    public static final String MEMBER_SETTER_NAME = "플러그 관리자";
    public static final String MEMBER_USER_NAME = "사용자";

    public static final int MEMBER_ON = 1;
    public static final int MEMBER_OFF = 0;

    public static final int MODE_NORMAL = 1;
    public static final int MODE_CHECK = 2;

    public static final String PLACE_IMAGE_NAME = "plc";
    public static final String PLACE_DEFAULT_IMAGE_NAME = "dpbg";

    public static final String USER_IMAGE_NAME = "uprof";
    public static final String USER_DEFAULT_IMAGE_NAME = "profile_default";

    public static final String PLUG_IMAGE_NAME = "pprof";
    public static final String PLUG_DEFAULT_IMAGE_NAME = "dppbg";

    public static final String GROUP_IMAGE_NAME = "gprof";
    public static final String GROUP_DEFAULT_IMAGE_NAME = "dgpbg";

    public static String FILE_PATH = "/Sicoms/";

    public static final String AM = "오전";
    public static final String PM = "오후";


    public static int DIALOG_TIMEOUT = 30 * 1000;

    public static final int PLUG_EDIT_PAGE01 = 1;
    public static final int PLUG_EDIT_PAGE02 = 2;
    public static final int PLUG_EDIT_PAGE03 = 3;

    public static final int PLUG_MAIN_OPEN = 1;
    public static final int PLUG_MAIN_CLOSE = 0;

    public static final int PICTURE_MENU_ALBUM = 1;
    public static final int PICTURE_MENU_CAMERA = 2;
    public static final int PICTURE_MENU_DEFAULT_IMAGE = 3;

    public static final int PICTURE_MENU_TYPE_HOME = 10;
    public static final int PICTURE_MENU_TYPE_PLACE = 11;
    public static final int PICTURE_MENU_TYPE_GROUP = 12;
    public static final int PICTURE_MENU_TYPE_PLUG = 13;

    public static final String NO_SCHEDULE = "25:00";
    public static final String NO_CUTOFF = "0";

    public static String CURRENT_PLACE_BL_PASSWORD = "1";
    public static final String PLACE_SETTING_BL_PASSWORD = "bl_password";

    public static final int SYNC_INTERVAL = 5 * 60; // 5분 주기로 동기화

    public static final String AES_KEY = "QCONLOCATIONAESKEY20150907";

    public static final int BLUR_RADIUS = 20;
}
