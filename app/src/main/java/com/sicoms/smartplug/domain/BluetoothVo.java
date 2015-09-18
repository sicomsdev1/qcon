package com.sicoms.smartplug.domain;

import com.sicoms.smartplug.network.bluetooth.BLScanner.ScanInfo;
import com.sicoms.smartplug.network.bluetooth.util.Device;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class BluetoothVo implements Comparable<ScanInfo> {
    private static final long TIME_SCANINFO_VALID = 5* 1000; // 5 secs

    private Device device; // 미사용
    private String uuid;
    private int uuidHash;
    private int rssi;
    public long timeStamp;

    public BluetoothVo(String uuid, int uuidHash, int rssi) {
        this.uuid = uuid;
        this.uuidHash = uuidHash;
        this.rssi = rssi;
        updated();
    }
    public void updated() {
        this.timeStamp = System.currentTimeMillis();
    }
    @Override
    public int compareTo(ScanInfo info) {
        // return
        if(this.rssi>info.rssi)
            return -1;
        else if(this.rssi<info.rssi)
            return 1;
        return 0;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getUuidHash() {
        return uuidHash;
    }

    public void setUuidHash(int uuidHash) {
        this.uuidHash = uuidHash;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * This method check if the timeStamp of the last update is still valid or not (time<TIME_SCANINFO_VALID).
     * @return true if the info is still valid
     */
    public boolean isInfoValid(){
        return ((System.currentTimeMillis()-this.timeStamp)<TIME_SCANINFO_VALID);
    }
}
