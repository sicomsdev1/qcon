package com.sicoms.smartplug.menu.event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.interfaces.PlaceSettingResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 6. 1..
 */
public class PlaceSettingEvent implements View.OnClickListener, HttpResponseCallbacks {

    private Context mContext;
    private PlaceSettingResultCallbacks mCallbacks;

    public PlaceSettingEvent(Context context){
        mContext = context;
    }

    public void setOnPlaceSettingResultCallbacks(PlaceSettingResultCallbacks callbacks){
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        ViewGroup rootView = (ViewGroup) v.getRootView();
        switch (v.getId()){
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
            case R.id.iv_finish_btn :
                if( !MainActivity.stBluetoothManager.isConnected()){
                    SPUtil.showToast(mContext, mContext.getString(R.string.blutooth_is_not_connected));
                    break;
                }
                EditText etNewPassword = (EditText) rootView.findViewById(R.id.et_new_password);
                String newPassword = etNewPassword.getText().toString();
                if( newPassword.equalsIgnoreCase("") || newPassword.length() < 4){
                    SPUtil.showToast(mContext, mContext.getString(R.string.password_more_than_four));
                    break;
                }

                PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
                PlaceSettingVo settingVo = new PlaceSettingVo(placeVo.getPlaceId(), SPConfig.PLACE_SETTING_BL_PASSWORD, newPassword);

                PlaceSettingService service = new PlaceSettingService(mContext);
                service.setOnHttpResponseCallbacks(this);
                service.requestUpdateBLPassword(settingVo);
                SPUtil.showDialog(mContext);
                break;
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

                        SPConfig.CURRENT_PLACE_BL_PASSWORD = settingVo.getSetVal();
                        SPUtil.showToast(mContext, "블루투스 비밀번호를 변경하였습니다.");
                        mCallbacks.onPlaceSettingResult(settingVo.getSetVal());
                    } else if( resultNum == -1) {
                        SPUtil.showToast(mContext, "블루투스 비밀번호 설정은 관리자만 설정 가능합니다.");
                    } else {
                        SPUtil.showToast(mContext, "블루투스 비밀번호 변경 요청에 실패하였습니다.");
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
