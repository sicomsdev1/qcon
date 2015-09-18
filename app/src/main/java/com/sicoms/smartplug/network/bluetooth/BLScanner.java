/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2014
 *
 *  This software is provided to the customer for evaluation
 *  purposes only and, as such early feedback on performance and operation
 *  is anticipated. The software source code is subject to change and
 *  not intended for production. Use of developmental release software is
 *  at the user's own risk. This software is provided "as is," and CSR
 *  cautions users to determine for themselves the suitability of using the
 *  beta release version of this software. CSR makes no warranty or
 *  representation whatsoever of merchantability or fitness of the product
 *  for any particular purpose or use. In no event shall CSR be liable for
 *  any consequential, incidental or special damages whatsoever arising out
 *  of the use of or inability to use this software, even if the user has
 *  advised CSR of the possibility of such damages.
 *
 ******************************************************************************/

package com.sicoms.smartplug.network.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.main.interfaces.BLIsEnabledCallbacks;
import com.sicoms.smartplug.main.interfaces.BLScanResultCallbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Activity used to scan for bridge devices. Show the results in a list. Automatically select the devices with the
 * smallest RSSI and pass them to the MainActivity for connection.
 */
public class BLScanner {

    public static final int REQUEST_ENABLE_BT = 1;
    private static final int CONNECT_RETRIES = 3;
    private static final int CONNECT_WAIT_TIME_MS = (10 * 1000);

    // Adjust this value to control how long scan should last for. Higher values will drain the battery more.
    // Adjust this value in the derived class.
    protected long mScanPeriodMillis = 3000;

    private Activity mActivity;

    private static ArrayList<ScanInfo> mScanResults = new ArrayList<ScanInfo>();

    private static HashSet<String> mScanAddreses = new HashSet<String>();

    private BluetoothAdapter mBtAdapter = null;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mCheckBt = false;

    private static final int INDEX_UUID_1 = 5;
    private static final int INDEX_UUID_2 = 6;
    private static final byte UUID_1 = (byte)0xF1;
    private static final byte UUID_2 = (byte)0xFE;

    // number of connection
    private int scanAttempts = 0;

    private BLScanResultCallbacks mScanResultCallbacks;
    private BLIsEnabledCallbacks mBLIsEnabledCallbacks;

    /**
     * Handle Bluetooth connection when the user selects a device.
     *
     *            The Bluetooth device selected by the user.
     */
    protected void onConnectedBluetooth(BluetoothDevice xdeviceToConnect) {
        // Try top 3 devices
        ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        for (int i = 0; i < mScanResults.size() && i < 3; i++) {
        	ScanInfo info = mScanResults.get(i);
        	devices.add(mBtAdapter.getRemoteDevice(info.address));
        }
        mScanResultCallbacks.onBLScanResult(devices);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BLScanner(Context context) {
        mActivity = (Activity) context;
        //mScanListView = (ListView) this.findViewById(R.id.scanListView);
        //mScanResultsAdapter = new ScanResultsAdapter(mActivity, mScanResults);
        //mScanListView.setAdapter(mScanResultsAdapter);

        final BluetoothManager btManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();

//        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
        // Register for broadcasts on BluetoothAdapter state change so that we can tell if it has been turned off.
//        checkEnableBt();
    }

    public void scanBluetoothDevice() {
        if( mBtAdapter != null) {
            if (mBtAdapter.isEnabled()) {
                scanLeDevice(true);
            }
        }
    }

    public void stopScanBluetoothDevice(){
        scanLeDevice(false);
    }

    public BroadcastReceiver getBluetoothReceiver(){
        return mReceiver;
    }
    /**
     *  Display a dialogue requesting Bluetooth to be enabled if it isn't already.
     */
//    private void checkEnableBt() {
//        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
//    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    //Toast.makeText(context, mActivity.getString(R.string.bluetooth_disabled), Toast.LENGTH_LONG).show();
                    scanLeDevice(false);
                    clearScanResults();
                    mBLIsEnabledCallbacks.onIsEnablbled(false);
                }
                else if (state == BluetoothAdapter.STATE_ON) {
                    //Toast.makeText(context, mActivity.getString(R.string.bluetooth_enabled), Toast.LENGTH_LONG).show();
                    mBtAdapter = ((BluetoothManager)mActivity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                    mBLIsEnabledCallbacks.onIsEnablbled(true);
                }
            }
        }
    };

	/**
     * Clear the cached scan results, and update the display.
     */
    private void clearScanResults() {
        mScanResults.clear();
        mScanAddreses.clear();
    }

    private Runnable scanTimeout = new Runnable() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            mBtAdapter.stopLeScan(mLeScanCallback);
            // Connect to the device with the smallest RSSI value
            if (!mScanResults.isEmpty()) {
                ScanInfo info = (ScanInfo) mScanResults.get(0);
                BluetoothDevice deviceToConnect = mBtAdapter.getRemoteDevice(info.address);
                onConnectedBluetooth(deviceToConnect);
            }
        }
    };

    /**
     * Start or stop scanning. Only scan for a limited amount of time defined by SCAN_PERIOD.
     *
     * @param enable
     *            Set to true to enable scanning, false to stop.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a predefined scan period.
            mHandler.postDelayed(scanTimeout, mScanPeriodMillis);
            clearScanResults();
            mActivity.setProgressBarIndeterminateVisibility(true);
            mBtAdapter.startLeScan(mLeScanCallback);
        }
        else {
            // Cancel the scan timeout callback if still active or else it may fire later.
            mHandler.removeCallbacks(scanTimeout);
            mActivity.setProgressBarIndeterminateVisibility(false);
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * Callback for scan results.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            mActivity.runOnUiThread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    if( device.getName() == null){
                        return;
                    }
                    if( device.getName().contains(BLConfig.DEVICE_NAME)){
                        if (!mScanAddreses.contains(device.getAddress())) {
                            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE &&
                                    scanRecord[INDEX_UUID_1] == UUID_1 && scanRecord[INDEX_UUID_2] == UUID_2) {
                                ScanInfo scanResult = new ScanInfo(device.getName(), device.getAddress(), rssi);
                                mScanAddreses.add(device.getAddress());
                                mScanResults.add(scanResult);
                            }
                        } else {
                            for (ScanInfo info : mScanResults) {
                                if (info.address.equalsIgnoreCase((device.getAddress()))) {
                                    info.rssi = rssi;
                                    Collections.sort(mScanResults);
                                    break;
                                }
                            }
                        }
                    } else {
                        mScanResultCallbacks.onBLScanResult(null);
                    }
                }
            });
        }
    };

    public boolean isEnableBluetooth(){
        return mBtAdapter.isEnabled();
    }

    public void setOnBLScanResultCallbacks(final BLScanResultCallbacks callbacks){
        mScanResultCallbacks = callbacks;
    }
    public void setOnBluetoothIsEnabledCallbacks(final BLIsEnabledCallbacks callbacks){
        mBLIsEnabledCallbacks = callbacks;
    }

    public class ScanInfo implements Comparable<ScanInfo> {
        public String name;
        public String address;
        public int rssi;

        public ScanInfo(String name, String address, int rssi) {
            this.name = name;
            this.address = address;
            this.rssi = rssi;
        }

		@Override
		public int compareTo(ScanInfo another) {
			final int BEFORE = -1;
		    final int EQUAL = 0;
		    final int AFTER = 1;

		    if (rssi == another.rssi) return EQUAL;
		    if (this.rssi < another.rssi) return AFTER;
		    if (this.rssi > another.rssi) return BEFORE;

		    return EQUAL;
		}
    }
}
