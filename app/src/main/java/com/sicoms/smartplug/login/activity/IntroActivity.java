package com.sicoms.smartplug.login.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.facebook.FacebookSdk;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 20..
 */
public class IntroActivity extends ActionBarActivity implements HttpResponseCallbacks {

    private static final String TAG = IntroActivity.class.getSimpleName();

    private LoginService mService;
    private PlaceService mPlaceService;
    private UserVo mUserVo;

    private void initialize(){
        SPConfig.FILE_PATH = getFilesDir().getAbsolutePath() + SPConfig.FILE_PATH;
        // Create Default Image
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    File placeFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.PLACE_DEFAULT_IMAGE_NAME);
//                    if( !placeFile.exists()){
//                        OutputStream out = new FileOutputStream(placeFile);
//                        Bitmap placeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.full_bg01);
//                        placeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                    }

                    File profileFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.USER_DEFAULT_IMAGE_NAME);
                    if( !profileFile.exists()){
                        OutputStream out = new FileOutputStream(profileFile);
                        Bitmap groupBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_default);
                        groupBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }

//                    File groupFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.GROUP_DEFAULT_IMAGE_NAME);
//                    if( !groupFile.exists()){
//                        OutputStream out = new FileOutputStream(groupFile);
//                        Bitmap groupBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_place_00_on);
//                        groupBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                    }

//                    File plugFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.PLUG_DEFAULT_IMAGE_NAME);
//                    if( !plugFile.exists()){
//                        OutputStream out = new FileOutputStream(plugFile);
//                        Bitmap plugBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dppbg_00);
//                        plugBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_intro);
        mService = new LoginService(this);
        mService.setOnHttpResponseCallbacks(this);
        mPlaceService = new PlaceService(this);
        mPlaceService.setOnHttpResponseCallbacks(this);
        mUserVo = new UserVo();

        initialize();

        if( !autologin()){
            mUserVo = new UserVo(mUserVo.getUserId(), "", "", 0, "", true);
            LoginService.saveLastLoginUser(this, mUserVo);

            SPActivity.intentLoginActivity(this);
        }
    }

    private boolean autologin(){
        mUserVo = LoginService.loadLastLoginUser(this);
        if( mUserVo == null){
            return false;
        }
        if( mUserVo.getUserId() == ""){
            return false;
        }
        if( mUserVo.getPassword() == ""){
            return false;
        }
        String gcmId = mService.getGCMId(this);
        if( gcmId == null){
            SPUtil.showToast(this, "로그인에 실패하였습니다.");
            return false;
        }
        mUserVo.setGcmId(gcmId);
        //login(mUserVo); // TEST
        mService.requestLoginMessage(mUserVo);

        return true;
    }

    private void login(UserVo userVo){
        // Login 정보 저장
        LoginService.saveLastLoginUser(this, userVo);
        mService.insertDbLoginUser(userVo);

        // Place List 가져옴
        mPlaceService.requestSelectPlaceList(userVo);

        SPActivity.intentMainActivity(this);
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
            int resultNum = Integer.parseInt(responseVo.getResult());
            if( resultNum == HttpConfig.HTTP_SUCCESS) {
                if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_AUTH_LOGIN) {
                    try {
                        UserVo userVo = new Gson().fromJson(responseVo.getJsonStr(), UserVo.class);
                        userVo.setPassword(mUserVo.getPassword());
                        mUserVo = userVo;
                        if (mUserVo != null) {
                            login(mUserVo);
                        }
                    } catch (JsonParseException jpe) {
                        jpe.printStackTrace();
                    }
                } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_PLACE_LIST) {
                    try {
                        List<PlaceVo> placeVoList = new Gson().fromJson(value, new TypeToken<List<PlaceVo>>() {
                        }.getType());

                        if (placeVoList != null) {
                            mPlaceService.updateDbPlaceList(placeVoList); // Local DB 에 플레이스 리스트 업데이트
                        }

                    } catch (JsonParseException jpe) {
                        jpe.printStackTrace();
                    } catch (NumberFormatException nfe){
                        nfe.printStackTrace();
                    }
                }
            } else {

            }
        } else {
            // 로그인 실패시 라스트 로그인 정보 초기화
            mUserVo = new UserVo(mUserVo.getUserId(), "", "", 0, "", true);
            LoginService.saveLastLoginUser(this, mUserVo);

            SPActivity.intentLoginActivity(this);
        }
    }
}
