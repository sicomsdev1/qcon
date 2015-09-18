package com.sicoms.smartplug.domain;

import com.sicoms.smartplug.dao.DbBluetoothVo;

import java.util.List;

/**
 * Created by gudnam on 2015. 9. 8..
 */
public class PlugAllDataVo {
    private List<PlugVo> plugVoList;
    private List<DbBluetoothVo> bluetoothVoList;

    public PlugAllDataVo(List<PlugVo> plugVoList, List<DbBluetoothVo> bluetoothVoList){
        this.plugVoList = plugVoList;
        this.bluetoothVoList = bluetoothVoList;
    }

    public List<PlugVo> getPlugVoList() {
        return plugVoList;
    }

    public void setPlugVoList(List<PlugVo> plugVoList) {
        this.plugVoList = plugVoList;
    }

    public List<DbBluetoothVo> getBluetoothVoList() {
        return bluetoothVoList;
    }

    public void setBluetoothVoList(List<DbBluetoothVo> bluetoothVoList) {
        this.bluetoothVoList = bluetoothVoList;
    }
}
