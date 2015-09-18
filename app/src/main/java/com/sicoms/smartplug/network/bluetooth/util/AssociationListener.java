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

import java.util.UUID;

/**
 * Call backs implemented by AssociationFragment, and called by MainActivity.
 * 
 */
public interface AssociationListener {

    /**
     * Called when a new UUID has been discovered from a device.
     * 
     * @param uuid
     *            The full UUID of the device.
     * @param uuidHash
     *            The 31-bit UUID hash of the device.
     * @param rssi
     *            RSSI of the advert that contained the device uuid packet.
     */
    public void newUuid(UUID uuid, int uuidHash, int rssi);

    /**
     * Called when association is complete with a device, or if association failed.
     * 
     * @param success
     *            True if association completed successfully.
     */
    public void deviceAssociated(boolean success);
    
    
    /**
     * Called to change the association bar progress
     * 
     * @param progress Percent between 0-100 of the association process.
     * @param message to be displayed.
     * 
     */
    public void associationProgress(int progress, String message);
}
