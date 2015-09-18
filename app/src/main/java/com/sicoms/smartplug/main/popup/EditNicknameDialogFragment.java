package com.sicoms.smartplug.main.popup;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.main.service.HomeService;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class EditNicknameDialogFragment extends DialogFragment implements View.OnClickListener, HttpResponseCallbacks {
    private static final String TAG = EditNicknameDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private HomeService mService;
    private UserVo mUserVo;
    private static EditNameFinishCallbacks mCallbacks;

    private EditText mEtNickname;
    private ImageView mIvDeleteName;
    private ImageView mIvFinishBtn;

    public static EditNicknameDialogFragment newInstance(UserVo userVo, EditNameFinishCallbacks callbacks) {
        EditNicknameDialogFragment fragment = new EditNicknameDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(userVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
            mUserVo = new Gson().fromJson(getArguments().getString(TAG), UserVo.class);
        if (savedInstanceState != null)
            mUserVo = new Gson().fromJson(savedInstanceState.getString(TAG), UserVo.class);

        mActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_editnickname, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mService = new HomeService(mActivity);
        mService.setOnHttpResponseCallbacks(this);

        mEtNickname = (EditText) view.findViewById(R.id.et_nickname);
        mIvDeleteName = (ImageView) view.findViewById(R.id.iv_delete_name);
        mIvFinishBtn = (ImageView)view.findViewById(R.id.iv_finish_btn);

        if( mUserVo != null){
            mEtNickname.setText(mUserVo.getUserName());
            mEtNickname.setHint(mUserVo.getUserName());
        }

        mIvFinishBtn.setOnClickListener(this);
        mIvDeleteName.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_delete_name :
                mEtNickname.setText("");
                break;
            case R.id.iv_finish_btn :
                String plugName = mEtNickname.getText().toString();
                if( plugName.equalsIgnoreCase("")){
                    Toast.makeText(mActivity, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mUserVo.setUserName(mEtNickname.getText().toString());

                if(!SPUtil.isNetwork(mActivity)){
                    if (mService.updateDbUser(mUserVo)) {
                        mCallbacks.onEditNameFinish(mUserVo.getUserName());
                        Toast.makeText(mActivity, "이름을 변경하였습니다.", Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    } else {
                        Toast.makeText(mActivity, "이름을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mService.requestUpdateMembershipName(mUserVo);
                }
                break;
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if (resultNum == HttpConfig.HTTP_SUCCESS) {
                    mUserVo = new Gson().fromJson(responseVo.getJsonStr(), UserVo.class);
                    if (mUserVo == null) {
                        Toast.makeText(mActivity, "이름을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mService.updateDbUser(mUserVo)) {
                        mCallbacks.onEditNameFinish(mUserVo.getUserName());
                        Toast.makeText(mActivity, "이름을 변경하였습니다.", Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    } else {
                        Toast.makeText(mActivity, "이름을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mActivity, "이름 변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}