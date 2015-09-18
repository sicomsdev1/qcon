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

public interface InfoListener {
    /**
     * Called when firmware information is received from a remote device.
     *
     * @param deviceId
     *            Id of device.
     * @param major
     *            Major version of device.
     * @param minor
     *            Minor version of device.
     * @param success
     *            True if version was received sucessfully, false if there was a timeout (other fields are zero in this
     *            case).
     */
    public void onFirmwareVersion(int deviceId, int major, int minor, boolean success);


    /**
     * Called when VID_PID_Version information is received from a remote device.
     *
     * @param vid
     *            Vendor ID.
     * @param pid
     *            Product ID.
     * @param version
     *            version.
     * @param deviceId
     *            Id of device.
     * @param success
     *            True if version was received sucessfully, false if there was a timeout (other fields are zero in this
     *            case).
     */
    public void onDeviceInfoReceived(byte[] vid, byte[] pid, byte[] version, int deviceId, boolean success);


    /**Called when new device configuration has been received.
     * @param success
     */
    public void onDeviceConfigReceived(boolean success);
}
