package com.sicoms.smartplug.plug.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.bluetooth.BLConfig;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.DialogFinishCallbacks;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class BLSecurityDialogFragment extends DialogFragment implements HttpResponseCallbacks {
    private static final String TAG = BLSecurityDialogFragment.class.getSimpleName();

    private static DialogFinishCallbacks mListener;
    private Context mContext;
    private RegDeviceVo mRegDeviceVo;
    private PlaceSettingService mService;

    public static BLSecurityDialogFragment newInstance(RegDeviceVo regDeviceVo, DialogFinishCallbacks listener) {
        BLSecurityDialogFragment fragment = new BLSecurityDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(regDeviceVo));
        fragment.setArguments(args);
        mListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
            mRegDeviceVo = new Gson().fromJson(getArguments().getString(TAG), RegDeviceVo.class);
        if (savedInstanceState != null)
            mRegDeviceVo = new Gson().fromJson(savedInstanceState.getString(TAG), RegDeviceVo.class);

        View view = inflater.inflate(R.layout.fragment_popup_bl_security, container, false);
        mContext = getActivity();

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mService = new PlaceSettingService(mContext);
        mService.setOnHttpResponseCallbacks(this);

        String phrase = MainActivity.stBluetoothManager.getNetworkKeyPhrase();
        boolean auth = MainActivity.stBluetoothManager.isAuthRequired();
        ImageView okBtn = (ImageView) view.findViewById(R.id.network_assocoiation_ok);
        final EditText passPhraseView = (EditText) view.findViewById(R.id.et_password);
        if (phrase != null) {
            passPhraseView.setText(phrase);
        }
        okBtn.setClickable(true);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtil.showDialog(mContext);
                String password = passPhraseView.getText().toString();
                if (password.trim().length() < 4) {
                    SPUtil.showToast(mContext, mContext.getString(R.string.password_more_than_four));
                    return;
                } else if(password.equalsIgnoreCase(BLConfig.BL_DEFAULT_SECURITY_PASSWORD)){
                    SPUtil.showToast(mContext, mContext.getString(R.string.password_not_default));
                }
                // Hide soft keyboard.
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(passPhraseView.getWindowToken(), 0);

                PlaceSettingVo placeSettingVo = new PlaceSettingVo(PlaceService.loadLastPlace(mContext).getPlaceId(), SPConfig.PLACE_SETTING_BL_PASSWORD, password);
                mService.requestUpdateBLPassword(placeSettingVo);
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if( getDialog() != null) {
            getDialog().dismiss();
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        SPUtil.dismissDialog();
        if( result == HttpConfig.HTTP_SUCCESS){
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_PLACE_BL_PASSWORD) {
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        PlaceSettingVo settingVo = new Gson().fromJson(responseVo.getJsonStr(), PlaceSettingVo.class);
                        PlaceSettingService service = new PlaceSettingService(mContext);
                        service.updateDbBLPassword(settingVo);
                        MainActivity.stBluetoothManager.setSecurity(settingVo.getSetVal(), false);
                        mListener.onDialogFinishCallbacks(TAG, mRegDeviceVo);
                        getDialog().dismiss();

                        SPUtil.showToast(mContext, "블루투스 비밀번호를 변경하였습니다.");
                    } else if( resultNum == -1) {
                        SPUtil.showToast(mContext, "블루투스 비밀번호 설정은 관리자만 설정 가능합니다.");
                    } else {
                        SPUtil.showToast(mContext, "블루투스 비밀번호 변경 요청에 실패하였습니다.");
                    }

                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            SPUtil.showToast(mContext, "서버 연결에 실패하였습니다.");
        }
    }
}