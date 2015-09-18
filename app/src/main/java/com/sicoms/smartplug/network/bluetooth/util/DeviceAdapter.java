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

package com.sicoms.smartplug.network.bluetooth.util;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * An Adapter that can be used to display a list of Device objects in sorted order.
 * 
 */
public class DeviceAdapter extends BaseAdapter {
    // List of device ids    
    private SparseArray<String> mDevices = new SparseArray<String>();
            
    private final Context mContext;

    public DeviceAdapter(Context context) {
        this.mContext = context;
    }

    public DeviceAdapter(Context context, List<Device> devices) {
        this.mContext = context;                    
        setDevices(devices);        
    }

    /**
     * Add a new device.
     * 
     * @param device
     *            The device to add.
     */
    public void addDevice(Device device) {
        mDevices.put(device.getDeviceId(), device.getName());
        notifyDataSetChanged();
    }
           
    /**
     * Set the list of devices. The list will be sorted.
     * 
     * @param devices
     *            The list of devices.
     */
    public void setDevices(List<Device> devices) {
        mDevices.clear();
        for (Device dev : devices) {        
            mDevices.put(dev.getDeviceId(), dev.getName());        
        }
        notifyDataSetChanged();
    }
    
    /**
     * Remove a single device from the adapter.
     * 
     * @param device
     *            The device to remove.
     */
    public void remove(int deviceId) {
        mDevices.remove(deviceId);        
        notifyDataSetChanged();
    }
    
    /**
     * Remove all devices from this adapter.
     */
    public void clear() {
        mDevices.clear();
        notifyDataSetChanged();
    }    
        
    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public String getItem(int position) {
        return mDevices.valueAt(position);
    }
    
    public int getItemDeviceId(int position) {
        return mDevices.keyAt(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getDevicePosition(int selectedDeviceId) {
        int index = mDevices.indexOfKey(selectedDeviceId);
        if (index < 0) {
            throw new IllegalArgumentException("Device id does not exist: " + String.format("0x%x", index));
        }
        return index;
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return customView(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return customView(position, convertView, parent, android.R.layout.simple_spinner_item);
    }

    private View customView(int position, View convertView, ViewGroup parent, int layoutResource) {
        TextView deviceView;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(layoutResource, parent, false);
            deviceView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(new ViewHolder(deviceView));
        }
        else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            deviceView = viewHolder.deviceView;
        }

        String deviceName = getItem(position);
        deviceView.setText(deviceName);

        return convertView;
    }

    private static class ViewHolder {
        public final TextView deviceView;

        public ViewHolder(TextView deviceView) {
            this.deviceView = deviceView;
        }
    }          
}