package com.sicoms.smartplug.login.popup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.gcm.GcmService;
import com.sicoms.smartplug.login.interfaces.LoginResultCallbacks;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.encrypt.SHA256Util;

public class MembershipDialogFragment extends DialogFragment implements HttpResponseCallbacks {

    private static final String TAG = MembershipDialogFragment.class.getSimpleName();
    private static LoginResultCallbacks mCallbacks;

    private Context mContext;
    private LoginService mService;

    private TextView mTvMembershipName;
    private TextView mTvMembershipEmail;
    private TextView mTvPassword;
    private TextView mTvPasswordConfirm;
    private TextView mTvLoginBtn;
    private TextView mTvTerms;
    private TextView mTvPersonalInfo;

    public static MembershipDialogFragment newInstance(LoginResultCallbacks callbacks) {
        MembershipDialogFragment fragment = new MembershipDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_membership, container, false);
        getDialog().setCanceledOnTouchOutside(true);

        mService = new LoginService(mContext);
        mService.setOnHttpResponseCallbacks(this);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mTvMembershipName = (TextView) view.findViewById(R.id.tv_membership_name);
        mTvMembershipEmail = (TextView) view.findViewById(R.id.tv_membership_email);
        mTvPassword = (TextView) view.findViewById(R.id.tv_password);
        mTvPasswordConfirm = (TextView) view.findViewById(R.id.tv_password_confirm);
        mTvLoginBtn = (TextView) view.findViewById(R.id.tv_login_btn);
        mTvTerms = (TextView) view.findViewById(R.id.tv_terms);
        mTvPersonalInfo = (TextView) view.findViewById(R.id.tv_personal_info);

        mTvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPFragment.intentTermsFragmentDialog((Activity) mContext);
            }
        });
        mTvPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPFragment.intentPersonalInfoFragmentDialog((Activity) mContext);
            }
        });
        mTvLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTvMembershipName.getText().toString();
                String email = mTvMembershipEmail.getText().toString();
                String password = mTvPassword.getText().toString();
                String passwordConfirm = mTvPasswordConfirm.getText().toString();
                if( name.equalsIgnoreCase("")){
                    Toast.makeText(mContext, "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if( email.equalsIgnoreCase("")){
                    Toast.makeText(mContext, "이메일을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if( password.equalsIgnoreCase("")){
                    Toast.makeText(mContext, "비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if( passwordConfirm.equalsIgnoreCase("")){
                    Toast.makeText(mContext, "비밀번호 확인을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserVo loginVo = new UserVo(email, SHA256Util.encryptSha256(password), name, 1, SPConfig.USER_DEFAULT_IMAGE_NAME, true);
                GcmService gcmService = new GcmService(mContext);
                String gcmId = gcmService.registGCM();
                if( gcmId == ""){
                    Toast.makeText(mContext, "회원가입에 실패하였습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginVo.setGcmId(gcmId);

                mService.requestInsertMembership(loginVo);
            }
        });

        return view;
    }


    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    UserVo userVo = new Gson().fromJson(responseVo.getJsonStr(), UserVo.class);
                    if (userVo != null) {
                        Toast.makeText(mContext, userVo.getUserName() + "님 회원가입을 축하드립니다!", Toast.LENGTH_SHORT).show();
                        mCallbacks.onMembershipResult(userVo);
                        dismiss();
                    }
                } else {
                    Toast.makeText(mContext, "회원가입에 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
