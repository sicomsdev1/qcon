package com.sicoms.smartplug.plug.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.domain.BluetoothVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.NodeListDataVo;
import com.sicoms.smartplug.domain.NodeListResponseVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.bluetooth.BluetoothManager;
import com.sicoms.smartplug.network.bluetooth.util.Device;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.network.udp.UDPBroadcaster;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.adapter.NonRegDeviceAdapter;
import com.sicoms.smartplug.plug.adapter.RegDeviceAdapter;
import com.sicoms.smartplug.plug.event.RegDeviceEvent;
import com.sicoms.smartplug.plug.interfaces.AssociatedDevicesResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.DialogFinishCallbacks;
import com.sicoms.smartplug.plug.interfaces.RegPlugResultCallbacks;
import com.sicoms.smartplug.plug.popup.BLSecurityDialogFragment;
import com.sicoms.smartplug.plug.popup.StationModeDialogFragment;
import com.sicoms.smartplug.plug.popup.WifiSecurityDialogFragment;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.RegDeviceService;
import com.sicoms.smartplug.plug.service.RegRouterService;
import com.sicoms.smartplug.util.SPUtil;

import java.lang.ref.WeakReference;
import java.util.List;

public class RegDeviceFragment extends Fragment implements RegPlugResultCallbacks, BluetoothManager.BLResultCallbacks, WifiConnectionManager.WifiScanResultCallbacks, UDPBroadcaster.UDPResponseCallbacks, AssociatedDevicesResultCallbacks, CreateGroupResultCallbacks, DialogFinishCallbacks, HttpResponseCallbacks {

    private static final String TAG = RegDeviceFragment.class.getSimpleName();

    private CharSequence mTitle = "새로운 플러그 추가";

    private Context mContext;
    private RegDeviceEvent mEvent;
    private RegDeviceService mService;

    private ImageView mIvDelete;
    private ImageView mIvComplete;
    private PullToRefreshListView mNonRegListView;
    private PullToRefreshListView mRegListView;
    private ImageView mIvIsBlConnect;
    private ImageView mIvIsWifiConnect;
    private TextView mTvIsBlConnect;
    private TextView mTvIsWifiConnect;
    private TextView mTvPullRefresh1;
    private TextView mTvPullRefresh2;

    private NonRegDeviceAdapter mNonRegAdapter;
    private RegDeviceAdapter mRegAdapter;

    private WifiConnectionManager mWifiScanner;
    private UDPBroadcaster mUDPBroadcaster;

    private BackgroundThread mBThread;
    private final PlugHandler mPHandler = new PlugHandler(this);

    public static RegDeviceFragment newInstance() {
        RegDeviceFragment fragment = new RegDeviceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);

        mService = new RegDeviceService(mContext);
        mService.setOnHttpResponseCallbacks(this);
        mEvent = new RegDeviceEvent(mContext);

        mNonRegAdapter = new NonRegDeviceAdapter(mContext, this, this);
        mRegAdapter = new RegDeviceAdapter(mContext);
        mEvent.setAdapter(mRegAdapter);

        mWifiScanner = new WifiConnectionManager(mContext);
        mWifiScanner.setOnWifiScanResultCallbacks(this);
        mUDPBroadcaster = new UDPBroadcaster();
        mUDPBroadcaster.setOnUDPResponseCallbacks(this);

