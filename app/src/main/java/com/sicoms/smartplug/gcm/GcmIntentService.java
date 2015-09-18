package com.sicoms.smartplug.gcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.activity.IntroActivity;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.util.NotificationUtil;

public class GcmIntentService extends IntentService
{
	public static final int NOTIFICATION_ID = 1;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
   protected void onHandleIntent(Intent intent)
   {
		SharedPreferences preference = getSharedPreferences("setting", 0);
		boolean isAlarm = preference.getBoolean("isAlarm", true);
		if (!isAlarm)
			return;
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			Log.i("GcmBroadcastReceiver", "Received: " + extras.toString());
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				// sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				// sendNotification("Deleted messages on server: " +
				// extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				String title = intent.getStringExtra("title");
				String message = intent.getStringExtra("message");
				int num = 0;
				try {
					num = Integer.parseInt(intent.getStringExtra("num"));
				} catch (NumberFormatException nfe){
					nfe.printStackTrace();
					return;
				}
				String data = intent.getStringExtra("data");

				GCMReceiveDataService service = new GCMReceiveDataService(getApplicationContext());
				service.setData(num, data);
			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
   }
	
	private void test(){

		SharedPreferences preference = getSharedPreferences("test", 0);
		SharedPreferences.Editor edit = preference.edit();
		edit.putBoolean("isGCM", true);
		edit.commit();
	}
}