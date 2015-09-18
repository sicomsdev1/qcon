package com.sicoms.smartplug.network.bluetooth.util;

import android.content.Context;

import com.csr.mesh.LightModelApi;
import com.csr.mesh.PowerModelApi;
import com.csr.mesh.SwitchModelApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DeviceStore {
	private LinkedHashMap<Integer, Device> mDevices = new LinkedHashMap<Integer, Device>();
	private LinkedHashMap<Integer, Device> mGroups = new LinkedHashMap<Integer, Device>();
	private DataBaseDataSource mDataBase;
	private Setting mSetting;

	public DeviceStore(Context context) {
		mDataBase = new DataBaseDataSource(context);
	}

	/**
	 * Get a device by id (single device or group).
	 *
	 * @param id
	 *            The device id to get.
	 * @return Device object that contains the device's state or null if the device doesn't exist.
	 */
	public Device getDevice(int deviceId) {
		if (mDevices.containsKey(deviceId)) {
			return new SingleDevice((SingleDevice)mDevices.get(deviceId));
		}
		else if (mGroups.containsKey(deviceId)) {
			return new GroupDevice((GroupDevice)mGroups.get(deviceId));
		}
		return null;
	}


	/**
	 * Get all the devices, including groups.
	 * @return
	 * 		   The list of all devices (SingleDevices and GroupDevices)
	 */
	public List<Device> getAllDevices() {
		ArrayList<Device> devices = new ArrayList<Device>();
		for (Device dev : mDevices.values()) {
			devices.add(dev);
		}
		for (Device dev : mGroups.values()) {
			devices.add(dev);
		}
		return devices;
	}


	/**
	 * Update a device/group name. This new value will be stored also in the database.
	 *
	 * @param deviceId
	 * 			Device/Group id.
	 * @param name
	 * 			New name to be applied.
	 */
	public void updateDeviceName(int deviceId, String name) {
		Device dev = null;
		if (mDevices.containsKey(deviceId)) {
			dev = mDevices.get(deviceId);
			//update in the database
			mDataBase.updateDeviceName(deviceId,name);
		}
		else if (mGroups.containsKey(deviceId)) {
			dev = mGroups.get(deviceId);
			//update in the database
			mDataBase.updateGroupName(deviceId,name);
		}
		if (dev != null) {
			dev.setName(name);
		}
	}

	/**
	 * Get a SingleDevice by device id.
	 * @param deviceId
	 * 		  Device id.
	 * @return
	 * 		  SingleDevice object if it exists. If not the method will return null.
	 */
	public SingleDevice getSingleDevice(int deviceId) {
		if (mDevices.containsKey(deviceId)) {
			return new SingleDevice((SingleDevice)mDevices.get(deviceId));
		}
		return null;
	}

	/**
	 * Get a GroupDevice by device id.
	 * @param deviceId
	 * 		  Group id.
	 * @return
	 * 		  GroupDevice object if it exists. If not the method will return null.
	 */
	public GroupDevice getGroupDevice(int deviceId) {
		if (mGroups.containsKey(deviceId)) {
			return new GroupDevice((GroupDevice)mDevices.get(deviceId));
		}
		return null;
	}


	/**
	 * Get a list of all SingleDevices
	 * @return
	 */
	public List<Device> getAllSingleDevices() {
		ArrayList<Device> devices = new ArrayList<Device>();
		for (Device dev : mDevices.values()) {
			devices.add((Device)dev);
		}
		return devices;
	}

	/**
	 * Get a list of all GroupDevices
	 * @return
	 */
	public List<Device> getAllGroups() {
		ArrayList<Device> groups = new ArrayList<Device>();
		for (Device dev : mGroups.values()) {
			groups.add(dev);
		}
		return groups;
	}


	/**
	 * Create a new GroupDevice or update if it already exists. This will apply also in the database if storeDataBase is true.
	 * @param device
	 * 			GroupDevice to be added.
	 * @param storeDataBase
	 * 			Apply changes into the database.
	 */
	public void addGroupDevice(GroupDevice device, boolean storeDataBase) {
		// Add all states.
		device.setState(new LightState());
		device.setState(new PowState());
		mGroups.put(device.getDeviceId(), new GroupDevice(device));

		// Add into the database.
		if (storeDataBase) {
			mDataBase.createOrUpdateGroup(device,mSetting.getId());
		}
	}

	/**
	 * Remove a device/group by id
	 * @param deviceId
	 * 			device/group id.
	 */
	public void removeDevice(int deviceId) {
		if (mDevices.containsKey(deviceId)) {
			mDevices.remove(deviceId);
			mDataBase.removeSingleDevice(deviceId);
		}
		else if (mGroups.containsKey(deviceId)) {
			mGroups.remove(deviceId);
			mDataBase.removeGroup(deviceId);
		}
	}


	/**
	 * Add a new device (SingleDevice or GroupDevice) and insert into the database.
	 * @param device
	 */
	public void addDevice(Device device) {
		if (device instanceof GroupDevice) {
			addGroupDevice((GroupDevice)device,true);
		}
		else {
			addSingleDevice((SingleDevice)device,true);
		}
	}

	/**
	 * Load all the SingleDevices and GroupDevices from the database.
	 */
	public void loadAllDevices() {
		// Clear devices and groups lists.
		clearDevices();

		// Get SingleDevices and GroupDevices from database.
		ArrayList<SingleDevice> devices = mDataBase.getAllSingleDevices();
		ArrayList<GroupDevice> groups = mDataBase.getAllGroupDevices();

		// Group and device index to be used as last index (we take the higher one in each case).
		int groupIndex = Device.GROUP_ADDR_BASE;
		int deviceIndex = Device.DEVICE_ADDR_BASE;

		// Add devices.
		for (int i=0; i < devices.size(); i++ ) {
			addSingleDevice(devices.get(i),false);
			deviceIndex = Math.max(deviceIndex, devices.get(i).getDeviceId());
		}
		// Add groups.
		for (int i=0; i < groups.size(); i++ ) {
			addGroupDevice(groups.get(i),false);
			groupIndex = Math.max(groupIndex, groups.get(i).getDeviceId());
		}
		mSetting.setLastDeviceIndex(deviceIndex);
		mSetting.setLastGroupIndex(groupIndex);
	}


	/**
	 * Get Setting object
	 * @return
	 */
	public Setting getSetting() {
		return mSetting;
	}

	/**
	 * Set and save in the database (if storeDatabase = true) the current setting to be used.
	 * @param setting
	 * @param storeDatabase
	 */
	public void setSetting(Setting setting, boolean storeDatabase) {
		if (storeDatabase) {
			setting = mDataBase.createSetting(setting);
		}
		mSetting = setting;
	}

	/**
	 * Load setting object by id. This method will clear the list of Single and Group devices. The method {@value #loadAllDevices())}
	 * should be called to get them.
	 * @param settingsID
	 */
	public void loadSetting(int settingsID) {
		mSetting = mDataBase.getSetting(settingsID);

		// clear devices and groups lists.
		clearDevices();
	}

	/**
	 * Insert into the dataBase a new set of Settings value.
	 * @param setting
	 */
	public void addSetting(Setting setting) {
		mSetting = mDataBase.createSetting(setting);

		// clear devices and groups lists.
		clearDevices();
	}


	/**
	 * Clear the single and group devices lists.
	 */
	private void clearDevices() {
		mDevices.clear();
		mGroups.clear();
	}


	/**
	 * Add a new SingleDevice assigning a new name (according with the models supported) if it haven't been assigned yet.
	 * If storeInDataBase is true, it will be also stored in the database.
	 * @param device
	 * 		  SingleDevice to be stored
	 * @param storeInDatabase
	 * 		  Determine if the device should be stored in the database or not.
	 */
	private void addSingleDevice(SingleDevice device, boolean storeInDatabase) {
		String name = device.getName();
		// The human readable device number starting at 1.
		final int deviceNumber = device.getDeviceId() - Device.DEVICE_ADDR_BASE;

		if (device.isModelSupported(LightModelApi.MODEL_NUMBER)) {
			if (name == null) {
				name = "Light "+ deviceNumber;
			}
			device.setState(new LightState());
		}
		if (device.isModelSupported(SwitchModelApi.MODEL_NUMBER)) {
			if (name == null) {
				name = "Switch " + deviceNumber;
			}
		}

		// Set state if necessary.
		if (device.isModelSupported(PowerModelApi.MODEL_NUMBER)) {
			device.setState(new PowState());
		}

		// If after all checks, the device still doesn't have assigned any name, then name it "Device".
		if (name == null) {
			name = "Device " + deviceNumber;
		}
		device.setName(name);

		mDevices.put(device.getDeviceId(), new SingleDevice(device));

		// Add into the database.
		if (storeInDatabase) {
			mDataBase.createOrUpdateSingleDevice(device, mSetting.getId());
		}
	}
}
