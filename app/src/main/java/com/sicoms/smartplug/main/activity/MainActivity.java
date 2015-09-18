package com.sicoms.smartplug.main.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.appevents.AppEventsLogger;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.fragment.HomeFragment;
import com.sicoms.smartplug.main.service.MainService;
import com.sicoms.smartplug.main.service.RealtimeService;
import com.sicoms.smartplug.menu.activity.MypageActivity;
import com.sicoms.smartplug.menu.activity.PlaceActivity;
import com.sicoms.smartplug.menu.activity.PlaceSettingActivity;
import com.sicoms.smartplug.network.bluetooth.BluetoothManager;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.CircleImageView;

import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;


public class MainActivity extends MaterialNavigationDrawer implements HttpBitmapResponseCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = true; // Set this to false to disable logs.

    public static BluetoothManager stBluetoothManager;

    //private GroupNavigationDrawerFragment mNavigationDrawerFragment;

    private Activity mActivity;
    private SPEvent mSPEvent;
    private MainService mService;
    private Thread mRealTimeThread;
    private UserVo mUserVo;
    private boolean isInit = false;

    private RealtimeService mRealtimeService;

    @Override
    public void init(Bundle savedInstanceState) {
        // set header data
        SPActivity.actList.add(this);
        mActivity = this;
        mSPEvent = new SPEvent();
        mService = new MainService();

        stBluetoothManager = new BluetoothManager(mActivity);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), 0x80000000);
        setDrawerBackgroundBitmap(bitmap);

        mUserVo = LoginService.loadLastLoginUser(this);
        setProfileImage();
        setUsername(mUserVo.getUserName());
        setUserEmail(mUserVo.getUserId());
        String userImagePath = SPConfig.FILE_PATH + "_" + mUserVo.getUserId() + "_" + SPConfig.USER_IMAGE_NAME;
        Bitmap userBitmap = BitmapFactory.decodeFile(userImagePath);
        //setFirstAccountPhoto(new BitmapDrawable(getResources(), userBitmap));

        getSupportActionBar().setIcon(R.drawable.logo_sicoms_s);
        // create sections
        this.addSection(newSection("Home", R.drawable.icon_menu_home, HomeFragment.newInstance()));
        this.addSection(newSection("Shop", R.drawable.icon_menu_shop, intentShopBrowser()));
        this.addSection(newSection("My Page", R.drawable.icon_menu_mypage, new Intent(this, MypageActivity.class)));
        this.addSection(newSection("Place", R.drawable.icon_menu_location, new Intent(this, PlaceActivity.class)));
        // create bottom section
        this.addBottomSection(newSection("Place Setting", R.drawable.ic_settings_black_24dp, new Intent(this, PlaceSettingActivity.class)));

        mRealtimeService = new RealtimeService(mActivity);
        mRealTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.d(TAG, "Schedule Realtime Service Run");
                    mRealtimeService.runService();
                    SPUtil.sleep(5 * 1000);
                }
            }
        });
    }

    private void setProfileImage(){
        String imagePath = SPConfig.FILE_PATH + mUserVo.getUserProfileImg();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if( bitmap != null) {
            CircleImageView circleImageView = new CircleImageView(this);
            circleImageView.setImageBitmap(bitmap);
            setFirstAccountPhoto(new BitmapDrawable(getResources(), bitmap));
        } else {
            setDrawerHeaderImage(R.drawable.profile_default);
            if( SPUtil.isNetwork(this)) {
                CommonService service = new CommonService(this);
                service.setOnHttpBitmapResponseCallbacks(this);
                service.requestDownloadImage(new ImgFileVo(mUserVo.getUserProfileImg()));
            }
        }
    }

    private Intent intentShopBrowser(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        PackageManager packageManager = getPackageManager();
        Uri uri = Uri.parse(SPConfig.SHOP_WEB);
        browserIntent.setDataAndType(uri, "text/html");
        List<ResolveInfo> list = packageManager.queryIntentActivities(browserIntent, 0);
        for (ResolveInfo resolveInfo : list) {
            String activityName = resolveInfo.activityInfo.name;

            Log.e("activityName", activityName);
            if (activityName.contains("Browser")) {
                browserIntent =
                        packageManager.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName);
                ComponentName comp =
                        new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                browserIntent.setAction(Intent.ACTION_VIEW);
                browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                browserIntent.setComponent(comp);
                browserIntent.setData(uri);
                if( !isInit){
                    isInit = true;
                    break;
                }
                startActivity(browserIntent);
                break;
            }
        }
        return browserIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealTimeThread.start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        mNavigationDrawerFragment.onSetCallbackManager(requestCode, resultCode, data);
//        mNavigationDrawerFragment.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mRealtimeService.unregisterScannerReceiver();
            unbindService(stBluetoothManager.mServiceConnection);
        } catch (IllegalArgumentException iae){
            iae.printStackTrace();
        }
        if (mRealTimeThread != null) {
            mRealTimeThread.interrupt();
        }
        if (stBluetoothManager != null) {
            stBluetoothManager.disconnectBluetooth();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        mSPEvent.backButtonPressed(this, this);
    }

    @Override
    public void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            SPUtil.saveBitmapImage(fileName, bitmap);
            setProfileImage();
        }
    }
}
