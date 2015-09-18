package com.sicoms.smartplug.network.http;

import android.graphics.Bitmap;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public interface HttpBitmapResponseCallbacks {
    void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap);
}