        if( MainActivity.stBluetoothManager.isConnected()) {
            MainActivity.stBluetoothManager.serviceAssociation();
            MainActivity.stBluetoothManager.setOnBLResultCallbacks(this);
            MainActivity.stBluetoothManager.setOnAssociatedDevicesResultCallbacks(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_reg_device, container, false);
        Bundle args = getArguments();

        mIvDelete = (ImageView) view.findViewById(R.id.iv_delete);
        mIvComplete = (ImageView) view.findViewById(R.id.iv_complete);
        mNonRegListView = (PullToRefreshListView) view.findViewById(R.id.lv_non_reg_device);
        mRegListView = (PullToRefreshListView) view.findViewById(R.id.lv_reg_device);
        mIvIsBlConnect = (ImageView) view.findViewById(R.id.iv_is_bl_connect);
        mIvIsWifiConnect = (ImageView) view.findViewById(R.id.iv_is_wifi_connect);
        mTvIsBlConnect = (TextView) view.findViewById(R.id.tv_is_bl_connect);
        mTvIsWifiConnect = (TextView) view.findViewById(R.id.tv_is_wifi_connect);
        mTvPullRefresh1 = (TextView) view.findViewById(R.id.tv_pull_refresh1);
        mTvPullRefresh2 = (TextView) view.findViewById(R.id.tv_pull_refresh2);

        mNonRegListView.setAdapter(mNonRegAdapter);
        mRegListView.setAdapter(mRegAdapter);

        initPullToRefreshNonRegDevice(view);
        initPullToRefreshRegDevice(view);

        mIvDelete.setOnClickListener(mEvent);
        mIvComplete.setOnClickListener(mEvent);

        if( mService.isAPMode()){ // AP 모드일 때는 AP만 보여준다.
            setAPDevice();
        } else {
            // AP Scan
            mWifiScanner.scanWifiDevice();
            // GW Scan
            executeUDPBroadcast();
            // BL Scan
            if( !MainActivity.stBluetoothManager.isConnected()){
            } else {
                setAssociatedDevice();
            }
        }

        setNetworkStatus();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mBThread = new BackgroundThread();
        mBThread.setRunning(true);
        mBThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        mBThread.setRunning(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if( mUDPBroadcaster != null){
            mUDPBroadcaster.disconnect();
        }
    }

    private void setNetworkStatus(){
        if( MainActivity.stBluetoothManager.isConnected()){
            mIvIsBlConnect.setImageResource(R.drawable.icon_bluetooth_point);
            mTvIsBlConnect.setText(SPConfig.ON_TEXT);
        } else {
            mIvIsBlConnect.setImageResource(R.drawable.icon_bluetooth);
            mTvIsBlConnect.setText(SPConfig.OFF_TEXT);
        }
        if( WifiConnectionManager.isWifiEnabled(mContext)){
            mIvIsWifiConnect.setImageResource(R.drawable.icon_wifi_point);
            mTvIsWifiConnect.setText(SPConfig.ON_TEXT);
        } else {
            mIvIsWifiConnect.setImageResource(R.drawable.icon_wifi);
            mTvIsWifiConnect.setText(SPConfig.OFF_TEXT);
        }
    }

    private void executeUDPBroadcast(){
        final String requestJson = mService.getUDPScanJsonData();
        mUDPBroadcaster.sendBroadcast(requestJson);
    }

    private void setAssociatedDevice(){
        List<PlugVo> plugVoList = new PlugAllService(mContext).selectDbPlugList();
        for(int cnt=0; cnt<plugVoList.size(); cnt++){
            PlugVo plugVo = plugVoList.get(cnt);
            String networkType = plugVo.getNetworkType();
            if( networkType.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
                try {
                    int deviceId = Integer.parseInt(plugVo.getUuid());
                    if( !MainActivity.stBluetoothManager.isConnected()){
                        return;
                    }

                    // 블루투스 그룹 생성시 필요한데 지금은 for 문으로 제어
                   if( MainActivity.stBluetoothManager.getDevice(deviceId) == null) {
                        MainActivity.stBluetoothManager.addDevice(deviceId, 0, "", 0, true);
                    }
                } catch (NumberFormatException nfe){
                    nfe.printStackTrace();
                }
            }
        }
        List<Device> deviceList = MainActivity.stBluetoothManager.getDevices();
        for( int cnt=0; cnt<deviceList.size(); cnt++){
            Device device = deviceList.get(cnt);
            String plugId = String.format("%x", device.getDeviceId());
            RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_bluetooth_point, R.drawable.icon_device_plugc, plugId, SPConfig.PLUG_TYPE_BLUETOOTH);
            regDeviceVo.setDeviceId(device.getDeviceId());
            setRegDeviceVoList(regDeviceVo);
            MainActivity.stBluetoothManager.setDeviceName(device.getDeviceId(), plugId);
        }
    }

