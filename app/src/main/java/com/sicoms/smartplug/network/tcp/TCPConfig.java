package com.sicoms.smartplug.network.tcp;

/**
 * Created by pc-11-user on 2015-03-26.
 */
public class TCPConfig {
    public static final String TCP_IP = "192.168.8.89";//"10.0.0.52";
    public static final int TCP_Port = 5001;

    public static int TCP_SUCCESS = 1;
    public static int TCP_CONNECT_FAIL = -1;
    public static int TCP_REQUEST_FAIL = -2;
    public static int TCP_RESPONSE_FAIL = -3;

    public static String TCP_RESPONSE_OK = "OK";
}
