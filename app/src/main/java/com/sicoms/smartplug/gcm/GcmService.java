package com.sicoms.smartplug.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.util.PreferenceUtil;

import java.io.IOException;

public class GcmService {

	public final static String SENDER_ID = "244190355840";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private GoogleCloudMessaging gcm;
	private String regId;

	private Context mContext;
	
	public GcmService(Context context){
		mContext = context;
	}

	public String registGCM() {
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(mContext);
			regId = getRegistrationId();

			if (TextUtils.isEmpty(regId))
				registerInBackground();
		} else {
			Log.i("GCMService",
					"|No valid Google Play Services APK found.|");
		}

		return regId;
	}

	// google play service가 사용가능한가
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mContext);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				//GooglePlayServicesUtil.getErrorDialog(resultCode, mContext,
				//		PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i("GCMService",
						"|This device is not supported.|");
				//mContext.finish();
			}
			return false;
		}
		return true;
	}

	// registration id를 가져온다.
	private String getRegistrationId() {
		String registrationId = PreferenceUtil.instance(mContext.getApplicationContext()).regId();
		if (TextUtils.isEmpty(registrationId)) {
			Log.i("GCMService",
					"|Registration not found.|");
			return "";
		}
		int registeredVersion = PreferenceUtil.instance(mContext.getApplicationContext()).appVersion();
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion) {
			Log.i("GCMService",
					"|App version changed.|");
			return "";
		}
		return registrationId;
	}

	// app version을 가져온다. 뭐에 쓰는건지는 모르겠다.
	private int getAppVersion() {
		try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	// gcm 서버에 접속해서 registration id를 발급받는다.
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging
								.getInstance(mContext.getApplicationContext());
					}
 					regId = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regId;

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(regId);
				} catch (IOException ex) {
					ex.printStackTrace();
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				} catch( Exception ex){
					ex.printStackTrace();
					msg = "Error :" + ex.getMessage();
				}

				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i("GCMService", "|" + msg + "|");
			}
		}.execute(null, null, null);
	}

	// registraion id를 preference에 저장한다.
	private void storeRegistrationId(String regId) {
		int appVersion = getAppVersion();
		Log.i("GCMService", "|"
				+ "Saving regId on app version " + appVersion + "|");
		PreferenceUtil.instance(mContext.getApplicationContext()).putRedId(regId);
		PreferenceUtil.instance(mContext.getApplicationContext()).putAppVersion(
				appVersion);
		SPConfig.SP_GCM_ID = regId;
		UserVo loginVo = LoginService.loadLastLoginUser(mContext);
		loginVo.setGcmId(SPConfig.SP_GCM_ID);
		LoginService.saveLastLoginUser(mContext, loginVo);
	}
}
