package com.sicoms.smartplug.network.bluetooth;

import com.sicoms.smartplug.network.bluetooth.util.DeviceController;

/**
 * Created by gudnam on 2015. 7. 13..
 */
public class BLMessage {
    private static final String TAG = BLMessage.class.getSimpleName();

    // Data
    public static String getDataRequestMessage(DeviceController controller, int deviceId){
        controller.setSelectedDeviceId(deviceId);
        String unixTime = String.format("%x", (System.currentTimeMillis() / 1000) + 32400);
        String requestMessage = BLConfig.DATA_REQUEST_NUM + unixTime;
        return requestMessage;
    }

    // Set Schedule
    public static String getScheduleRequestMessage(DeviceController controller, int deviceId, long scheduleNum, int startTime, int endTime, String status){
        controller.setSelectedDeviceId(deviceId);
        scheduleNum = 1; // 일단 한개만 저장 될 수 있음
        String requestMessage = BLConfig.SCHEDULE_REQUEST_NUM + String.format("%02d", scheduleNum) + String.format("%04x", startTime) + String.format("%04x", endTime) + status;
        return requestMessage;
    }

    // Get Schedule
    public static String getGetScheduleRequestMessage(DeviceController controller, int deviceId, long scheduleNum){
        controller.setSelectedDeviceId(deviceId);
        scheduleNum = 1;
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
        String requestMessage = BLConfig.GET_SCHEDULE_REQUEST_NUM + String.format("%02d", scheduleNum) + unixTime;
        return requestMessage;
    }

    // Set Cutoff
    public static String getSetCutoffRequestMessage(DeviceController controller, int deviceId, int power, int min, String status){
        controller.setSelectedDeviceId(deviceId);
        String requestMessage = BLConfig.CUTOFF_REQUEST_NUM + String.format("%02d", power) + String.format("%02d", min) + status;
        return requestMessage;
    }

    // Get Cutoff
    public static String getGetCutoffRequestMessage(DeviceController controller, int deviceId){
        controller.setSelectedDeviceId(deviceId);
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
        String requestMessage = BLConfig.GET_CUTOFF_REQUEST_NUM + unixTime;
        return requestMessage;
    }

    // VA

    // Association
    public static String getAssociationRequest(DeviceController controller, int deviceId){
        controller.setSelectedDeviceId(deviceId);
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
        String requestMessage = BLConfig.ASSOCIATION_REQUEST_NUM + unixTime;
        return requestMessage;
    }

    // Device ID
    public static String getDeviceIdRequestMessage(DeviceController controller){
        controller.setSelectedDeviceId(0);
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
        String requestMessage = BLConfig.DEVICE_ID_REQUEST_NUM + unixTime;
        return requestMessage;
    }
    public static String getDeviceIdRequestMessage(DeviceController controller, int deviceId){
        controller.setSelectedDeviceId(deviceId);
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
        String requestMessage = BLConfig.DEVICE_ID_REQUEST_NUM + unixTime;
        return requestMessage;
    }

    // LED Control
    public static String getLEDControlRequest(DeviceController controller, int deviceId, boolean isOn){
        controller.setSelectedDeviceId(deviceId);
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
        String status = isOn ? "0" : "9";
        String requestMessage = BLConfig.LED_CONTROL_REQUEST_NUM + unixTime + status;
        return requestMessage;
    }
}
