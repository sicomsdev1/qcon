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

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.csr.mesh.AssociationFailedException;
import com.csr.mesh.AttentionModelApi;
import com.csr.mesh.ConfigModelApi;
import com.csr.mesh.DataModelApi;
import com.csr.mesh.FirmwareModelApi;
import com.csr.mesh.GroupModelApi;
import com.csr.mesh.LightModelApi;
import com.csr.mesh.MeshService;
import com.csr.mesh.PowerModelApi;
import com.csr.mesh.SwitchModelApi;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.domain.BluetoothVo;
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.ScheduleVo;
import com.sicoms.smartplug.network.bluetooth.util.AssociationListener;
import com.sicoms.smartplug.network.bluetooth.util.AssociationStartedListener;
import com.sicoms.smartplug.network.bluetooth.util.DataListener;
import com.sicoms.smartplug.network.bluetooth.util.Device;
import com.sicoms.smartplug.network.bluetooth.util.DeviceController;
import com.sicoms.smartplug.network.bluetooth.util.DeviceInfoProtocol;
import com.sicoms.smartplug.network.bluetooth.util.DeviceState.StateType;
import com.sicoms.smartplug.network.bluetooth.util.DeviceStore;
import com.sicoms.smartplug.network.bluetooth.util.GroupDevice;
import com.sicoms.smartplug.network.bluetooth.util.GroupListener;
import com.sicoms.smartplug.network.bluetooth.util.InfoListener;
import com.sicoms.smartplug.network.bluetooth.util.LightState;
import com.sicoms.smartplug.network.bluetooth.util.PowState;
import com.sicoms.smartplug.network.bluetooth.util.RemovedListener;
import com.sicoms.smartplug.network.bluetooth.util.Setting;
import com.sicoms.smartplug.network.bluetooth.util.SimpleNavigationListener;
import com.sicoms.smartplug.network.bluetooth.util.SingleDevice;
import com.sicoms.smartplug.plug.interfaces.AssociatedDevicesResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.LastDataResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.ScheduleResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BluetoothManager implements DeviceController, AssociationService.BLAssociationResultCallbacks {
    private static final String TAG = BluetoothManager.class.getSimpleName();

    // How often to send a colour - i.e. how often the periodic timer fires.
    private static final int TRANSMIT_PERIOD_MS = 240;

    // Time to wait for group acks.
    private static final int GROUP_ACK_WAIT_TIME_MS = (30 * 1000);

    // Time to wait for device UUID after removing a device.
    private static final int REMOVE_ACK_WAIT_TIME_MS = (10 * 1000);

    // Time to wait until giving up on connection.
    private static final int CONNECT_WAIT_TIME_MS = (30 * 1000);
    
 // Time to wait showing the progress dialog.
    private static final int PROGRESS_DIALOG_TIME_MS = (10 * 1000);
    
    private static final int CONNECT_RETRIES = 3;
    
    // Watch for disconnect for this time period and reconnect.
    private static final long DISCONNECT_WATCH_TIME_MS = 500;
    
    private static final int DATA_BUFFER_SIZE = 200;
    
    private int mNumMeshConnectAttempts = 0;

    private long mConnectTime = 0;
    
    private boolean mConnected = false;
    
    private DeviceStore mDeviceStore;

    // The address to send packets to.
    private int mSendDeviceId = Device.DEVICE_ID_UNKNOWN;

    // The colour sent every time the periodic timer fires (if mNewColor is true).
    // This will be updated by calls to setLightColor.
    private int mColorToSend = Color.rgb(0, 0, 0);

    // A new colour is only sent every TRANSMIT_PERIOD_MS if this is true. Set to true by setLightColour.
    private boolean mNewColor = false;

    private int mGroupAcksWaiting = 0;
        
    private ArrayList<Integer> mNewGroups = new ArrayList<Integer>();
    private List<Integer> mGroupsToSend;
    
    private MeshService mService = null;

    // True if authorization should be used during key distribution.

    private int mRemovedUuidHash;
    private int mRemovedDeviceId;

    private ProgressDialog mProgress;

    private SimpleNavigationListener mNavListener;

    private byte [] mData = new byte[DATA_BUFFER_SIZE];
    
    private String mConnectedAddress;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    int mMeshDeviceIndex = 0;

    // Listeners
    private GroupListener mGroupAckListener;
    private InfoListener mInfoListener;
    private AssociationListener mAssListener;
    private RemovedListener mRemovedListener;
    private DataListener mDataListener;

    private AssociationService mAssociationService;
    private static BLResultCallbacks mBLResultCallbacks;
    private static LastDataResultCallbacks mLastDataResultCallbacks;
    private static AssociatedDevicesResultCallbacks mAssociatedCallbacks;
    private static ScheduleResultCallbacks mScheduleResultCallbacks;
    private static CutoffResultCallbacks mCutoffResultCallbacks;

    private static Context stContext;
    private static Activity stActivity;

    // A list of model ids that are waiting on a query being sent to find out how many groups are supported.
    private Queue<Integer> mModelsToQueryForGroups = new LinkedList<Integer>();

    private SparseIntArray mDeviceIdtoUuidHash = new SparseIntArray();
    private SparseArray<String> mUuidHashToAppearance = new SparseArray<String>();

    private boolean mGroupSuccess = true;

    // Keys used to save settings
    private static final String SETTING_LAST_ID = "lastID";

    byte[] vid;
    byte[] pid;
    byte[] version;

    public BluetoothManager(Activity activity) {
        stContext = activity;
        stActivity = activity;

        mDeviceStore = new DeviceStore(stActivity);
    }

    public void setDevices(ArrayList<BluetoothDevice> devices){
        mDevices = devices;
    }

    public void stopBluetooth(){
        if( mAssociationService != null) {
            mAssociationService.stop();
        }
    }

    public void disconnectBluetooth() {
        stopBluetooth();
        if( mService != null) {
            mService.disconnectBridge();
        }
//        if( mGattService != null){
//            mGattService.disconnect();
//        }
        mMeshHandler.removeCallbacksAndMessages(null);
        mConnected = false;
    }

    /**
     * Callbacks for changes to the state of the connection.
     */
    public ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((MeshService.LocalBinder) rawBinder).getService();
            if (mService != null) {
                mService.setContinuousLeScanEnabled(true);
                // try to get the last setting ID used.
                SharedPreferences activityPrefs = stActivity.getPreferences(Activity.MODE_PRIVATE);
                int lastIdUsed = activityPrefs.getInt(SETTING_LAST_ID, Setting.UKNOWN_ID);
                restoreSettings(lastIdUsed);

                // Start the required transport.
                mMeshHandler.postDelayed(connectTimeout, CONNECT_WAIT_TIME_MS);
                connect();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

//    public ServiceConnection mGattServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
//
//            mGattService = ((BluetoothLeService.LocalBinder) rawBinder).getService();
//            if (!mGattService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                return;
//            }
//            // Automatically connects to the device upon successful start-up initialization.
//            mMeshHandler.postDelayed(gattConnectTimeout, CONNECT_WAIT_TIME_MS);
//            gattConnect();
//        }
//
//        public void onServiceDisconnected(ComponentName classname) {
//            mGattService.disconnect();
//            mService = null;
//        }
//    };

    private LeScanCallback mScanCallBack = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {        	
        	mService.processMeshAdvert(scanRecord, rssi);
        }
    };
    
    public void connect() {
        if( mDevices == null){
            return;
        }
        try {
            if (mDevices.size() <= mMeshDeviceIndex) {
                mMeshDeviceIndex = 0;
            }
            BluetoothDevice bridgeDevice = mDevices.get(mMeshDeviceIndex);
            mConnectedAddress = bridgeDevice.getAddress();
            if (mMeshDeviceIndex < mDevices.size()) {
                mMeshDeviceIndex++;
            } else {
                mMeshDeviceIndex = 0;
            }
            mNumMeshConnectAttempts++;
            Log.d(TAG, "Mesh Connect attempt " + mNumMeshConnectAttempts + ", address: " + bridgeDevice.getAddress());
            mService.setHandler(mMeshHandler);
            mService.setLeScanCallback(mScanCallBack);
            mService.connectBridge(bridgeDevice);
        } catch (IndexOutOfBoundsException ioobe){
            ioobe.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Executed when LE link to bridge is connected.
     */
    private void onConnected() {
    	mMeshHandler.removeCallbacks(connectTimeout);
        hideProgress();

        mConnectTime = SystemClock.elapsedRealtime();
        mConnected = true;

        mNavListener = new SimpleNavigationListener(stActivity.getFragmentManager(), stActivity);
        if (mDeviceStore.getSetting() == null || mDeviceStore.getSetting().getNetworkKey() == null) {
            Log.d("TEST", "TEST");
        }
        startPeriodicColorTransmit();
    }
    private void onDisconnect(){
        mConnected = false;
    }

    public boolean isConnected(){
        return mConnected;
    }

    public void serviceAssociation(){
        if( !isConnected()){
            return;
        }
        mAssociationService = new AssociationService(stActivity, this);
        mAssociationService.setOnBLAssociationResultCallbacks(this);
        mAssociationService.serviceAssociation();
    }

    @Override
    public void onBLNonAssociationResult(String uuid, int uuidHsh, int rssi) {
        if( mBLResultCallbacks != null) {
            mBLResultCallbacks.onBLScanNonAssociationResult(new BluetoothVo(uuid, uuidHsh, rssi));
        }
    }

    @Override
    public void onBLAssociationResult(boolean isAssociated) {
        SPUtil.dismissDialog();
        if( !isAssociated) {
            if( mBLResultCallbacks != null) {
                mBLResultCallbacks.onBLAssociationCompleteResult(0, 0);
            }
        }
    }

    /**
     * Handle messages from mesh service.
     */
    private final Handler mMeshHandler = new MeshHandler(Looper.getMainLooper(), this);


    private static class MeshHandler extends Handler {
        private final WeakReference<BluetoothManager> mActivity;

        public MeshHandler(Looper looper, BluetoothManager bluetoothManager) {
            super(looper);
            mActivity = new WeakReference<>(bluetoothManager);
        }

        public void handleMessage(Message msg) {
            BluetoothManager parentActivity = mActivity.get();
            //Toast.makeText(stContext, "Receive Value : " + String.valueOf(msg.what), Toast.LENGTH_LONG).show();
            switch (msg.what) {
                case MeshService.MESSAGE_LE_CONNECTED: {
                    parentActivity.onConnected();
                    //Toast.makeText(BluetoothManager.stActivity, "블루투스 장비에 연결되었습니다. \n(address:" + mBridgeAddress + ")", Toast.LENGTH_SHORT).show();
                    if (mBLResultCallbacks != null) {
                        mBLResultCallbacks.onBLConnectedResult(true);
                    }
                    break;
                }
                case MeshService.MESSAGE_LE_DISCONNECTED: {
                    // If we haven't been connected at all yet then retry the connection.
                    parentActivity.onDisconnect();
                    long upTime = SystemClock.elapsedRealtime() - parentActivity.mConnectTime;
                    if (upTime < DISCONNECT_WATCH_TIME_MS) {
                        parentActivity.connect();
                    }
                    if (mBLResultCallbacks != null) {
                        mBLResultCallbacks.onBLConnectedResult(false);
                    } else {
                        //Toast.makeText(BluetoothManager.stActivity, "블루투스와의 연결을 해제하였습니다.", Toast.LENGTH_SHORT).show();
                        //BluetoothManager.stActivity.finish();
                    }
                    break;
                }
                case MeshService.MESSAGE_TIMEOUT:
                    int expectedMsg = msg.getData().getInt(MeshService.EXTRA_EXPECTED_MESSAGE);
                    parentActivity.onMessageTimeout(expectedMsg);
                    //SPUtil.showToast(stActivity, "블루투스 요청에 실패하였습니다.");
                    SPUtil.dismissDialog();
                    break;
                case MeshService.MESSAGE_DEVICE_DISCOVERED: {
                    ParcelUuid uuid = msg.getData().getParcelable(MeshService.EXTRA_UUID);
                    int uuidHash = msg.getData().getInt(MeshService.EXTRA_UUIDHASH_31);
                    int rssi = msg.getData().getInt(MeshService.EXTRA_RSSI);
                    if (parentActivity.mRemovedListener != null && parentActivity.mRemovedUuidHash == uuidHash) {
                        // This was received after a device was removed, so let the removed listener know.
                        parentActivity.mDeviceStore.removeDevice(parentActivity.mRemovedDeviceId);
                        parentActivity.mRemovedListener.onDeviceRemoved(parentActivity.mRemovedDeviceId, true);
                        parentActivity.mRemovedListener = null;
                        parentActivity.mRemovedUuidHash = 0;
                        parentActivity.mRemovedDeviceId = 0;
                        parentActivity.mService.setDeviceDiscoveryFilterEnabled(false);
                        removeCallbacks(parentActivity.removeDeviceTimeout);
                    } else if (parentActivity.mAssListener != null) {
                        // This was received after discover was enabled so let the uuid listener know.
                        parentActivity.mAssListener.newUuid(uuid.getUuid(), uuidHash, rssi);
                    }
                    break;
                }
                case MeshService.MESSAGE_DEVICE_ASSOCIATED: {
                    // New device has been associated and is telling us its device id.
                    // Request supported models before adding to DeviceStore, and the UI.
                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    int uuidHash = msg.getData().getInt(MeshService.EXTRA_UUIDHASH_31);
                    Log.d(TAG, "New device associated with id " + String.format("0x%x", deviceId));

                    if (parentActivity.mDeviceStore.getDevice(deviceId) == null) {
                        // Save the device id with the UUID hash so that we can store the UUID hash in the device
                        // object when MESSAGE_CONFIG_MODELS is received.
                        parentActivity.mDeviceIdtoUuidHash.put(deviceId, uuidHash);

                        // We add the device with no supported models. We will update that once we get the info.
                        if (uuidHash != 0) {
                            parentActivity.addDevice(deviceId, uuidHash, null, 0, false);
                        }

                        // If we don't already know about this device request its model support.
                        // We only need the lower 64-bits, so just request those.
                        ConfigModelApi.getInfo(deviceId, ConfigModelApi.DeviceInfo.MODEL_LOW);
                    }
                    break;
                }
                case MeshService.MESSAGE_CONFIG_DEVICE_INFO:
                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    int uuidHash = parentActivity.mDeviceIdtoUuidHash.get(deviceId);

                    ConfigModelApi.DeviceInfo infoType =
                            ConfigModelApi.DeviceInfo.values()[msg.getData().getByte(MeshService.EXTRA_DEVICE_INFO_TYPE)];
                    if (infoType == ConfigModelApi.DeviceInfo.MODEL_LOW) {
                        long bitmap = msg.getData().getLong(MeshService.EXTRA_DEVICE_INFORMATION);
                        // If the uuidHash was saved for this device id then this is an expected message, so process it.
                        if (uuidHash != 0) {
                            // Remove the uuidhash from the array as we have received its model support now.
                            parentActivity.mDeviceIdtoUuidHash
                                    .removeAt(parentActivity.mDeviceIdtoUuidHash.indexOfKey(deviceId));
                            String shortName = parentActivity.mUuidHashToAppearance.get(uuidHash);
                            if (shortName != null) {
                                parentActivity.mUuidHashToAppearance.remove(uuidHash);
                            }
                            parentActivity.addDevice(deviceId, uuidHash, shortName, bitmap, true);
                            if (parentActivity.mAssListener != null) {
                                parentActivity.mAssListener.deviceAssociated(true);
                            }

                            if (mBLResultCallbacks != null) {
                                mBLResultCallbacks.onBLAssociationCompleteResult(deviceId, uuidHash);
                            }
                        } else if (parentActivity.mDeviceIdtoUuidHash.size() == 0) {
                            if (parentActivity.mInfoListener != null) {
                                SingleDevice device = parentActivity.mDeviceStore.getSingleDevice(deviceId);
                                if (device != null) {
                                    device.setModelSupport(bitmap, 0);
                                    parentActivity.mDeviceStore.addDevice(device);
                                    parentActivity.mInfoListener.onDeviceConfigReceived(true);
                                } else {
                                    parentActivity.mInfoListener.onDeviceConfigReceived(false);
                                }


                            }
                        }
                    } else if (infoType == ConfigModelApi.DeviceInfo.VID_PID_VERSION) {
                        parentActivity.vid = msg.getData().getByteArray(MeshService.EXTRA_VID_INFORMATION);
                        parentActivity.pid = msg.getData().getByteArray(MeshService.EXTRA_PID_INFORMATION);
                        parentActivity.version = msg.getData().getByteArray(MeshService.EXTRA_VERSION_INFORMATION);
                        if (parentActivity.mInfoListener != null) {
                            parentActivity.mInfoListener.onDeviceInfoReceived(parentActivity.vid, parentActivity.pid, parentActivity.version, deviceId, true);
                        } else {
                            // shouldn't happen. Just in case for avoiding endless loops.
                            parentActivity.hideProgress();
                        }

                    }
                    break;
                case MeshService.MESSAGE_GROUP_NUM_GROUPIDS: {
                    if (parentActivity.mGroupAckListener != null) {
                        int numIds = msg.getData().getByte(MeshService.EXTRA_NUM_GROUP_IDS);
                        int modelNo = msg.getData().getByte(MeshService.EXTRA_MODEL_NO);
                        int expectedModelNo = parentActivity.mModelsToQueryForGroups.peek();
                        int deviceId1 = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);

                        if (expectedModelNo == modelNo) {
                            SingleDevice currentDev = parentActivity.mDeviceStore.getSingleDevice(deviceId1);
                            if (currentDev != null) {
                                currentDev.setNumSupportedGroups(numIds, modelNo);
                                parentActivity.mDeviceStore.addDevice(currentDev);
                                // We know how many groups are supported for this model now so remove it from the queue.
                                parentActivity.mModelsToQueryForGroups.remove();
                                if (parentActivity.mModelsToQueryForGroups.isEmpty()) {
                                    // If there are no more models to query then we can assign groups now.
                                    parentActivity.assignGroups(currentDev.getMinimumSupportedGroups());
                                } else {
                                    // Otherwise ask how many groups the next model supports, by taking the next model number from the queue.
                                    GroupModelApi.getNumModelGroupIds(parentActivity.mSendDeviceId, parentActivity.mModelsToQueryForGroups.peek());
                                }
                            } else {
                                parentActivity.mGroupAckListener.groupsUpdated(parentActivity.mSendDeviceId, false, stActivity.getString(R.string.group_query_fail));
                            }
                        }
                    }
                    break;
                }
                case MeshService.MESSAGE_GROUP_MODEL_GROUPID: {
                    // This is the ACK returned after calling setModelGroupId.
                    if (parentActivity.mGroupAckListener != null && parentActivity.mGroupAcksWaiting > 0) {
                        parentActivity.mGroupAcksWaiting--;
                        int index = msg.getData().getByte(MeshService.EXTRA_GROUP_INDEX);
                        int groupId = msg.getData().getInt(MeshService.EXTRA_GROUP_ID);
                        // Update the group membership of this device in the device store.
                        SingleDevice updatedDev = parentActivity.mDeviceStore.getSingleDevice(parentActivity.mSendDeviceId);
                        try {
                            updatedDev.setGroupId(index, groupId);

                        } catch (IndexOutOfBoundsException exception) {
                            parentActivity.mGroupSuccess = false;
                        }
                        parentActivity.mDeviceStore.addDevice(updatedDev);


                        if (parentActivity.mGroupAcksWaiting == 0) {
                            // Tell the listener that the update was OK.
                            parentActivity.mGroupAckListener.groupsUpdated(
                                    parentActivity.mSendDeviceId, true,
                                    parentActivity.mGroupSuccess ? stActivity.getString(R.string.group_update_ok) : stActivity.getString(R.string.group_update_with_problems));
                        }
                    }
                    break;
                }
                case MeshService.MESSAGE_FIRMWARE_VERSION:
                    parentActivity.mInfoListener.onFirmwareVersion(msg.getData().getInt(MeshService.EXTRA_DEVICE_ID), msg
                                    .getData().getInt(MeshService.EXTRA_VERSION_MAJOR),
                            msg.getData().getInt(MeshService.EXTRA_VERSION_MINOR), true);
                    parentActivity.mInfoListener = null;
                    break;
                case MeshService.MESSAGE_RECEIVE_STREAM_DATA:
                    deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    byte[] data = msg.getData().getByteArray(MeshService.EXTRA_DATA);
                    int sqn = msg.getData().getInt(MeshService.EXTRA_DATA_SQN);
                    if (deviceId == parentActivity.mSendDeviceId && sqn + data.length < DATA_BUFFER_SIZE) {
                        System.arraycopy(data, 0, parentActivity.mData, sqn, data.length);
                    }
                    // String receiveData  = new String(data, 0, data.length);
                    final String hexString = "0" + new java.math.BigInteger(data).toString(16);
                    try {
                        final String num = hexString.substring(0, 2);
                        final String plugId = String.format("%x", deviceId);
                        if (num.equalsIgnoreCase(BLConfig.DATA_RESPONSE_NUM)) { // Data Response
                            final String elec = hexString.substring(2, 6);
                            final String add = hexString.substring(6, 14);
                            final String onOff = hexString.substring(15, 16);
                            if (deviceId != BLConfig.RESULT_FAIL) {
                                mLastDataResultCallbacks.onGetLastData(plugId, add, elec, onOff);
                            }
                        } else if (num.equalsIgnoreCase(BLConfig.SCHEDULE_RESPONSE_NUM)) { // Schedule Response
                            final int result = Integer.parseInt(hexString.substring(2, 4));
                            if (result == BLConfig.RESULT_SUCCESS) {
                                if (mScheduleVo != null) {
                                    mScheduleResultCallbacks.onScheduleResult(mScheduleVo);
                                }
                            }
                        } else if (num.equalsIgnoreCase(BLConfig.CUTOFF_RESPONSE_NUM)) { // Cutoff Response
                            final int result = Integer.parseInt(hexString.substring(2, 4));
                            if (result == BLConfig.RESULT_SUCCESS) {
                                if (mCutoffVo != null) {
                                    mCutoffResultCallbacks.onCutoffResult(mCutoffVo);
                                }
                            }
                        } else if (num.equalsIgnoreCase(BLConfig.VA_RESPONSE_NUM)) { // VA Response

                        } else if (num.equalsIgnoreCase(BLConfig.ASSOCIATION_RESPONSE_NUM)) { // Association Response
                            final int result = Integer.parseInt(hexString.substring(2, 4));
                            if (result == BLConfig.RESULT_SUCCESS) {
                                SPUtil.showToast(stContext, "플러그를 등록하였습니다.");
                            }
                        } else if (num.equalsIgnoreCase(BLConfig.DEVICE_ID_RESPONSE_NUM)) { // Device Info Response
                            Log.d(TAG, "Get Device ID Success");
                            List<String> groupIdList = new ArrayList<String>();
                            int start = 2;
                            int end = start + 2;
                            for (int cnt = 0; cnt < BLConfig.MAX_GROUP_COUNT; cnt++) {
                                String groupId = hexString.substring(start, end);
                                if (!groupId.equalsIgnoreCase("00")) {
                                    groupIdList.add(groupId);
                                }
                                start = end;
                                end = start + 2;
                            }
                            if (mAssociatedCallbacks != null) {
                                mAssociatedCallbacks.onGetAssociatedDevice(plugId, groupIdList);
                            }
                        } else if(num.equalsIgnoreCase(BLConfig.GET_SCHEDULE_RESPONSE_NUM)){
                            final int result = Integer.parseInt(hexString.substring(12, 14));
                            if (result == BLConfig.RESULT_SUCCESS) {
                                String startTime = hexString.substring(4, 8);
                                String endTime = hexString.substring(8, 12);

                                // TODO : AM, PM 구해서 넣기
                                ScheduleVo scheduleVo = new ScheduleVo();
                                mScheduleResultCallbacks.onScheduleResult(scheduleVo);
                            }
                        } else if(num.equalsIgnoreCase(BLConfig.GET_CUTOFF_RESPONSE_NUM)){
                            final int result = Integer.parseInt(hexString.substring(10, 12));
                            if (result == BLConfig.RESULT_SUCCESS) {
                                String power = hexString.substring(2, 6);
                                String min = "";
                                try{
                                    min = String.valueOf(Integer.parseInt(hexString.substring(6, 10)) / 60);
                                } catch (NumberFormatException nfe){
                                    nfe.printStackTrace();
                                }
                                CutoffVo cutoffVo = new CutoffVo(power, min, true);
                                mCutoffResultCallbacks.onCutoffResult(cutoffVo);
                            }
                        }

                        SharedPreferences preference = stActivity.getSharedPreferences("last_device", 0);
                        SharedPreferences.Editor edit = preference.edit();
                        edit.putString("name", plugId);
                        edit.commit();
                    } catch (StringIndexOutOfBoundsException se) {
                        se.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case MeshService.MESSAGE_ASSOCIATING_DEVICE:
                    if (parentActivity.mAssListener != null) {
                        int progress = msg.getData().getInt(MeshService.EXTRA_PROGRESS_INFORMATION);
                        String message = msg.getData().getString(MeshService.EXTRA_PROGRESS_MESSAGE);
                        parentActivity.mAssListener.associationProgress(progress, message);
                    }
                    break;
                case MeshService.MESSAGE_RECEIVE_STREAM_DATA_END:
                    if (parentActivity.mDataListener != null) {
                        deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                        if (deviceId == parentActivity.mSendDeviceId) {
                            parentActivity.mDataListener.dataReceived(deviceId, parentActivity.mData);
                        } else {
                            parentActivity.mDataListener.dataGroupReceived(deviceId);
                        }
                    }
                    break;
                case MeshService.MESSAGE_RECEIVE_BLOCK_DATA:
                    if (true) {
                        deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                        byte[] data1 = msg.getData().getByteArray(MeshService.EXTRA_DATA);
                        int sqn1 = msg.getData().getInt(MeshService.EXTRA_DATA_SQN);
                        if (deviceId == parentActivity.mSendDeviceId && sqn1 + data1.length < DATA_BUFFER_SIZE) {
                            System.arraycopy(data1, 0, parentActivity.mData, sqn1, data1.length);
                        }
                    }
                    break;
            }
        }
    }

    private static CutoffVo mCutoffVo;
    public void setCutoffVo(CutoffVo cutoffVo){
        mCutoffVo = cutoffVo;
    }

    private static ScheduleVo mScheduleVo;
    public void setSchedule(ScheduleVo scheduleVo){
        mScheduleVo = scheduleVo;
    }

    /**
     * Called when a response is not seen to a sent command.
     * 
     * @param expectedMessage
     *            The message that would have been received in the Handler if there hadn't been a timeout.
     */
    private void onMessageTimeout(int expectedMessage) {
        switch (expectedMessage) {

            case MeshService.MESSAGE_GROUP_MODEL_GROUPID:
                if (mGroupAcksWaiting > 0) {
                    if (mGroupAckListener != null) {
                        // Timed out waiting for group update ACK.
                        mGroupAckListener.groupsUpdated(mSendDeviceId, false,
                                stActivity.getString(R.string.group_timeout));
                    }
                    mGroupAcksWaiting = 0;
                }
                break;
            case MeshService.MESSAGE_DEVICE_ASSOCIATED:
                // Fall through.
            case MeshService.MESSAGE_CONFIG_MODELS:
                // If we couldn't find out the model support for the device then we have to report association failed.
                if (mAssListener != null) {
                    mAssListener.deviceAssociated(false);
                }
                if (mInfoListener!= null) {
                    mInfoListener.onDeviceConfigReceived(false);
                }
                break;
            case MeshService.MESSAGE_FIRMWARE_VERSION:
                if (mInfoListener != null) {
                    mInfoListener.onFirmwareVersion(0, 0, 0, false);
                }
                break;
            case MeshService.MESSAGE_GROUP_NUM_GROUPIDS:
                if (mGroupAckListener != null) {
                    mGroupAckListener.groupsUpdated(mSendDeviceId, false, stActivity.getString(R.string.group_query_fail));
                }
                break;
            case MeshService.MESSAGE_CONFIG_DEVICE_INFO:

                // if we were waiting to get the configModels once we associate the device, we just assume we couldn't get the models
                // that the device support, but the association was successful.
                if (mDeviceIdtoUuidHash.size() > 0) {

                    Device device =mDeviceStore.getDevice(mDeviceIdtoUuidHash.keyAt(0));
                    mDeviceIdtoUuidHash.removeAt(0);
                    if (device != null) {
                        //Toast.makeText(stActivity, device.getName() + " " + stActivity.getString(R.string.added),Toast.LENGTH_SHORT).show();
                    }
                    if (mAssListener != null) {
                        mAssListener.deviceAssociated(true);
                    }
                }
                if (mInfoListener!= null) {
                    mInfoListener.onDeviceConfigReceived(false);
                }
                if (mInfoListener != null) {
                    mInfoListener.onDeviceInfoReceived(new byte[0], new byte[0], new byte[0], 0, false);
                }
                break;

        }
    }

    /**
     * Send group assign messages to the currently selected device using the groups contained in mNewGroups.
     */
    private void assignGroups(int numSupportedGroups) {
        if (mSendDeviceId == Device.DEVICE_ID_UNKNOWN)
            return;
        // Check the number of supported groups matches the number requested to be set.
        if (numSupportedGroups >= mNewGroups.size()) {

            mGroupAcksWaiting = 0;

            // Make a copy of existing groups for this device.
            mGroupsToSend = mDeviceStore.getSingleDevice(mSendDeviceId).getGroupMembershipValues();
            // Loop through existing groups.
            for (int i = 0; i < mGroupsToSend.size(); i++) {
                int groupId = mGroupsToSend.get(i);
                if (groupId != 0) {
                    int foundIndex = mNewGroups.indexOf(groupId);
                    if (foundIndex > -1) {
                        // The device is already a member of this group so remove it from the list of groups to add.
                        mNewGroups.remove(foundIndex);
                    }
                    else {
                        // The device should no longer be a member of this group, so set that index to -1 to flag
                        // that a message must be sent to update this index.
                        mGroupsToSend.set(i, -1);
                    }
                }
            }
            // Now loop through currentGroups, and for every index set to -1 or zero send a group update command for
            // that index with one of our new groups if one is available. If there are no new groups to set, then just
            // send a message for all indices set to -1, to set them to zero.
            boolean commandSent = false;
            for (int i = 0; i < mGroupsToSend.size(); i++) {
                int groupId = mGroupsToSend.get(i);
                if (groupId == -1 || groupId == 0) {
                    if (mNewGroups.size() > 0) {
                        int newGroup = mNewGroups.get(0);
                        mNewGroups.remove(0);
                        commandSent = true;
                        sendGroupCommands(mSendDeviceId, i, newGroup);
                    }
                    else if (groupId == -1) {
                        commandSent = true;
                        sendGroupCommands(mSendDeviceId, i, 0);
                    }
                }
            }
            if (!commandSent) {
                // There were no changes to the groups so no updates were sent. Just tell the listener
                // that the operation is complete.
                if (mGroupAckListener != null) {
                    mGroupAckListener.groupsUpdated(mSendDeviceId, true, stActivity.getString(R.string.group_no_changes));
                }
            }
        }
        else {
            // Not enough groups supported on device.
            if (mGroupAckListener != null) {
                mGroupAckListener.groupsUpdated(mSendDeviceId, false,
                        stActivity.getString(R.string.group_max_fail) + " " + numSupportedGroups + " " + stActivity.getString(R.string.groups));
            }
        }
    }

    private void sendGroupCommands(int deviceId, int index, int group) {
        mGroupSuccess = true;

        SingleDevice dev = mDeviceStore.getSingleDevice(deviceId);

        if (dev.isModelSupported(LightModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(LightModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, LightModelApi.MODEL_NUMBER,index, 0, group );
            // If a light also supports power then set groups for that too.
            if (dev.isModelSupported(LightModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(LightModelApi.MODEL_NUMBER) != 0) {
                mGroupAcksWaiting++;
                GroupModelApi.setModelGroupId(deviceId, PowerModelApi.MODEL_NUMBER, index, 0, group);
            }
        }
        else if (dev.isModelSupported(SwitchModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(SwitchModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, SwitchModelApi.MODEL_NUMBER, index, 0, group);
        }

        // Check if device supports data model and that it supports groups. If it does, then setModelGroupId
        if (dev.isModelSupported(DataModelApi.MODEL_NUMBER) &&
                dev.getNumSupportedGroups(DataModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, DataModelApi.MODEL_NUMBER, index, 0, group);
        }
    }
    
    // Runnables that execute after a timeout /////
    
    /**
     * This is the implementation of the periodic timer that will call sendLightRgb() every TRANSMIT_PERIOD_MS if
     * mNewColor is set to TRUE.
     */    
    private Runnable transmitCallback = new Runnable() {
        @Override
        public void run() {
            if (mNewColor) {
                if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN) {                    
                    byte red = (byte) (Color.red(mColorToSend) & 0xFF);
                    byte green = (byte) (Color.green(mColorToSend) & 0xFF);
                    byte blue = (byte) (Color.blue(mColorToSend) & 0xFF);

                    LightModelApi.setRgb(mSendDeviceId, red, green, blue, (byte) 0xFF, 0, false);

                    Device light = mDeviceStore.getDevice(mSendDeviceId);
                    LightState state = (LightState)light.getState(StateType.LIGHT);
                    if (light != null) {
                    	state.setRed(red);
                    	state.setGreen(green);
                    	state.setBlue(blue);
                    	state.setStateKnown(true);
                    	light.setState(state);                    	
                        mDeviceStore.addDevice(light);
                    }
                }
                // Colour sent so clear the flag.
                mNewColor = false;
            }                        
            mMeshHandler.postDelayed(this, TRANSMIT_PERIOD_MS);
        }
    };
    
    private Runnable removeDeviceTimeout = new Runnable() {
		@Override
		public void run() {
			// Handle timeouts on removing devices.
            if (mRemovedListener != null) {
                // Timed out waiting for device UUID that indicates device removal happened.
                mRemovedListener.onDeviceRemoved(mRemovedDeviceId, false);
                mRemovedListener = null;
                mRemovedUuidHash = 0;
                mRemovedDeviceId = 0;
                mService.setDeviceDiscoveryFilterEnabled(false);
            }
		}
    };
    
    private Runnable connectTimeout = new Runnable() {
        @Override
        public void run() {
        	if (mNumMeshConnectAttempts < CONNECT_RETRIES) {
        		mMeshHandler.postDelayed(connectTimeout, CONNECT_WAIT_TIME_MS);
        		connect();
        	}
        	else {
	        	hideProgress();
	        	Toast.makeText(stActivity, stActivity.getString(R.string.connect_faiL), Toast.LENGTH_SHORT).show();
                if( mBLResultCallbacks != null) {
                    mBLResultCallbacks.onBLConnectedResult(false);
                }
        	}
        }        
    };

//    private Runnable gattConnectTimeout = new Runnable() {
//        @Override
//        public void run() {
//            if (mNumGattConnectAttempts < CONNECT_RETRIES) {
//                mGattHandler.postDelayed(gattConnectTimeout, CONNECT_WAIT_TIME_MS);
//                gattConnect();
//            }
//            else {
//                hideProgress();
//                Toast.makeText(stActivity, stActivity.getString(R.string.connect_faiL), Toast.LENGTH_SHORT).show();
//                if( mBLResultCallbacks != null) {
//                    mBLResultCallbacks.onBLConnectedResult(false);
//                }
//            }
//        }
//    };

    private Runnable progressTimeOut = new Runnable() {
        @Override
        public void run() {
        	
        	if(mDataListener!=null)
        		mDataListener.UITimeout();
        	
        }        
    };  
        
    // End of timeout handlers /////
    
    /**
     * Start transmitting colours to the current send address. Colours are sent every TRANSMIT_PERIOD_MS ms.
     */
    private void startPeriodicColorTransmit() {
        mMeshHandler.postDelayed(transmitCallback, TRANSMIT_PERIOD_MS);
    }

    /**
     * Show a modal progress dialogue until hideProgress is called.
     * 
     * @param message
     *            The message to display in the dialogue.
     */
    private void showProgress(String message) {
    	if(mProgress==null){
    		mProgress = new ProgressDialog(stActivity);
	        mProgress.setMessage(message);
	        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mProgress.setIndeterminate(true);
	        mProgress.setCancelable(false);        
	        mProgress.show();
    	}
    }

    /**
     * Hide the progress dialogue.
     */
    private void hideProgress() {
    	if(mProgress!=null){
    		mProgress.dismiss();
        	mProgress=null;
    	}
    }
    
    /**
     * Add a device to the device store, creating state based on model support.
     * @param deviceId Device id of the device to add.
     * @param uuidHash 31-bit UUID hash of the device to add.
     * @param modelSupportBitmapLow The low part of the model support bitmap. Currently the only part we care about.
     */
//    private void addDevice(int deviceId, int uuidHash, long modelSupportBitmapLow, boolean showToast) {
//
//    	SingleDevice device = new SingleDevice(deviceId, " ", uuidHash, modelSupportBitmapLow, 0);
//    	addDevice(device);
//
//    	if(showToast){
//    		//Toast.makeText(stActivity, device.getName() + " " + stActivity.getString(R.string.added), Toast.LENGTH_SHORT).show();
//    	}
//
//    }

    public void addDevice(int deviceId, int uuidHash, String shortName, long modelSupportBitmapLow, boolean showToast) {
        String name = null;
        if (shortName != null) {
            int id = deviceId - Device.DEVICE_ADDR_BASE;
            name = String.format(shortName.trim() + " %d", id);
        }
        SingleDevice device = new SingleDevice(deviceId, name, uuidHash, modelSupportBitmapLow, 0);
        mDeviceStore.addDevice(device);

        if (showToast) {
            //Toast.makeText(stActivity, device.getName() + " " + stActivity.getString(R.string.added), Toast.LENGTH_SHORT).show();
        }

    }
    
//    private void addDevice(SingleDevice device) {
//    	String name = null;
//    	if (device.isModelSupported(LightModelApi.MODEL_NUMBER)) {
//    		name = "Light ";
//    		device.setState(new LightState());
//    	}
//    	if (device.isModelSupported(SwitchModelApi.MODEL_NUMBER)) {
//    		if (name == null) {
//    			name = "Switch ";
//    		}
//    	}
//    	if (device.isModelSupported(PowerModelApi.MODEL_NUMBER)) {
//    		device.setState(new PowState());
//    	}
//    	if (name == null) {
//    		name = "Plug ";
//    	}
//    	device.setName(name + String.format("%x", device.getDeviceId()));
//    	mDeviceStore.addSingleDevice(device);
//    }
    public void removeDevice(int deviceId){
        mDeviceStore.removeDevice(deviceId);
    }
    public void removeDeviceAll(){
        List<Device> deviceList = getDevices();
        for( int cnt=0; cnt<deviceList.size(); cnt++) {
            int deviceId = deviceList.get(cnt).getDeviceId();
            mDeviceStore.removeDevice(deviceId);
        }
    }
    /**
     * Restore app settings including devices and groups.
     */
    private void restoreSettings(int settingsID) {
        // Try to get the settings if we know the ID.
        if (settingsID != Setting.UKNOWN_ID) {
            mDeviceStore.loadSetting(settingsID);
        }
        // save in sharePreferences the last settings used.
        SharedPreferences activityPrefs = stActivity.getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = activityPrefs.edit();

        if (mDeviceStore.getSetting() != null) {

            // set the networkKey to MeshService.
            mService.setNetworkPassPhrase(mDeviceStore.getSetting().getNetworkKey());


            // save in sharePreferences the last settings used.
            editor.putInt(SETTING_LAST_ID, settingsID);
            editor.commit();

            // get all the SingleDevices and GroudDevices from the dataBase.
            mDeviceStore.loadAllDevices();

            // set next device id to be used according with the last device used in the database.
            mService.setNextDeviceId(mDeviceStore.getSetting().getLastDeviceIndex()+1);
        }
        else {
            // No setting founded. We need to create one...
            Setting setting = new Setting();
            setting.setLastGroupIndex(Device.GROUP_ADDR_BASE + 5);
            mDeviceStore.setSetting(setting, true);

            // add group devices. By default we add 5 groups (1 for "All" with id=0 and 4 extra with ids 1-4).
            for (int i=0; i < 5 ; i++) {
                GroupDevice group;
                if (i==0) {
                    group = new GroupDevice(Device.GROUP_ADDR_BASE, stActivity.getString(R.string.all_lights));
                }
                else {
                    group = new GroupDevice(Device.GROUP_ADDR_BASE + i, stActivity.getString(R.string.group) + " " + i);
                }

                // store the group in the database.
                mDeviceStore.addGroupDevice(group,true);
            }

            // save in sharePreferences the last settings used.
            editor.putInt(SETTING_LAST_ID, mDeviceStore.getSetting().getId());
            editor.commit();

        }
        
    }

    @Override
    public void discoverDevices(boolean enabled, AssociationListener listener) {
        if (enabled) {
            mAssListener = listener;
        }
        else {
            mAssListener = null;
        }
        
        //avoiding crashes
        if(mService !=null)
        	mService.setDeviceDiscoveryFilterEnabled(enabled);
    }

    @Override
    public void associateDevice(int uuidHash) {
        try {
            mService.associateDevice(uuidHash, 0);
        } catch (AssociationFailedException afe){
            SPUtil.showToast(stActivity, "Bluetooth 플러그를 추가하지 못했습니다.");
        }
    }

    @Override
    public boolean associateDevice(int uuidHash, String shortCode) {
    	int decodedHash = MeshService.getDeviceHashFromShortcode(shortCode);
    	if (decodedHash == uuidHash) {    		    	
    		mService.associateDevice(uuidHash, MeshService.getAuthorizationCode(shortCode));
    		return true;
    	}
    	return false;
    }

    @Override
    public Device getDevice(int deviceId) {
        return mDeviceStore.getDevice(deviceId);
    }

    @Override
    public void setSelectedDeviceId(int deviceId) {
        Log.d(TAG, String.format("Device id is now 0x%x", deviceId));
        mSendDeviceId = deviceId;
    }

    @Override
    public int getSelectedDeviceId() {
        return mSendDeviceId;
    }

    @Override
    public void setLightColor(int color, int brightness) {
        if (brightness < 0 || brightness > 99) {
            throw new NumberFormatException("Brightness value should be between 0 and 99");
        }

        // Convert currentColor to HSV space and make the brightness (value) calculation. Then convert back to RGB to
        // make the colour to send.
        // Don't modify currentColor with the brightness or else it will deviate from the HS colour selected on the
        // wheel due to accumulated errors in the calculation after several brightness changes.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = ((float) brightness + 1) / 100.0f;
        mColorToSend = Color.HSVToColor(hsv);

        // Indicate that there is a new colour for next time the timer fires.
        mNewColor = true;
    }

    @Override
    public void setLightPower(PowerModelApi.PowerState state) {
        PowerModelApi.setState(mSendDeviceId, state, false);
        setLocalLightPower(state);
    }
    
    
    @Override
    public void setLocalLightPower(PowerModelApi.PowerState state) {
        Device dev = mDeviceStore.getDevice(mSendDeviceId);
        if (dev != null) {
            PowState powState = (PowState)dev.getState(StateType.POWER);
            if( powState != null){
                powState.setPowerState(state);
            }
            mDeviceStore.addDevice(dev);
        }
    }
    
    @Override
    public void removeDevice(RemovedListener listener) {
        if (mSendDeviceId < Device.DEVICE_ADDR_BASE && mSendDeviceId >= Device.GROUP_ADDR_BASE) {
            mDeviceStore.removeDevice(mSendDeviceId);
            listener.onDeviceRemoved(mSendDeviceId, true);
            mSendDeviceId = Device.GROUP_ADDR_BASE;
        }
        else {
            mRemovedUuidHash = mDeviceStore.getSingleDevice(mSendDeviceId).getUuidHash();
            mRemovedDeviceId = mSendDeviceId;
            mRemovedListener = listener;
            // Enable discovery so that the device uuid message is received when the device is unassociated.
            mService.setDeviceDiscoveryFilterEnabled(true);
            // Send CONFIG_RESET
            ConfigModelApi.resetDevice(mSendDeviceId);
            mSendDeviceId = Device.GROUP_ADDR_BASE;
            // Start a timer so that we don't wait for the ack forever.
            mMeshHandler.postDelayed(removeDeviceTimeout, REMOVE_ACK_WAIT_TIME_MS);
        }
    }

    @Override
    public void getFwVersion(InfoListener listener) {
        mInfoListener = listener;
        FirmwareModelApi.getVersionInfo(mSendDeviceId);
    }
    
    @Override
    public void getVID_PID_VERSION(InfoListener listener) {
        mInfoListener = listener;
        ConfigModelApi.getInfo(mSendDeviceId, ConfigModelApi.DeviceInfo.VID_PID_VERSION);
    }

    @Override
    public void setDeviceGroups(List<Integer> groups, GroupListener listener) {
        if (mSendDeviceId == Device.DEVICE_ID_UNKNOWN)
            return;
        mNewGroups.clear();
        mGroupAckListener = listener;
        boolean inProgress = false;
        for (int group : groups) {
            mNewGroups.add(group);
        }
        SingleDevice selectedDev = mDeviceStore.getSingleDevice(mSendDeviceId);

        if( selectedDev == null){
            return;
        }

        // Send message to find out how many group ids the device supports for each model type.
        // Once a response is received to this command sendGroupAssign will be called to assign the groups.
        if (selectedDev.isModelSupported(LightModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(LightModelApi.MODEL_NUMBER)) {
            // Only query light model and assume power model supports the same number.
            mModelsToQueryForGroups.add(LightModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (selectedDev.isModelSupported(SwitchModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(SwitchModelApi.MODEL_NUMBER)) {
            mModelsToQueryForGroups.add(SwitchModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (selectedDev.isModelSupported(DataModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(DataModelApi.MODEL_NUMBER)) {
            mModelsToQueryForGroups.add(DataModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (inProgress) {
            GroupModelApi.getNumModelGroupIds(mSendDeviceId,mModelsToQueryForGroups.peek());
        }
        else {
            // We already know the number of supported groups from a previous query, so go straight to assigning.
            assignGroups(selectedDev.getMinimumSupportedGroups());
            inProgress = true;
        }


        // There isn't any operation to do, so the dialog should be dismissed.
        if (!inProgress) {
            mGroupAckListener.groupsUpdated(mSendDeviceId, false, stActivity.getString(R.string.group_query_fail));
        }
    }

    @Override
    public void setDeviceName(int deviceId, String name) {
    	mDeviceStore.updateDeviceName(deviceId, name);        
    }

    @Override
    public void setSecurity(String networkKeyPhrase, boolean authRequired) {
        Setting setting = mDeviceStore.getSetting();
        if (setting != null) {
            // Set the new setting values
            setting.setNetworkKey(networkKeyPhrase);
            setting.setAuthRequired(authRequired);
        }
        else {
            // if we don't have settings yet we need to create one and set the new setting values.
            setting = new Setting();
            setting.setNetworkKey(networkKeyPhrase);
            setting.setAuthRequired(authRequired);
        }
        // store the setting in the database.
        mDeviceStore.setSetting(setting, true);

        // set the new NetworkPassPhrase to the MeshService
        mService.setNetworkPassPhrase(mDeviceStore.getSetting().getNetworkKey());

        // change to the association fragment.
        mNavListener.setNavigationEnabled(true);
    }

    @Override
    public boolean isAuthRequired() {
        Setting setting = mDeviceStore.getSetting();
        return setting.isAuthRequired();
    }

    @Override
    public String getNetworkKeyPhrase() {
        Setting setting = mDeviceStore.getSetting();
        return setting.getNetworkKey();
    }

    @Override
    public void associateWithQrCode(AssociationStartedListener listener) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            stActivity.startActivityForResult(intent, 0);
        }
        catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            stActivity.startActivity(marketIntent);
        }
    }

    public interface BLResultCallbacks {
        void onBLConnectedResult(boolean isConnected);
        void onBLScanNonAssociationResult(BluetoothVo bluetoothVo);
        void onBLAssociationCompleteResult(int deviceId, int uuidHash);
    }

    public void setOnBLResultCallbacks(final BLResultCallbacks callbacks){
        mBLResultCallbacks = callbacks;
    }

    public void setOnLastDataResultCallbacks(final LastDataResultCallbacks callbacks){
        mLastDataResultCallbacks = callbacks;
    }

    public void setOnAssociatedDevicesResultCallbacks(AssociatedDevicesResultCallbacks callbacks){
        mAssociatedCallbacks = callbacks;
    }

    public void setOnScheduleResultCallbcks(ScheduleResultCallbacks callbacks){
        mScheduleResultCallbacks = callbacks;
    }

    public void setOnCutoffResultCallbacks(CutoffResultCallbacks callbacks){
        mCutoffResultCallbacks = callbacks;
    }

//    public void setOnBLControlResultCallbacks(BLControlResultCallbacks callbacks){
//        mBLControlResultCallbacks = callbacks;
//    }

    private static String tryString(String string, String fallback){
        if(string == null){
            return fallback;
        } else{
            return string;
        }
    }

    @Override
    public Device addLightGroup(String groupName) {
        GroupDevice result = new GroupDevice(mDeviceStore.getSetting().getNextGroupIndex(), groupName);
        mDeviceStore.addGroupDevice(result,true);
        return result;
    }

	@Override
	public void setAttentionEnabled(boolean enabled) {
		AttentionModelApi.setState(mSendDeviceId, enabled, AttentionModelApi.DURATION_INFINITE);
	}

    @Override
    public void removeDeviceLocally(RemovedListener removedListener) {
        mDeviceStore.removeDevice(mSendDeviceId);
        removedListener.onDeviceRemoved(mSendDeviceId, true);
        mSendDeviceId = Device.GROUP_ADDR_BASE;
        removedListener = null;        
    }

	@Override
	public String getBridgeAddress() {
		if (mConnected) {
			return mConnectedAddress;
		}
		else {
			return null;
		}
	}	

	@Override
	public List<Device> getDevices(int ... modelNumber) {
		ArrayList<Device> result = new ArrayList<Device>();
		for (Device dev : mDeviceStore.getAllSingleDevices()) {			
			if (((SingleDevice)dev).isAnyModelSupported(modelNumber)) {
				result.add(dev);
			}
		}
		return result;
	}

    public List<Device> getDevices() {
        ArrayList<Device> result = new ArrayList<Device>();
        for (Device dev : mDeviceStore.getAllSingleDevices()) {
            result.add(dev);
        }
        return result;
    }
	
	@Override
	public ArrayList<String> getModelsLabelSupported(int deviceId) {
		
		Device device =mDeviceStore.getDevice(deviceId);
		if(device instanceof SingleDevice){
			return ((SingleDevice)device).getModelsLabelSupported();
		}
		return null;
	}
	 
   
	@Override
	public List<Device> getGroups() {
		return mDeviceStore.getAllGroups();
	}

	@Override
	public void getDeviceData(DataListener listener) {
		this.mDataListener = listener;
		mService.setContinuousLeScanEnabled(true);
		DeviceInfoProtocol.requestDeviceInfo(mSendDeviceId);
	}
	
	public Handler getMeshHandler(){
		return mMeshHandler;
	}

	@Override
	public void startUITimeOut() {
		mMeshHandler.postDelayed(progressTimeOut, PROGRESS_DIALOG_TIME_MS);
		
	}

	@Override
	public void stopUITimeOut() {
		mMeshHandler.removeCallbacks(progressTimeOut);
	}

    @Override
    public void sendData(byte[] data, boolean ack) {
        if( isConnected()) {
            DataModelApi dataModelApi = new DataModelApi();
            if (data.length > 0) {
                DataModelApi.sendData(mSendDeviceId, data, ack);
            }
        }
    }
}
