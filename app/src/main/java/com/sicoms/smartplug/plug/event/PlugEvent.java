package com.sicoms.smartplug.plug.event;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.network.bluetooth.BLMessage;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.plug.adapter.PlugGalleryAdapter;
import com.sicoms.smartplug.plug.interfaces.ControlResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.PlugService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class PlugEvent implements View.OnClickListener, UDPClient.UDPResponseCallbacks, PlugGalleryAdapter.OnItemClickListener {

    private Context mContext;
    private PlugVo mPlugVo;
    private EditNameFinishCallbacks mCallbacks;
    private PictureMenuCallbacks mPictureCallbacks;
    private ImageSelectedResultCallbacks mImageCallbacks;
    private ControlResultCallbacks mControlCallbacks;

    public PlugEvent(Context context){
        mContext = context;
    }
    public PlugEvent(Context context, PlugVo plugVo){
        mContext = context;
        mPlugVo = plugVo;
    }
    public void setOnControlResultCallbacks(ControlResultCallbacks callbacks){
        mControlCallbacks = callbacks;
    }
    public void setEditNameFinishListener(EditNameFinishCallbacks callbacks){
        mCallbacks = callbacks;
    }
    public void setOnPictureMenuCallbacks(PictureMenuCallbacks callbacks){
        mPictureCallbacks = callbacks;
    }
    public void setOnImageSelectedResultCallbacks(ImageSelectedResultCallbacks callbacks){
        mImageCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // Plug
            case R.id.iv_set_camera_btn :
                SPFragment.intentPictureMenuFragmentDialog((Activity) mContext, mPictureCallbacks, SPConfig.PICTURE_MENU_TYPE_PLACE);
                break;
            case R.id.rl_set_schedule_btn :
                if( mPlugVo != null) {
                    SPFragment.intentScheduleFragment((Activity) mContext, mPlugVo);
                }
                break;
            case R.id.rl_set_cutoff_btn :
                if( mPlugVo != null) {
                    SPFragment.intentCutoffFragment((Activity) mContext, mPlugVo);
                }
                break;
            case R.id.iv_plug_name_edit_btn :
                if( mCallbacks != null && mPlugVo != null) {
                    SPFragment.intentEditPlugNameFragmentDialog((Activity) mContext, mPlugVo, mCallbacks);
                }
                break;
            case R.id.iv_set_led_btn:
                boolean isOn = !v.isSelected();

                if( mPlugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                    String requestMessage = BLMessage.getLEDControlRequest(MainActivity.stBluetoothManager, Integer.parseInt(mPlugVo.getUuid()), isOn);
                    if( MainActivity.stBluetoothManager.isConnected()) {
                        if( !new PlugAllService(mContext).isEqualBluetoothPassword(mPlugVo)){
                            SPUtil.showToast(mContext, "블루투스 비밀번호가 틀렸습니다. Place Setting 메뉴에서 비밀번호를 변경해주세요.");
                            return;
                        }
                        MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
                        mControlCallbacks.onControlOnOffResult(mPlugVo, isOn);
                    } else {
                        SPUtil.showToast(mContext, "블루투스에 연결되지 않았습니다.");
                        return;
                    }
                } else {
                    PlugService service = new PlugService(mContext);
                    String requestMessage = service.getLEDRequestJsonData(isOn);

                    UDPClient udpClient = new UDPClient();
                    udpClient.setOnUDPResponseCallbacks(this);
                    if( requestMessage != null){
                        if( mPlugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
                            udpClient.execute(SPConfig.AP_IP, requestMessage);
                        } else if( mPlugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
                            udpClient.execute(mPlugVo.getBssid(), requestMessage);
                        } else if( mPlugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
                            udpClient.execute(mPlugVo.getGatewayIp(), requestMessage);
                        }
                    }
                }
                v.setSelected(isOn);
                break;
        }
    }

    @Override
    public void onUDPResponseResultStatus(int result, String response) {
        if (result != UDPConfig.UDP_SUCCESS)
            return;
    }

    @Override
    public void onItemClick(View view, String imageName) {
        if( mImageCallbacks != null){
            mImageCallbacks.onImageSelectedResult(imageName);
            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
        }
    }
}
