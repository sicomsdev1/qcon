package com.sicoms.smartplug.plug.event;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.adapter.RegDeviceAdapter;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.RegDeviceService;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class RegDeviceEvent implements View.OnClickListener, AdapterView.OnItemClickListener, HttpResponseCallbacks {

    private Context mContext;
    private RegDeviceService mService;
    private RegDeviceAdapter mRegAdapter;

    public RegDeviceEvent(Context context){
        mContext = context;
        mService = new RegDeviceService(mContext);
        mService.setOnHttpResponseCallbacks(this);
    }
    public void setAdapter( RegDeviceAdapter adapter){
        mRegAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        String type = "";
        ViewGroup rootView = (ViewGroup) v.getRootView();
        switch (v.getId()) {
            case R.id.iv_complete :
                if( !SPUtil.isNetwork(mContext)){
                    SPUtil.showToast(mContext, "네트워크가 불안정합니다. 네트워크를 확인해주세요.");
                }
                List<RegDeviceVo> voList = mRegAdapter.getAll();
                List<RegDeviceVo> selectedVoList = new ArrayList<RegDeviceVo>();
                if( voList == null){
                    return;
                }
                for( RegDeviceVo vo : voList){
                    if( vo.isRegDevice()){
                        selectedVoList.add(vo);
                    }
                }

                for(int voCnt=0; voCnt<selectedVoList.size(); voCnt++) {
                    RegDeviceVo vo = selectedVoList.get(voCnt);
                    List<PlugVo> apPlugVoList = new ArrayList<PlugVo>();
                    type = vo.getNetworkType();

                    // TODO : 로그인 모드에서 AP모드 어떻게 할지 결정 필요 start
                   if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                        selectedVoList.remove(vo); // Cloud Server에 보낼 때 AP 타입은 제
                        PlugVo plugVo = new PlugVo(vo.getPlugId(), vo.getPlugId(), vo.getNetworkType(), SPConfig.PLUG_DEFAULT_IMAGE_NAME + "_00", false);
                        plugVo.setBssid(vo.getBssid());
                        apPlugVoList.add(plugVo);
                    }
                    // AP, BL 타입은 바로 로컬에 저장
                    if( apPlugVoList.size() > 0) {
                        String currentSsid = new WifiConnectionManager(mContext).getConnectedWifiInfo().getSsid();
                        if (mService.updateDbPlugList(apPlugVoList, currentSsid)) {
                            SPUtil.showToast(mContext, "선택하신 AP 플러그를 추가하였습니다.");
                        } else {
                            SPUtil.showToast(mContext, "AP 플러그를 추가하지 못했습니다.");
                        }
                    }
                    // TODO : end
                }
                if( selectedVoList.size() > 0){
                    if( !mService.checkDuplicatedPlug(selectedVoList)){
                        return;
                    }
                    mService.requestInsertDevice(selectedVoList);
                    SPUtil.showDialog(mContext);
                }

                break;

            case R.id.iv_delete :
                if( mRegAdapter == null){
                    return;
                }
                SPUtil.showDialog(mContext);
                PlugAllService plugAllService = new PlugAllService(mContext);
                final List<PlugVo> plugVoList = plugAllService.selectDbPlugList();
                final List<RegDeviceVo> regDeviceVoList = mRegAdapter.getAll();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<RegDeviceVo> removeList = new ArrayList<RegDeviceVo>();
                        for (int rCnt = 0; rCnt < regDeviceVoList.size(); rCnt++) {
                            RegDeviceVo regDeviceVo = regDeviceVoList.get(rCnt);
                            if (!regDeviceVo.isRegDevice()) {
                                continue;
                            }
                            if (!regDeviceVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                                SPUtil.showToast(mContext, regDeviceVo.getPlugId() + " 플러그를 지울 수 없습니다. 블루투스 타입 외에는 원격으로 초기화 할 수 없습니다.");
                                SPUtil.dismissDialog();
                                return;
                            }
                            for( int pCnt=0; pCnt < plugVoList.size(); pCnt++) {
                                PlugVo plugVo = plugVoList.get(pCnt);
                                if( plugVo.getPlugId().equalsIgnoreCase(regDeviceVo.getPlugId())){
                                    SPUtil.showToast(mContext, regDeviceVo.getPlugId() + " 플러그를 지울 수 없습니다. 플러그 리스트에서 먼저 제거해주시기 바랍니다.");
                                    SPUtil.dismissDialog();
                                    return;
                                }
                            }
                            mService.deleteAssociatedDevice(regDeviceVo);
                            MainActivity.stBluetoothManager.removeDevice(regDeviceVo.getDeviceId());
                            removeList.add(regDeviceVo);

                            SPUtil.sleep(500);
                        }
                        for(int rCnt=0; rCnt<removeList.size(); rCnt++){
                            RegDeviceVo regDeviceVo = removeList.get(rCnt);
                            if (regDeviceVo.isRegDevice()) {
                                mRegAdapter.removeItem(regDeviceVo);
                            }
                        }
                        SPUtil.showToast(mContext, "선택한 플러그를 삭제하였습니다.");
                        SPUtil.dismissDialog();
                    }
                });

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getAdapter().getItem(position) instanceof RegDeviceVo) {
            RegDeviceVo vo = (RegDeviceVo) parent.getAdapter().getItem(position);
            vo.setIsRegDevice(!vo.isRegDevice());
            Log.d("RegDeviceEvent", "Reg Device Status " + vo.isRegDevice());
            view.setSelected(vo.isRegDevice());
            ((RegDeviceAdapter) parent.getAdapter()).setSelected(position);
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        SPUtil.dismissDialog();
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if (resultNum == HttpConfig.HTTP_SUCCESS) {
                    List<PlugVo> plugVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<PlugVo>>() {
                    }.getType());
                    if (plugVoList == null) {
                        if (value.equalsIgnoreCase(HttpConfig.HTTP_RESULT_FALSE)) {
                            SPUtil.showToast(mContext, "이미 추가된 플러그입니다. 동기화 해주시기 바랍니다.");
                        }
                        return;
                    }
                    String currentSsid = new WifiConnectionManager(mContext).getConnectedWifiInfo().getSsid();
                    if (mService.updateDbPlugList(plugVoList, currentSsid)) {
                        SPUtil.showToast(mContext, "선택한 플러그를 추가하였습니다.");
                    } else {
                        SPUtil.showToast(mContext, "플러그를 추가하지 못했습니다.");
                    }
                } else {
                    SPUtil.showToast(mContext, "플러그를 추가하지 못했습니다.");
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
    }
}
