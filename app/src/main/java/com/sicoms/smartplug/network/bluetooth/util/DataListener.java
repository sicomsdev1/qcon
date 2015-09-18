package com.sicoms.smartplug.network.bluetooth.util;

public interface DataListener {
	public void dataReceived(int deviceId, byte[] data);
	public void UITimeout();
	public void dataGroupReceived(int deviceId);
}
