/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2015
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

/**
 * This class represents the configuration
 */
public class Setting {
	
	public static int UKNOWN_ID = -1;

	private int id = UKNOWN_ID;
	private String networkKey;
	private int lastGroupIndex = Device.GROUP_ADDR_BASE;
	private int lastDeviceIndex = Device.DEVICE_ADDR_BASE;
	private boolean authRequired = false;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNetworkKey() {
		return networkKey;
	}
	public void setNetworkKey(String networkKey) {
		this.networkKey = networkKey;
	}
	public int getLastGroupIndex() {
		return lastGroupIndex;
	}
	public void setLastGroupIndex(int lastGroupIndex) {
		this.lastGroupIndex = lastGroupIndex;
	}
	public int getLastDeviceIndex() {
		return lastDeviceIndex;
	}
	public void setLastDeviceIndex(int lastDeviceIndex) {
		this.lastDeviceIndex = lastDeviceIndex;
	}
	public boolean isAuthRequired() {
		return authRequired;
	}
	public void setAuthRequired(boolean authRequired) {
		this.authRequired = authRequired;
	}
	
	/**
	 * Increment the last group index and return the value.
	 * @return The next valid group index.
	 */
	public int getNextGroupIndex() 	{
		return ++lastGroupIndex;
	}
	
	
}
