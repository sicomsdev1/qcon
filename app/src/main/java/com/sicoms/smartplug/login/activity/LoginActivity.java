package com.sicoms.smartplug.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.event.LoginEvent;
import com.sicoms.smartplug.login.interfaces.LoginResultCallbacks;
import com.sicoms.smartplug.login.service.FacebookServiceManager;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.encrypt.SHA256Util;
import com.sicoms.smartplug.util.SPUtil;

import java.util.List;

/**
 * Created by gudnam on 2015. 5. 26..
 */
public class LoginActivity extends ActionBarActivity implements LoginResultCallbacks, HttpResponseCallbacks, FacebookServiceManager.FacebookLoginCallbacks {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private final String PENDING_ACTION_BUNDLE_KEY = TAG + ":PendingAction";

    private Activity mActivity;

    private LoginEvent mEvent = null;
    private SPEvent mSPEvent = null;
    private LoginService mService = null;
    private PlaceService mPlaceService = null;
    private UserVo mUserVo;

    //private FacebookServiceManager mFBServiceManager;
    //private LoginButton mFBLoginBtn;

    private TextView mEtEmail;
    private TextView mEtPassword;
    private TextView mTvLoginBtn = null;
    private ImageView mIvMembershipBtn = null;
    private ImageView mIvSkipBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mActivity = this;
        mEvent = new LoginEvent(this, this);
        mSPEvent = new SPEvent(mActivity);
        mService = new LoginService(this);
        mService.setOnHttpResponseCallbacks(this);
        mPlaceService = new PlaceService(this);
        mPlaceService.setOnHttpResponseCallbacks(this);
        mUserVo = LoginService.loadLastLoginUser(this);
        //mFBServiceManager = new FacebookServiceManager(this, this);

        mEtEmail = (TextView) findViewById(R.id.et_email);
        mEtPassword = (TextView) findViewById(R.id.et_password);
        mTvLoginBtn = (TextView) findViewById(R.id.tv_login_btn);
        mIvMembershipBtn = (ImageView) findViewById(R.id.iv_membership_btn);
        mIvSkipBtn = (ImageView) findViewById(R.id.iv_skip_btn);
        //mFBLoginBtn = (LoginButton) findViewById(R.id.fb_login_btn);

        mEtEmail.setText(mUserVo.getUserId());
        mTvLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = mEtEmail.getText().toString();
                String userPassword = mEtPassword.getText().toString();
                if (userId.equalsIgnoreCase("")) {
                    Toast.makeText(mActivity, "이메일을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userPassword.equalsIgnoreCase("")) {
                    Toast.makeText(mActivity, "비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cloud Server 에 로그인 정보 요청 (이름, 이미지 받아옴)
                mUserVo = new UserVo(userId, SHA256Util.encryptSha256(userPassword));
                mUserVo.setIsOnOff(true);
                String gcmId = mService.getGCMId(mActivity);
                if (gcmId == null) {
                    Toast.makeText(mActivity, "알림기능이 작동하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mUserVo.setGcmId(gcmId);

                mService.requestLoginMessage(mUserVo);
                SPUtil.showDialog(mActivity);
            }
        });
        mIvMembershipBtn.setOnClickListener(mEvent);
        mIvSkipBtn.setOnClickListener(mEvent);

        if( savedInstanceState != null){
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            //mFBServiceManager.setPendingAction(name);
        }
    }

    private void login(UserVo userVo){
        // Login 정보 저장
        LoginService.saveLastLoginUser(this, userVo);
        mService.insertDbLoginUser(userVo);

        // Place List 가져옴
        mPlaceService.requestSelectPlaceList(userVo);

        SPActivity.intentMainActivity(mActivity);
    }

    @Override
    public void onMembershipResult(UserVo loginVo) {

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
                        if (userVo != null) {
                            login(userVo);
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
            Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //mFBServiceManager.onSetCallbackManager(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString(PENDING_ACTION_BUNDLE_KEY, mFBServiceManager.getPendingActionName());
    }

    @Override
    public void onFacebookLoginResultStatus(int result, UserVo userVo) {
        if( result == FacebookServiceManager.FACEBOOK_LOGIN_SUCCESS){
            if( userVo != null && !userVo.getUserId().equalsIgnoreCase("")) {
                login(userVo);
            }
        }
    }

    @Override
    public void onBackPressed() {
        mSPEvent.backButtonPressed(this, this);
    }
}
