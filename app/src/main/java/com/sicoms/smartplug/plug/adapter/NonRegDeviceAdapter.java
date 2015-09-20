package com.sicoms.smartplug.plug.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.bluetooth.BLConfig;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.interfaces.DialogFinishCallbacks;
import com.sicoms.smartplug.plug.interfaces.RegPlugResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-03-04.
 */
public class NonRegDeviceAdapter extends BaseAdapter {

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<RegDeviceVo> mVoList;
    private RegPlugResultCallbacks mRegCallbacks;
    private DialogFinishCallbacks mDialogCallbacks;

    public NonRegDeviceAdapter(Context context, RegPlugResultCallbacks regCallbacks, DialogFinishCallbacks dialogCallbacks){
        mContext = context;
        mRegCallbacks = regCallbacks;
        mDialogCallbacks = dialogCallbacks;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mVoList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mVoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mVoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = new ViewHolder();

        try {
            final RegDeviceVo regDeviceVo = mVoList.get(position);

            view = mInflater.inflate(R.layout.adapter_non_reg_device, parent, false);

            viewHolder.iv_plug_type = (ImageView) view.findViewById(R.id.iv_plug_type);
            viewHolder.iv_plug = (ImageView) view.findViewById(R.id.iv_plug);
            viewHolder.tv_plug_id = (TextView) view.findViewById(R.id.tv_plug_id);
            viewHolder.iv_add = (ImageView) view.findViewById(R.id.iv_add);

            viewHolder.iv_plug_type.setImageResource(regDeviceVo.getPlugTypeIconImg());
            viewHolder.iv_plug.setImageResource(regDeviceVo.getPlugIconImg());

            String plugId = regDeviceVo.getPlugId();
            String plugType = regDeviceVo.getNetworkType();
            if (plugType.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                plugId = plugId.split("-")[4];
            }
            viewHolder.tv_plug_id.setText(plugId);

            viewHolder.iv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!SPUtil.isNetwork(mContext)) {
                        SPUtil.showToast(mContext, "네트워크가 불안정합니다. 네트워크를 확인해주세요.");
                    }
                    if (regDeviceVo == null) {
                        return;
                    }
                    String type = regDeviceVo.getNetworkType();
                    if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                        PlaceSettingVo settingVo = new PlaceSettingService(mContext).selectDbBLPassword();
                        String blNetworkKey = settingVo.getSetVal();
                        if( blNetworkKey.equalsIgnoreCase(BLConfig.BL_DEFAULT_SECURITY_PASSWORD)){
                            SPFragment.intentBLSecurityFragmentDialog((Activity)mContext, regDeviceVo, mDialogCallbacks);
                        } else {
                            int hash = regDeviceVo.getUuidHash();
                            if (hash != 0) {
                                MainActivity.stBluetoothManager.associateDevice(hash);
                                if (mRegCallbacks != null) {
                                    mRegCallbacks.onRegCompleteResult(regDeviceVo);
                                    return;
                                }
                            } else {
                                SPUtil.showToast(mContext, "Bluetooth 플러그를 찾을 수 없습니다.");
                                SPUtil.dismissDialog();
                            }
                        }
                    } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {

                        WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(mContext);
                        WifiVo wifiVo = wifiConnectionManager.getConnectedWifiInfo();
                        if (wifiVo.getBssid().equalsIgnoreCase("")) {
                            SPUtil.showToast(mContext, "먼저 플러그에 접속시킬 Wi-Fi를 연결해주세요.");
                        }
                        SPFragment.intentWifiSecurityFragmentDialog((Activity)mContext, regDeviceVo, mDialogCallbacks);
                    }
                    //AP, Gateway 모드는 등록된 것으로 취급
                }
            });
        } catch (IndexOutOfBoundsException ioobe){
            ioobe.printStackTrace();
            return view;
        }
        return view;
    }

    public List<RegDeviceVo> getAll(){
        return mVoList;
    }

    public void add(RegDeviceVo vo){
        // 중복 검사
        for( RegDeviceVo savedVo : mVoList){
            if( savedVo.getPlugId() == vo.getPlugId()){
                return;
            }
        }
        mVoList.add(0, vo);
    }
    public void addAll(List<RegDeviceVo> listVo){
        mVoList.addAll(listVo);
    }
    public void removeAll(){
        mVoList.clear();
    }
    public void removeBluetooth(){
        for(int cnt=0; cnt<mVoList.size(); cnt++) {
            RegDeviceVo regDeviceVo = mVoList.get(cnt);
            if( regDeviceVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                mVoList.remove(regDeviceVo);
            }
        }
    }
    public void removeItem(RegDeviceVo regDeviceVo){
        for(int cnt=0; cnt<mVoList.size(); cnt++) {
            RegDeviceVo removeVo = mVoList.get(cnt);
            if( removeVo.getPlugId().equalsIgnoreCase(regDeviceVo.getPlugId())) {
                mVoList.remove(removeVo);
            }
        }
    }

    private class ViewHolder{
        private ImageView iv_plug_type;
        private ImageView iv_plug;
        private TextView tv_plug_id;
        private ImageView iv_add;
    }
}
