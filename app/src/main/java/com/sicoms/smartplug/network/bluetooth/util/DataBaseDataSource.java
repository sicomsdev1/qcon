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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DataBaseDataSource {

	// Log Tag
	private String TAG = "DataBaseDataSource";

	// Database fields
	private SQLiteDatabase db;
	private MeshSQLHelper dbHelper;

	public DataBaseDataSource(Context context) {
		dbHelper = new MeshSQLHelper(context);
	}	

	/**
	 * Create a setting entry or update if it already exists.
	 * 
	 * @param setting
	 * @return
	 */
	public Setting createSetting(Setting setting) {		
		open();
		ContentValues values = new ContentValues();
		values.put(MeshSQLHelper.SETTINGS_COLUMN_KEY, setting.getNetworkKey());
		values.put(MeshSQLHelper.SETTINGS_COLUMN_NEXT_DEVICE_INDEX,setting.getLastDeviceIndex());
		values.put(MeshSQLHelper.SETTINGS_COLUMN_NEXT_GROUP_INDEX,setting.getLastGroupIndex());
		values.put(MeshSQLHelper.SETTINGS_COLUMN_AUTH_REQUIRED,setting.isAuthRequired());

		long id;
		if (setting.getId() != Setting.UKNOWN_ID) {
			values.put(MeshSQLHelper.SETTINGS_COLUMN_ID, setting.getId());
			id = db.replace(MeshSQLHelper.TABLE_SETTINGS, null, values);
		} else {
			id = db.insert(MeshSQLHelper.TABLE_SETTINGS, null, values);
		}

		close();

		if (id == -1) {
			// error, return null;
			return null;
		} else {
			setting.setId((int) id);
			return setting;
		}
	}

	/**
	 * Get single setting entry by id.
	 * 
	 * @param id
	 * @return
	 */
	public Setting getSetting(int id) {
		String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_SETTINGS
				+ " WHERE " + MeshSQLHelper.SETTINGS_COLUMN_ID + " = " + id;

		open();
		Cursor c = db.rawQuery(selectQuery, null);

		if (c != null && c.moveToFirst()) {
			Setting setting = new Setting();
			setting.setId(c.getInt(c
					.getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_ID)));
			setting.setNetworkKey(c.getString(c
					.getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_KEY)));
			setting.setLastDeviceIndex(c.getInt(c
					.getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_NEXT_DEVICE_INDEX)));
			setting.setLastGroupIndex(c.getInt(c
					.getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_NEXT_GROUP_INDEX)));
			setting.setAuthRequired((c.getInt(c
					.getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_AUTH_REQUIRED))>0));
			close();
			return setting;
		} else {
			close();
			return null;
		}
	}

	/**
	 * Get the list of SingleDevices stored in the database.
	 * 
	 * @return
	 */
	public ArrayList<SingleDevice> getAllSingleDevices() {
		open();
		db.beginTransaction();
		ArrayList<SingleDevice> devices = new ArrayList<SingleDevice>();
		String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_DEVICES;
		Cursor devicesCursor = db.rawQuery(selectQuery, null);
		while (devicesCursor.moveToNext()) {
			SingleDevice device = new SingleDevice(
					devicesCursor.getInt(devicesCursor
							.getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_ID)),
					devicesCursor.getString(devicesCursor
							.getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_NAME)),
					devicesCursor.getInt(devicesCursor
							.getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_HASH)),
					devicesCursor.getLong(devicesCursor
							.getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_LOW)),
					devicesCursor.getLong(devicesCursor
							.getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_HIGH)));
			device.setMinimumSupportedGroups(devicesCursor.getInt(devicesCursor
					.getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_GROUPS_SUPPORTED)));

			String query = "SELECT " + MeshSQLHelper.MODELS_COLUMN_GROUP_ID
					+ " FROM " + MeshSQLHelper.TABLE_MODELS + " WHERE "
					+ MeshSQLHelper.MODELS_COLUMN_DEVICE_ID + " ='"
					+ device.getDeviceId() + "'";
			Cursor groupsCursor = db.rawQuery(query, null);

			for (int i = 0; groupsCursor.moveToNext(); i++) {
				device.setGroupId(i, groupsCursor.getInt(groupsCursor
						.getColumnIndex(MeshSQLHelper.MODELS_COLUMN_GROUP_ID)));
			}
			devices.add(device);
		}
		db.endTransaction();
		close();

		return devices;
	}

	/**
	 * Get the list of SingleDevices stored in the database.
	 * @return
	 */
	public ArrayList<GroupDevice> getAllGroupDevices() {
		open();
		ArrayList<GroupDevice> groups = new ArrayList<GroupDevice>();
		String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_GROUPS;
		Cursor groupsCursor = db.rawQuery(selectQuery, null);
		while (groupsCursor.moveToNext()) {
			GroupDevice group = new GroupDevice(
					groupsCursor.getInt(groupsCursor
							.getColumnIndex(MeshSQLHelper.GROUPS_COLUMN_ID)),
					groupsCursor.getString(groupsCursor
							.getColumnIndex(MeshSQLHelper.GROUPS_COLUMN_NAME)));
			groups.add(group);
		}
		close();
		return groups;
	}

	
	/**
	 * Create a GroupDevice or update if it already exists in the database.
	 * @param group
	 * @param settingsID
	 * @return
	 */
	public GroupDevice createOrUpdateGroup(GroupDevice group, int settingsID) {
		open();
		ContentValues values = new ContentValues();
		values.put(MeshSQLHelper.GROUPS_COLUMN_ID, group.getDeviceId());
		values.put(MeshSQLHelper.GROUPS_COLUMN_NAME, group.getName());
		values.put(MeshSQLHelper.GROUPS_COLUMN_SETTINGS_ID, settingsID);

		// insert row and close db
		long id = db.replace(MeshSQLHelper.TABLE_GROUPS, null, values);
		close();

		if (id == -1) {
			// error, return null;
			return null;
		} else {
			group.setDeviceId((int) id);
			return group;
		}
	}

	/**
	 * Create a SingleDevice or update if it already exists in the database.
	 * @param device
	 * @param settingsID
	 * @return
	 */
	public boolean createOrUpdateSingleDevice(SingleDevice device,
			int settingsID) {
		open();
		ContentValues values = new ContentValues();
		values.put(MeshSQLHelper.DEVICES_COLUMN_ID, device.getDeviceId());
		values.put(MeshSQLHelper.DEVICES_COLUMN_NAME, device.getName());
		values.put(MeshSQLHelper.DEVICES_COLUMN_GROUPS_SUPPORTED,
				device.getMinimumSupportedGroups());
		values.put(MeshSQLHelper.DEVICES_COLUMN_HASH, device.getUuidHash());
		values.put(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_LOW,
				device.getModelSupportBitmapLow());
		values.put(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_HIGH,
				device.getModelSupportBitmapHigh());
		values.put(MeshSQLHelper.DEVICES_COLUMN_SETTINGS_ID, settingsID);

		// insert row and close db
		long id = db.replace(MeshSQLHelper.TABLE_DEVICES, null, values);
		close();

		if (id == -1) {
			// Error so return false.
			close();
			return false;
		} 
		else {
			removeAllModels(device.getDeviceId());
			for (int i = 0; i < device.getGroupMembership().size(); i++) {
				createOrUpdateModel(device.getDeviceId(), device.getGroupMembership().get(i).intValue());
			}
			close();
			return true;
		}
	}

	/**
	 * Update the device name of an existing device of the data base.
	 * @param deviceId
	 * @param name
	 */
	public void updateDeviceName(int deviceId, String name) {
		open();
		ContentValues values = new ContentValues();
		values.put(MeshSQLHelper.DEVICES_COLUMN_NAME, name);

		db.update(MeshSQLHelper.TABLE_DEVICES, values,
				MeshSQLHelper.DEVICES_COLUMN_ID + "=" + deviceId, null);
		close();

	}

	/**
	 * Update the group name of an existing device of the group base.
	 * @param deviceId
	 * @param name
	 */
	public void updateGroupName(int deviceId, String name) {
		open();
		ContentValues values = new ContentValues();
		values.put(MeshSQLHelper.DEVICES_COLUMN_NAME, name);

		db.update(MeshSQLHelper.TABLE_GROUPS, values,
				MeshSQLHelper.DEVICES_COLUMN_ID + "=" + deviceId, null);
		close();

	}

	/**
	 * Remove all the models that a device has.
	 * @param deviceID
	 */
	public void removeAllModels(int deviceID) {
		open();
		db.delete(MeshSQLHelper.TABLE_MODELS,
				MeshSQLHelper.MODELS_COLUMN_DEVICE_ID + "=" + deviceID, null);
		close();
	}

	/**
	 * Create a model or update if it already exists in the database.
	 * @param deviceID
	 * @param groupID
	 */
	public void createOrUpdateModel(int deviceID, int groupID) {
		open();
		ContentValues values = new ContentValues();
		values.put(MeshSQLHelper.MODELS_COLUMN_DEVICE_ID, deviceID);
		values.put(MeshSQLHelper.MODELS_COLUMN_GROUP_ID, groupID);

		// insert row and close db
		db.replace(MeshSQLHelper.TABLE_MODELS, null, values);

		close();
	}

	/**
	 * Remove a SingleDevice from the database by device id.
	 * @param deviceId
	 */
	public void removeSingleDevice(int deviceId) {
		open();
		db.delete(MeshSQLHelper.TABLE_DEVICES, MeshSQLHelper.DEVICES_COLUMN_ID
				+ "=" + deviceId, null);
		close();
	}

	/**
	 * Remove a GroupDevice from the database by device id.
	 * @param groupId
	 */
	public void removeGroup(int groupId) {
		open();
		db.delete(MeshSQLHelper.TABLE_GROUPS, MeshSQLHelper.GROUPS_COLUMN_ID
				+ "=" + groupId, null);
		close();
	}
	
	/**
	 * Open the database
	 * 
	 * @throws SQLException
	 */
	private void open() throws SQLException {		
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * Close the database
	 */
	private void close() {		
		dbHelper.close();
	}
}
