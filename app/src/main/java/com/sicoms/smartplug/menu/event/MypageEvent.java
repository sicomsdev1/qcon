package com.sicoms.smartplug.menu.event;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.menu.service.MypageService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.util.encrypt.SHA256Util;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 6. 1..
 */
public class MypageEvent implements View.OnClickListener, HttpResponseCallbacks {

    private Context mContext;
    private EditNameFinishCallbacks mCallbacks;
    private PictureMenuCallbacks mPictureCallbacks;
    private UserVo mUserVo;

    public MypageEvent(Context context, UserVo userVo){
        mContext = context;
        mUserVo = userVo;
    }
    public void setEditNameFinishCallbacks(EditNameFinishCallbacks callbacks){
        mCallbacks = callbacks;
    }
    public void setPictureMenuCallbacks(PictureMenuCallbacks callbacks){
        mPictureCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        ViewGroup rootView = (ViewGroup) v.getRootView();
        switch (v.getId()){
            case R.id.iv_camera_btn :
                SPFragment.intentPictureMenuFragmentDialog((Activity) mContext, mPictureCallbacks, SPConfig.PICTURE_MENU_TYPE_HOME);
                break;
            case R.id.iv_nickname_btn :
                SPFragment.intentEditNicknameFragmentDialog((Activity) mContext, mUserVo, mCallbacks);
                break;
            case R.id.rl_change_password_btn :
                RelativeLayout rl_edit_password = (RelativeLayout)rootView.findViewById(R.id.rl_edit_password);
                ImageView iv_arrow_btn = (ImageView) rootView.findViewById(R.id.iv_arrow_btn);

                if( rl_edit_password.getVisibility() == View.VISIBLE) {
                    rl_edit_password.setVisibility(View.GONE);
                    iv_arrow_btn.setSelected(false);
                } else {
                    rl_edit_password.setVisibility(View.VISIBLE);
                    iv_arrow_btn.setSelected(true);
                }
                break;
            case R.id.rl_logout_btn :
                LoginService loginService = new LoginService(mContext);
                loginService.setOnHttpResponseCallbacks(this);
                loginService.requestLogout();
                break;
            case R.id.iv_finish_btn :
                EditText etCurrentPassword = (EditText) rootView.findViewById(R.id.et_current_password);
                EditText etNewPassword = (EditText) rootView.findViewById(R.id.et_new_password);
                EditText etConfirmPassword = (EditText) rootView.findViewById(R.id.et_confirm_password);

                UserVo loginVo = LoginService.loadLastLoginUser(mContext);

                String editPassword = SHA256Util.encryptSha256(etCurrentPassword.getText().toString());
                if( !loginVo.getPassword().equalsIgnoreCase(editPassword)){
                    SPUtil.showToast(mContext, "비밀번호가 틀렸습니다.");
                    return;
                }
                if( !etNewPassword.getText().toString().equalsIgnoreCase(etConfirmPassword.getText().toString())){
                    SPUtil.showToast(mContext, "새 비밀번호가 일치하지 않습니다.");
                    return;
                }

                String newPassword = SHA256Util.encryptSha256(etNewPassword.getText().toString());
                loginVo.setPassword(newPassword);
                MypageService service = new MypageService(mContext);
                service.setOnHttpResponseCallbacks(this);
                service.requestUpdatePassword(loginVo);
                SPUtil.showDialog(mContext);
                break;
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS){
            try {
                if( value.equalsIgnoreCase("")){
                    Toast.makeText(mContext, "로그아웃 하였습니다.", Toast.LENGTH_SHORT).show();
                    UserVo userVo = LoginService.loadLastLoginUser(mContext);
                    userVo.setPassword("");
                    LoginService.saveLastLoginUser(mContext, userVo);
                    SPActivity.intentLoginActivity((Activity) mContext);
                } else {
                    HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                    int resultNum = Integer.parseInt(responseVo.getResult());
                    if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_MEMBERSHIP_PASSWORD) {
                        if (resultNum == HttpConfig.HTTP_SUCCESS) {
                            LoginService loginService = new LoginService(mContext);
                            loginService.setOnHttpResponseCallbacks(this);
                            loginService.requestLogout();
                            SPUtil.dismissDialog();
                        } else {
                            SPUtil.showToast(mContext, "비밀번호 변경 요청에 실패하였습니다.");
                        }

                    }
                }
            } catch (JsonParseException jpe) {
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
