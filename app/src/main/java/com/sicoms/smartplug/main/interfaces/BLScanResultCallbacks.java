package com.sicoms.smartplug.main.interfaces;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public interface BLScanResultCallbacks {
        void onBLScanResult(ArrayList<BluetoothDevice> devices);
    }