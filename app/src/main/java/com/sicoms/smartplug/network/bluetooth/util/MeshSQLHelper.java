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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * MeshSQLHelper class contains all the methods to perform database operations
 * like opening connection, closing connection, insert, update, read, delete and
 * other things.
 */
public class MeshSQLHelper extends SQLiteOpenHelper {

	// Logcat tag.
	private static final String TAG = "MeshSQLHelper";

	// Database version and name.
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "mesh.db";

	// Database table names.
	static final String TABLE_SETTINGS = "settings";
	static final String TABLE_DEVICES = "devices";
	static final String TABLE_GROUPS = "groups";
	static final String TABLE_MODELS = "models";

	// Settings Table - Columns names.
	public static final String SETTINGS_COLUMN_ID = "id";
	public static final String SETTINGS_COLUMN_KEY = "networkKey";
	public static final String SETTINGS_COLUMN_AUTH_REQUIRED = "authRequired";
	public static final String SETTINGS_COLUMN_NEXT_DEVICE_INDEX = "nextDeviceIndex";
	public static final String SETTINGS_COLUMN_NEXT_GROUP_INDEX = "nextGroupIndex";

	// Devices Table - Columns names.
	public static final String DEVICES_COLUMN_ID = "id";
	public static final String DEVICES_COLUMN_HASH = "hash";
	public static final String DEVICES_COLUMN_NAME = "name";
	public static final String DEVICES_COLUMN_GROUPS_SUPPORTED = "groupsSupported";
	public static final String DEVICES_COLUMN_MODELSUPPORT_LOW = "modelSupportL";
	public static final String DEVICES_COLUMN_MODELSUPPORT_HIGH = "modelSupportH";
	public static final String DEVICES_COLUMN_SETTINGS_ID = "settingsID";

	// Groups Table - Columns names.
	public static final String GROUPS_COLUMN_ID = "id";
	public static final String GROUPS_COLUMN_NAME = "name";
	public static final String GROUPS_COLUMN_SETTINGS_ID = "settingsID";

	// Models Table - Columns names.
	public static final String MODELS_COLUMN_DEVICE_ID = "deviceID";
	public static final String MODELS_COLUMN_GROUP_ID = "groupID";

	// Settings table create statement.
	private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE "
			+ TABLE_SETTINGS + "(" + SETTINGS_COLUMN_ID
			+ " INTEGER PRIMARY KEY autoincrement,"
			+ SETTINGS_COLUMN_AUTH_REQUIRED + " BOOLEAN," + SETTINGS_COLUMN_KEY
			+ " TEXT," + SETTINGS_COLUMN_NEXT_DEVICE_INDEX + " INTEGER,"
			+ SETTINGS_COLUMN_NEXT_GROUP_INDEX + " INTEGER" + ")";

	// Devices table create statement.
	private static final String CREATE_TABLE_DEVICES = "CREATE TABLE "
			+ TABLE_DEVICES + "(" + DEVICES_COLUMN_ID + " INTEGER PRIMARY KEY,"
			+ DEVICES_COLUMN_HASH + " INTEGER," + DEVICES_COLUMN_NAME
			+ " TEXT," + DEVICES_COLUMN_GROUPS_SUPPORTED + " INTEGER,"
			+ DEVICES_COLUMN_SETTINGS_ID + " INTEGER,"
			+ DEVICES_COLUMN_MODELSUPPORT_LOW + " INTEGER,"
			+ DEVICES_COLUMN_MODELSUPPORT_HIGH + " INTEGER" + ")";

	// Models table create statement.
	private static final String CREATE_TABLE_MODELS = "CREATE TABLE "
			+ TABLE_MODELS + "(" + MODELS_COLUMN_DEVICE_ID
			+ " INTEGER NOT NULL," + MODELS_COLUMN_GROUP_ID
			+ " INTEGER NOT NULL," + "PRIMARY KEY (" + MODELS_COLUMN_DEVICE_ID
			+ "," + MODELS_COLUMN_GROUP_ID + ")" + ")";

	// Groups table create statement.
	private static final String CREATE_TABLE_GROUPS = "CREATE TABLE "
			+ TABLE_GROUPS + "(" + GROUPS_COLUMN_ID + " INTEGER PRIMARY KEY,"
			+ GROUPS_COLUMN_NAME + " TEXT," + GROUPS_COLUMN_SETTINGS_ID
			+ " INTEGER" + ")";

	public MeshSQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		// Create required tables.
		database.execSQL(CREATE_TABLE_SETTINGS);
		database.execSQL(CREATE_TABLE_DEVICES);
		database.execSQL(CREATE_TABLE_MODELS);
		database.execSQL(CREATE_TABLE_GROUPS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");

		// On upgrade drop older tables.
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODELS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);

		// create new tables
		onCreate(db);
	}

}
