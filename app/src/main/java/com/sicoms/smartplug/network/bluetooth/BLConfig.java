package com.sicoms.smartplug.network.bluetooth;

/**
 * Created by gudnam on 2015. 6. 12..
 */
public class BLConfig {

    public static final String DEVICE_NAME = "SmartPlug";
    public static final String BL_DEFAULT_SECURITY_PASSWORD = "1";

    public static final String DATA_REQUEST_NUM = "01";
    public static final String DATA_RESPONSE_NUM = "02";
    public static final String SCHEDULE_REQUEST_NUM = "03";
    public static final String SCHEDULE_RESPONSE_NUM = "04";
    public static final String CUTOFF_REQUEST_NUM = "05";
    public static final String CUTOFF_RESPONSE_NUM = "06";
    public static final String VA_REQUEST_NUM = "07";
    public static final String VA_RESPONSE_NUM = "08";
    public static final String ASSOCIATION_REQUEST_NUM = "09";
    public static final String ASSOCIATION_RESPONSE_NUM = "0A";
    public static final String DEVICE_ID_REQUEST_NUM = "0B";
    public static final String DEVICE_ID_RESPONSE_NUM = "0C";
    public static final String LED_CONTROL_REQUEST_NUM = "0D";
    public static final String GET_SCHEDULE_REQUEST_NUM = "11";
    public static final String GET_SCHEDULE_RESPONSE_NUM = "12";
    public static final String GET_CUTOFF_REQUEST_NUM = "13";
    public static final String GET_CUTOFF_RESPONSE_NUM = "14";

    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;

    public static final int MAX_GROUP_COUNT = 4;
}
