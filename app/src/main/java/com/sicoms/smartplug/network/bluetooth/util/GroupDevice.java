package com.sicoms.smartplug.network.bluetooth.util;

import org.json.JSONException;
import org.json.JSONObject;

public class GroupDevice extends Device {

	public GroupDevice(int deviceId, String name) {
		super(deviceId, name);
	}

	public GroupDevice(GroupDevice other) {
		super(other);
	}
}