    private Handler mRefreshCompleteHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            mNonRegListView.onRefreshComplete();
            mRegListView.onRefreshComplete();
        }
    };

    private void initPullToRefreshNonRegDevice(View view) {
        // Set a listener to be invoked when the list should be refreshed.
        mNonRegListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(mContext.getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                Log.d(RegDeviceFragment.class.getCanonicalName(), "onRefreshing..." + label);
                //mNonRegAdapter.removeAll();
                mNonRegAdapter.notifyDataSetChanged();
                if (mService.isAPMode()) {

                } else {
                    // AP Scan
                    mWifiScanner.scanWifiDevice();
                    // Bluetooth Scan
                    if (MainActivity.stBluetoothManager.isConnected()) {
                        MainActivity.stBluetoothManager.serviceAssociation();
                    }
                }
                mRefreshCompleteHandler.sendEmptyMessageDelayed(1, 5000);
            }
        });

        final ListView actualListView = mNonRegListView.getRefreshableView();
        actualListView.setAdapter(mNonRegAdapter);
    }

    private void initPullToRefreshRegDevice(View view) {
        // Set a listener to be invoked when the list should be refreshed.
        mRegListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(mContext.getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                Log.d(RegDeviceFragment.class.getCanonicalName(), "onRefreshing..." + label);
                //mRegAdapter.removeAll();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mService.isAPMode()) { // AP 모드일 때는 AP만 보여준다.
                            setAPDevice();
                        } else {
                            executeUDPBroadcast();
                            setAssociatedDevice();
                        }
                        mRefreshCompleteHandler.sendEmptyMessageDelayed(1, 5000);
                    }
                });
                thread.start();
            }
        });

        final ListView actualListView = mRegListView.getRefreshableView();
        actualListView.setAdapter(mRegAdapter);
    }

    @Override
    public void onGetAssociatedDevice(String plugId, List<String> groupIdList) {
        RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_bluetooth_point, R.drawable.icon_device_plugc, plugId, SPConfig.PLUG_TYPE_BLUETOOTH);
        int deviceId = Integer.parseInt(plugId, 16);
        regDeviceVo.setDeviceId(deviceId);
        setRegDeviceVoList(regDeviceVo);
        MainActivity.stBluetoothManager.setDeviceName(deviceId, plugId);
    }

    private void setAPDevice(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                WifiVo wifiVo = mWifiScanner.getConnectedWifiInfo(); // AP모드면 현재 Wifi정보 가져옴
                RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_wifi_point, R.drawable.icon_device_plugb, wifiVo.getSsid(), SPConfig.PLUG_TYPE_WIFI_AP);
                regDeviceVo.setBssid(wifiVo.getBssid());
                mRegAdapter.add(regDeviceVo);
                mRegAdapter.notifyDataSetChanged();
            }
        });
    }

    // 미등록 리스트 등록 (공통 프로세스 처리)
    private void setNonRegDeviceVoList(final RegDeviceVo regDeviceVo){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if( mTvPullRefresh1.getVisibility() == View.VISIBLE) {
                    mTvPullRefresh1.setVisibility(View.GONE);
                }
                List<RegDeviceVo> nonRegDeviceVoList = mNonRegAdapter.getAll();
                if (nonRegDeviceVoList.size() == 0) {
                    nonRegDeviceVoList.add(regDeviceVo);
                } else {
                    // 중복 체크
                    boolean isVo = false;
                    for (int voCnt = 0; voCnt < nonRegDeviceVoList.size(); voCnt++) {
                        if (nonRegDeviceVoList.get(voCnt).getPlugId().equalsIgnoreCase(regDeviceVo.getPlugId())) {
                            nonRegDeviceVoList.set(voCnt, regDeviceVo);
                            isVo = true;
                            break;
                        }
                    }
                    if (!isVo) {
                        nonRegDeviceVoList.add(regDeviceVo);
                    }
                }

                mNonRegAdapter.notifyDataSetChanged();
            }
        });
    }

    // 등록된 리스트 등록 (공통 프로세스 처리)
    private void setRegDeviceVoList(final RegDeviceVo regDeviceVo){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if( mTvPullRefresh2.getVisibility() == View.VISIBLE) {
                    mTvPullRefresh2.setVisibility(View.GONE);
                }
                List<RegDeviceVo> regDeviceVoList = mRegAdapter.getAll();
                if (regDeviceVoList.size() == 0) {
                    regDeviceVoList.add(regDeviceVo);
                } else {
                    // 중복 체크
                    boolean isVo = false;
                    for (int voCnt = 0; voCnt < regDeviceVoList.size(); voCnt++) {
                        if (regDeviceVoList.get(voCnt).getPlugId().equalsIgnoreCase(regDeviceVo.getPlugId())) {
                            regDeviceVoList.set(voCnt, regDeviceVo);
                            isVo = true;
                            break;
                        }
                    }
                    if (!isVo) {
                        regDeviceVoList.add(regDeviceVo);
                    }
                }

                mRegAdapter.notifyDataSetChanged();
                SPUtil.dismissDialog();
            }
        });
    }

    @Override
    public void onWifiScanResult(List<WifiVo> wifiList) {
        for(WifiVo wifiVo : wifiList){
            RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_wifi_point, R.drawable.icon_device_plugb, wifiVo.getSsid(), SPConfig.PLUG_TYPE_WIFI_ROUTER);
            regDeviceVo.setBssid(wifiVo.getBssid());
            setNonRegDeviceVoList(regDeviceVo);
        }
        SPUtil.dismissDialog();
    }

    @Override
    public void onBLConnectedResult(boolean isConnected) {
        mNonRegAdapter.removeAll();
        mNonRegAdapter.notifyDataSetChanged();
        if( isConnected){
            MainActivity.stBluetoothManager.serviceAssociation();
            // setAssociatedDevice();
        }
        SPUtil.dismissDialog();
    }

    @Override
    public void onBLScanNonAssociationResult(BluetoothVo bluetoothVo) {
        Log.d(TAG, "Receive Bluetooth : " + bluetoothVo.getUuid());
        RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_bluetooth_point, R.drawable.icon_device_plugc, bluetoothVo.getUuid(), SPConfig.PLUG_TYPE_BLUETOOTH);
        regDeviceVo.setUuidHash(bluetoothVo.getUuidHash());

        setNonRegDeviceVoList(regDeviceVo);
        SPUtil.dismissDialog();
    }

    @Override
    public void onBLAssociationCompleteResult(final int deviceId, int uuidHash) {
        SPUtil.dismissDialog();
        if( deviceId == 0 && uuidHash == 0) { // Association Fail
            //SPFragment.intentBLSecurityFragmentDialog(mContext, PlugAllFragment.stBluetoothManager);
        } else {
            PlaceSettingService settingService = new PlaceSettingService(mContext);
            PlaceSettingVo settingVo = settingService.selectDbBLPassword();
            DbBluetoothVo dbBluetoothVo = new DbBluetoothVo(String.valueOf(deviceId), settingVo.getSetVal());
            mService.requestUpdateBluetooth(dbBluetoothVo);
            SPUtil.showToast(mContext, String.format("%x", deviceId) + " 플러그를 추가하였습니다.");
            mNonRegAdapter.removeBluetooth();
            mNonRegAdapter.notifyDataSetChanged();
            if (MainActivity.stBluetoothManager.isConnected()) {
                MainActivity.stBluetoothManager.serviceAssociation();
            }
            setAssociatedDevice();
        }
    }

    @Override
    public void onRegCompleteResult(RegDeviceVo regDeviceVo) {
        SPUtil.showDialog(mContext);
        mNonRegAdapter.removeItem(regDeviceVo);
        mNonRegAdapter.notifyDataSetChanged();
        if (mService.isAPMode()) {

        } else {
            // AP Scan
            mWifiScanner.scanWifiDevice();
            // Bluetooth Scan
            if (MainActivity.stBluetoothManager.isConnected()) {
                MainActivity.stBluetoothManager.serviceAssociation();
            }
        }

        if( regDeviceVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
            setAssociatedDevice();
        } else if( regDeviceVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
            // AP Scan
            mWifiScanner.scanWifiDevice();
            // GW Scan
            executeUDPBroadcast();
        }
    }

    String mReceiveData;
    @Override
    public void onUDPResponseResultStatus(final String ip, String receiveData) {
        mReceiveData = receiveData;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    NodeListResponseVo nodeListResponseVo = new Gson().fromJson(mReceiveData, NodeListResponseVo.class);
                    if (nodeListResponseVo.getAg().equalsIgnoreCase(SPConfig.NODE_TYPE_P)) { // Station Mode
                        for (int cnt=0; cnt<nodeListResponseVo.getDp().size(); cnt++) {
                            NodeListDataVo vo = nodeListResponseVo.getDp().get(cnt);
                            if( !SPUtil.isCharacters(vo.getND_CODE())){
                                vo.setND_CODE(SPUtil.getRandomId());
                            }
                            RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_wifi_point, R.drawable.icon_device_plugb, vo.getND_CODE(), SPConfig.PLUG_TYPE_WIFI_ROUTER);
                            regDeviceVo.setIpAddress(ip);

                            setRegDeviceVoList(regDeviceVo);
                        }
                    } else { // Gateway Mode
                        for (int cnt=0; cnt<nodeListResponseVo.getDp().size(); cnt++) {
                            NodeListDataVo vo = nodeListResponseVo.getDp().get(cnt);
                            RegDeviceVo regDeviceVo = new RegDeviceVo(R.drawable.icon_wifi_point, R.drawable.icon_device_gateway, vo.getND_NAME(), SPConfig.PLUG_TYPE_WIFI_GW);
                            regDeviceVo.setIpAddress(ip);

                            setRegDeviceVoList(regDeviceVo);
                        }
                    }
                } catch (JsonSyntaxException jse){
                    jse.printStackTrace();
                    SPUtil.showToast(mContext, "잘못된 데이터 수신");
                }
                SPUtil.dismissDialog();
            }
        });
    }

    @Override
    public void onGroupAddPlugList(List<PlugVo> plugVoList) {

    }

    @Override
    public void onGroupAddMemberList(List<UserVo> userVoList) {

    }

    @Override
    public void onGroupEditMember(UserVo userVo) {

    }

    @Override
    public void onCompleteCreateBLGroup(int groupId, boolean isCreate) {
        if( isCreate){
//            if( mService.updateDbPlugList(Arrays.asList(mRegedPlugVo), "")) {
//                SPUtil.showToast(mContext, "선택하신 Bluetooth 플러그를 추가하였습니다.");
//            } else {
//                SPUtil.showToast(mContext, "Bluetooth 플러그를 추가하지 못했습니다.");
//            }
            SPUtil.showToast(mContext, String.valueOf(groupId) + " 블루투스 플러그를 추가하였습니다.");
            mNonRegAdapter.notifyDataSetChanged();
            setAssociatedDevice();
            SPUtil.dismissDialog();
        } else {
//            PlugAllService service = new PlugAllService(mContext);
//            int deviceId = Integer.parseInt(mRegedPlugVo.getUuid());
//            SPUtil.sleep(1000);
//            service.deleteAssociatedDevice(mRegedPlugVo);
//            MainActivity.stBluetoothManager.removeDevice(deviceId);
            SPUtil.showToast(mContext, "블루투스 플러그를 추가하지 못했습니다.");
        }
    }

    @Override
    public void onDialogFinishCallbacks(String dialogName, RegDeviceVo regDeviceVo) {
        if( dialogName.equalsIgnoreCase(BLSecurityDialogFragment.class.getSimpleName())) {
            if (regDeviceVo != null) {
                if (regDeviceVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                    int hash = regDeviceVo.getUuidHash();
                    if (hash != 0) {
                        MainActivity.stBluetoothManager.associateDevice(hash);
                        onRegCompleteResult(regDeviceVo);
                        return;
                    } else {
                        SPUtil.showToast(mContext, "블루투스 플러그를 찾을 수 없습니다.");
                        SPUtil.dismissDialog();
                    }
                } else {
                    SPUtil.showToast(mContext, "블루투스 플러그를 찾을 수 없습니다.");
                    SPUtil.dismissDialog();
                }
            } else {
                SPUtil.showToast(mContext, "블루투스 플러그를 찾을 수 없습니다.");
                SPUtil.dismissDialog();
            }
        } else
        if( dialogName.equalsIgnoreCase(WifiSecurityDialogFragment.class.getSimpleName())) {
            if (regDeviceVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
                PlugVo plugVo = new PlugVo(regDeviceVo.getPlugId(), regDeviceVo.getPlugId(), regDeviceVo.getNetworkType(), SPConfig.PLUG_DEFAULT_IMAGE_NAME + "_00", false);
                RegRouterService service = new RegRouterService(mContext);
                WifiVo currentWifiVo = new WifiConnectionManager(mContext).getConnectedWifiInfo();

                // Station Mode 1. 플러그 와이파이 접속
                if (!service.connectPlugWifi(plugVo)) {
                    return;
                }
                // Station Mode 2. 와이파이 접속 대기 -> 연결 완료시 3. UDP 통신으로 AP 에 로컬망 Wifi 정보 전달
                SPFragment.intentWifiConnectWaitFragmentDialog((Activity) mContext, this, currentWifiVo);
            }
        } else if( dialogName.equalsIgnoreCase(StationModeDialogFragment.class.getSimpleName())){
            onRegCompleteResult(regDeviceVo);
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS){
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if(CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_INSERT_BLUETOOTH) {
                    if( resultNum == HttpConfig.HTTP_SUCCESS) {
                        DbBluetoothVo dbBluetoothVo = new Gson().fromJson(responseVo.getJsonStr(), DbBluetoothVo.class);
                        if( !mService.updateDbBluetooth(dbBluetoothVo)){
                            SPUtil.showToast(mContext, "블루투스 정보를 저장하지 못했습니다.");
                        }

                    } else {
                        SPUtil.showToast(mContext, "블루투스 비밀번호를 동기화하지 못했습니다.");
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

    private void handleMessage(Message msg){
        setNetworkStatus();
    }

    private class BackgroundThread extends Thread {
        boolean running = false;

        void setRunning(boolean b){
            running = b;
        }

        @Override
        public void run() {
            while(running){
                SPUtil.sleep(3000);
                mPHandler.sendMessage(mPHandler.obtainMessage());
            }
        }
    }

    private static class PlugHandler extends Handler {
        private final WeakReference<RegDeviceFragment> mFragment;
        public PlugHandler(RegDeviceFragment fragment){
            mFragment = new WeakReference<RegDeviceFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            RegDeviceFragment fragment = mFragment.get();
            if( fragment != null){
                fragment.handleMessage(msg);
            }
        }
    }
}