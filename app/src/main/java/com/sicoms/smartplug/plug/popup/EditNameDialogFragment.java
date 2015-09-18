package com.sicoms.smartplug.plug.popup;

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
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.plug.service.PlugHttpService;
import com.sicoms.smartplug.plug.service.PlugDBService;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class EditNameDialogFragment extends DialogFragment implements HttpResponseCallbacks, View.OnClickListener {
    private static final String TAG = EditNameDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private DBHelper mDBHelper;
    private PlugDBService mDBService;
    private PlugHttpService mHttpService;
    private PlugVo mPlugVo;
    private static EditNameFinishCallbacks mListener;

    private EditText mEtPlugName;
    private ImageView mIvDeleteName;
    private ImageView mIvFinishBtn;

    public static EditNameDialogFragment newInstance(PlugVo plugVo, EditNameFinishCallbacks listener) {
        EditNameDialogFragment fragment = new EditNameDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(plugVo));
        fragment.setArguments(args);
        mListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
            mPlugVo = new Gson().fromJson(getArguments().getString(TAG), PlugVo.class);
        if (savedInstanceState != null)
            mPlugVo = new Gson().fromJson(savedInstanceState.getString(TAG), PlugVo.class);

        mActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_popup_editnickname, container, false);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mDBHelper = new DBHelper(mActivity);
        mDBService = new PlugDBService(mActivity);
        mHttpService = new PlugHttpService(mActivity, this);

        mEtPlugName = (EditText) view.findViewById(R.id.et_nickname);
        mIvDeleteName = (ImageView) view.findViewById(R.id.iv_delete_name);
        mIvFinishBtn = (ImageView)view.findViewById(R.id.iv_finish_btn);

        if( mPlugVo != null){
            mEtPlugName.setText(mPlugVo.getPlugName());
            mEtPlugName.setHint(mPlugVo.getPlugName());
        }

        mIvFinishBtn.setOnClickListener(this);
        mIvDeleteName.setOnClickListener(this);

        return view;
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if (resultNum == HttpConfig.HTTP_SUCCESS) {
                    mPlugVo = new Gson().fromJson(responseVo.getJsonStr(), PlugVo.class);
                    if (mPlugVo == null) {
                        Toast.makeText(mActivity, "이름을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mDBService.updateDbDevice(mPlugVo)) {
                        mListener.onEditNameFinish(mPlugVo.getPlugName());
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
        if( SPConfig.IS_TEST){
            if( mDBService.updateDbDevice(mPlugVo)) {
                mListener.onEditNameFinish(mPlugVo.getPlugName());
                Toast.makeText(mActivity, "플러그를 수정하였습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, "플러그를 수정하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_delete_name :
                mEtPlugName.setText("");
                break;
            case R.id.iv_finish_btn :
                String plugName = mEtPlugName.getText().toString();
                if( plugName.equalsIgnoreCase("")){
                    Toast.makeText(mActivity, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPlugVo.setPlugName(mEtPlugName.getText().toString());

                String type = mPlugVo.getNetworkType();
                if(type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                    if (mDBService.updateDbDevice(mPlugVo)) {
                        mListener.onEditNameFinish(mPlugVo.getPlugName());
                        Toast.makeText(mActivity, "이름을 변경하였습니다.", Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    } else {
                        Toast.makeText(mActivity, "이름을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mHttpService.requestUpdateDevice(mPlugVo);
                }
                break;
        }
    }
}