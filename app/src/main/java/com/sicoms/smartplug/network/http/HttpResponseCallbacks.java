package com.sicoms.smartplug.network.http;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public interface HttpResponseCallbacks {
    void onHttpResponseResultStatus(int type, int result, String value);
}
