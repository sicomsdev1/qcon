package com.sicoms.smartplug.network.http;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public class HttpConfig {

//    public static final String PROTOCOL = "https://";
//    public static final String CLOUD_HTTP_IP = "sicoms.co.kr"; // Cloud Server
//    public static final String CLOUD_HTTP_PORT = "8543"; // HTTPS
    public static final String PROTOCOL = "http://";
    public static final String CLOUD_HTTP_IP = "61.74.63.132"; // 사내 Server
    public static final String CLOUD_HTTP_PORT = "8180"; // HTTP

    public static final int HTTP_TIMEOUT = 30 * 1000;
    public static final String HTTP_CONTEXT_PATH = "/szc/index.szc/open";
    public static final String HTTP_PORT = "8801";
    public static final int HTTP_SUCCESS = 1;
    public static final int HTTP_FAIL = -1;
    public static final String HTTP_RESULT_TRUE = "1";
    public static final String HTTP_RESULT_FALSE = "0";

    public static final int TYPE_POST = 1;
    public static final int TYPE_POST_SCHEDULE = 2;
    public static final int TYPE_GET = 3;

    public static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON_= "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // Point List
    public static final String POINT_LIST_MSG = "view";
    public static final String POINT_LIST_CMD = "get_points";
    public static final String POINT_LIST_TR = "123456789";

    // Node List
    public static final String NODE_LIST_MSG = "view";
    public static final String NODE_LIST_CMD = "get_nodes";
    public static final String NODE_LIST_TR = "sdfasdf2a";

    // Control On/Off
    public static final int CONTROL_SUCCESS = 0;
    public static final String CONTROL_ON_OFF_MSG = "ctrl";
    public static final String CONTROL_ON_OFF_CMD = "set_onoff";
    public static final String CONTROL_ON_OFF_TR = "123456789";
    public static final String CONTROL_ON_OFF_DP_P = "ha";
    public static final String CONTROL_ON_OFF_DP_C = "0x0372";
    public static final String CONTROL_ON_OFF_DP_S_ON = "1"; // 0n : 1 / Off : 0
    public static final String CONTROL_ON_OFF_DP_S_OFF = "0"; // 0n : 1 / Off : 0
    public static final String CONTROL_ON_OFF_DP_L = "7";
    
    // Wifi Mode
    public static final int WIFIMODE_SUCCESS = 0;
    public static final String WIFIMODE_MSG = "model";
    public static final String WIFIMODE_CMD = "set_wifi_mode";
    public static final String WIFIMODE_TR = "123456789";

    // Schedule
    public static final String SCHEDULE_MSG = "model";
    public static final String SCHEDULE_CMD = "set_schedule";

    // Standby Power
    public static final String CUTOFF_MSG = "model";
    public static final String CUTOFF_CMD = "set_standby_power";
}