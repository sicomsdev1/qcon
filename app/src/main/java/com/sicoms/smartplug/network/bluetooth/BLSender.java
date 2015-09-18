package com.sicoms.smartplug.network.bluetooth;

import android.app.Activity;

import com.sicoms.smartplug.network.bluetooth.util.DeviceController;

/**
 * Created by pc-11-user on 2015-04-10.
 */
public class BLSender {
    private static final String TAG = "BLSender";

    private DeviceController mDeviceController;

    public BLSender(Activity activity){
        try {
            mDeviceController = (DeviceController) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController callback interface.");
        }
    }
    public void sendData(String requestMessage){
        String data = requestMessage;
        data = data.replaceAll("\\s{1,}+", "");

        byte [] output = new byte[(data.length() + 1) / 2];
        int i = 0;
        int idx = 0;


        for ( i = 0; i < data.length(); ++i ) {
            if ( (i % 2) != 0 ) {
                try {
                    output[idx] = (byte)(Integer.parseInt(data.substring(i-1, i+1), 16) & 0xFF);
                } catch ( NumberFormatException e ) {
                    // insert 0 here as this is an invalid character
                    output[idx] = 0;
                }
                ++idx;
            }
        }

        if ( (i % 2) != 0 ) {
    			/* We must have missed out the last character, as there are odd number of characters */
            try {
                output[idx] = Byte.parseByte(data.substring(i-1,  i), 16);
            } catch ( NumberFormatException e ) {
                // insert 0 here as this is an invalid character
                output[idx] = 0;
            }
            ++idx;
        }

        //mDeviceController.sendData(output, false);
    }
}
