package com.sicoms.smartplug.network.http;

/**
 * Created by gudnam on 2015. 6. 9..
 */
public class ContextPathStore {

    // Login
    public static final String CLOUD_MEMBERSHIP_INSERT = "/PlugRestApp/restful/membership/add";
    public static final int REQUEST_MEMBERSHIP_INSERT = 11;
    public static final String CLOUD_AUTH_LOGIN = "/PlugRestApp/restful/auth";
    public static final int REQUEST_AUTH_LOGIN = 12;
    public static final String CLOUD_AUTH_LOGOUT = "/PlugRestApp/restful/logout";
    public static final int REQUEST_AUTH_LOGOUT = 13;
    public static final String CLOUD_UPDATE_MEMBERSHIP_PROFILE = "/PlugRestApp/restful/membership/mod/profile";
    public static final int REQUEST_UPDATE_MEMBERSHIP_PROFILE = 14;
    public static final String CLOUD_UPDATE_MEMBERSHIP_PASSWORD = "/PlugRestApp/restful/membership/mod/password";
    public static final int REQUEST_UPDATE_MEMBERSHIP_PASSWORD = 15;
    public static final String CLOUD_UPDATE_MEMBERSHIP_NAME = "/PlugRestApp/restful/membership/mod/name";
    public static final int REQUEST_UPDATE_MEMBERSHIP_NAME = 16;

    // Place
    public static final String CLOUD_SELECT_PLACE_LIST = "/PlugRestApp/restful/place/getAll";
    public static final int REQUEST_SELECT_PLACE_LIST = 21;
    public static final String CLOUD_INSERT_PLACE = "/PlugRestApp/restful/place/add";
    public static final int REQUEST_INSERT_PLACE = 22;
    public static final String CLOUD_UPDATE_PLACE_ = "/PlugRestApp/restful/place/mod";
    public static final int REQUEST_UPDATE_PLACE = 23;
    public static final String CLOUD_OUT_PLACE = "/PlugRestApp/restful/place/out";
    public static final int REQUEST_OUT_PLACE = 24;
    public static final String CLOUD_UPDATE_PLACE_BL_PASSWORD = "/PlugRestApp/restful/place/setting/mod/bl_password";
    public static final int REQUEST_UPDATE_PLACE_BL_PASSWORD = 25;
    public static final String CLOUD_SELECT_PLACE_BL_PASSWORD = "/PlugRestApp/restful/place/setting/get/bl_password";
    public static final int REQUEST_SELECT_PLACE_BL_PASSWORD = 26;

    // User
    public static final String CLOUD_SELECT_USER_LIST = "/PlugRestApp/restful/user/getAll";
    public static final int REQUEST_SELECT_USER_LIST = 32;
    public static final String CLOUD_INSERT_USER = "/PlugRestApp/restful/user/add";
    public static final int REQUEST_INSERT_USER = 33;
    public static final String CLOUD_UPDATE_USER = "/PlugRestApp/restful/user/mod";
    public static final int REQUEST_UPDATE_USER = 34;
    public static final String CLOUD_DELETE_USER = "/PlugRestApp/restful/user/removeUser";
    public static final int REQUEST_DELETE_USER = 35;

    // Plug
    public static final String CLOUD_INSERT_PLUG = "/PlugRestApp/restful/plug/add";
    public static final int REQUEST_INSERT_PLUG = 41;
    public static final String CLOUD_UPDATE_PLUG = "/PlugRestApp/restful/plug/mod";
    public static final int REQUEST_UPDATE_PLUG = 42;
    public static final String CLOUD_DELETE_PLUG = "/PlugRestApp/restful/plug/remove";
    public static final int REQUEST_DELETE_PLUG = 43;
    public static final String CLOUD_SELECT_PLUG_LIST = "/PlugRestApp/restful/plug/getAll";
    public static final int REQUEST_SELECT_PLUG_LIST = 44;
    public static final String CLOUD_INSERT_BLUETOOTH = "/PlugRestApp/restful/plug/bluetooth/add";
    public static final int REQUEST_INSERT_BLUETOOTH = 45;
    public static final String CLOUD_SELECT_BLUETOOTH_LIST = "/PlugRestApp/restful/plug/bluetooth/getList";
    public static final int REQUEST_SELECT_BLUETOOTH_LIST = 46;

    // Group
    public static final String CLOUD_INSERT_GROUP = "/PlugRestApp/restful/group/add";
    public static final int REQUEST_INSERT_GROUP = 51;
    public static final String CLOUD_UPDATE_GROUP = "/PlugRestApp/restful/group/mod";
    public static final int REQUEST_UPDATE_GROUP = 52;
    public static final String CLOUD_SELECT_GROUP_LIST = "/PlugRestApp/restful/group/getAll";
    public static final int REQUEST_SELECT_GROUP_LIST = 53;
    public static final String CLOUD_DELETE_GROUP_LIST = "/PlugRestApp/restful/group/removeList";
    public static final int REQUEST_DELETE_GROUP_LIST = 54;
    public static final String CLOUD_DELETE_GROUP_PLUG = "/PlugRestApp/restful/group/plug/remove";
    public static final int REQUEST_DELETE_GROUP_PLUG = 55;
    public static final String CLOUD_INSERT_GROUP_PLUG = "/PlugRestApp/restful/group/plug/add";
    public static final int REQUEST_INSERT_GROUP_PLUG = 57;
    public static final String CLOUD_INSERT_GROUP_USER = "/PlugRestApp/restful/group/user/add";
    public static final int REQUEST_INSERT_GROUP_USER = 58;
    public static final String CLOUD_UPDATE_GROUP_USER = "/PlugRestApp/restful/group/user/mod";
    public static final int REQUEST_UPDATE_GROUP_USER = 59;

    // Common
    public static final String CLOUD_COMMON_IMAGE_DOWNLOAD = "/PlugRestApp/restful/common/downloadImg";
    public static final int REQUEST_DOWNLOAD_COMMON_IMAGE = 91;
    public static final String CLOUD_COMMON_IMAGE_UPLOAD = "/PlugRestApp/restful/common/uploadImg";
    public static final int REQUEST_UPLOAD_COMMON_IMAGE = 92;
}
