package com.sicoms.smartplug.plug.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.hollowsoft.library.slidingdrawer.OnDrawerCloseListener;
import com.hollowsoft.library.slidingdrawer.OnDrawerOpenListener;
import com.hollowsoft.library.slidingdrawer.SlidingDrawer;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPPagerAdapter;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.domain.BluetoothVo;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugAllDataVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.bluetooth.BluetoothManager;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.adapter.PlugAdapter;
import com.sicoms.smartplug.plug.event.PlugAllEvent;
import com.sicoms.smartplug.plug.interfaces.ControlResultCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.util.BlurEffect;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;
import com.software.shell.fab.ActionButton;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlugAllFragment extends Fragment implements BluetoothManager.BLResultCallbacks, HttpResponseCallbacks, ControlResultCallbacks {

    private static String TAG = PlugAllFragment.class.getSimpleName();

    private CharSequence mTitle = "플러그";
    private Context mContext;
    private ActionMode mActionMode;
    private View mView;

    private List<PlugVo> mBLPlugVoList;

    private PlugAllEvent mEvent;
    private PlugAllService mService;

    private TextView mTvOnCount;
    private TextView mTvAllCount;
    private ImageView mIvAllPowerBtn;
    private ImageView mIvSyncBtn;
    private ActionButton mFabAddDeviceBtn;

    private RecyclerView mRecyclerView;
    private PlugAdapter mAdapter;

    private ViewPager mViewPager;
    private SPPagerAdapter mPagerAdapter;

    private BackgroundThread mBThread;
    private final PlugHandler mPHandler = new PlugHandler(this);

    public static PlugAllFragment newInstance() {
        PlugAllFragment fragment = new PlugAllFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void initialize(){
        Bitmap bitmap = SPUtil.getBackgroundImage(mContext);
        if( bitmap != null) {
            bitmap = BlurEffect.blur(mContext, bitmap, SPConfig.BLUR_RADIUS);
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        if( mContext == null || ((ActionBarActivity) mContext).getSupportActionBar() == null){
            return;
        }
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
        CommonService.saveLastMenu(mContext, TAG);

        if( MainActivity.stBluetoothManager != null && MainActivity.stBluetoothManager.isConnected()) {
            MainActivity.stBluetoothManager.setOnBLResultCallbacks(this);
        }
        mBLPlugVoList = new ArrayList<>();
        mEvent = new PlugAllEvent(mContext);
        mEvent.setOnControlResultCallbacks(this);
        mEvent.setOnHttpResponseCallbacks(this);
        mService = new PlugAllService(mContext);
        mService.setOnHttpResponseCallbacks(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_plug_all, container, false);
        mView = view;
        initialize();

        mTvOnCount = (TextView) view.findViewById(R.id.tv_on_count);
        mTvAllCount = (TextView) view.findViewById(R.id.tv_all_count);
        mIvAllPowerBtn = (ImageView) view.findViewById(R.id.iv_all_power_btn);
        mIvSyncBtn = (ImageView) view.findViewById(R.id.iv_sync_btn);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_plug);
        mFabAddDeviceBtn = (ActionButton) view.findViewById(R.id.fab_add_device_btn);

        mIvAllPowerBtn.setOnClickListener(mEvent);
        mIvSyncBtn.setOnClickListener(mEvent);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new PlugAdapter(mContext);
        mAdapter.SetOnItemClickListener(mEvent);
        int auth = 1;
        try {
            auth = Integer.parseInt(PlaceService.loadLastPlace(mContext).getAuth());
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        if(auth == SPConfig.MEMBER_MASTER || auth == SPConfig.MEMBER_SETTER) {
            mAdapter.SetOnItemLongClickListener(new PlugAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    if (mActionMode != null) {
                        return;
                    }

                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    view.setSelected(true);

                    mAdapter.setMode(SPConfig.MODE_CHECK);
                    mAdapter.notifyDataSetChanged();
                }
            });
            mFabAddDeviceBtn.setImageResource(R.drawable.fab_plus_icon);
            mFabAddDeviceBtn.setType(ActionButton.Type.MINI);
            mFabAddDeviceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SPActivity.intentRegDeviceActivity((Activity) mContext);
                }
            });
        } else {
            mFabAddDeviceBtn.setVisibility(View.GONE);
        }
        mRecyclerView.setAdapter(mAdapter);

        Fragment targetFragment[] = new Fragment[6];
//        targetFragment[0] = DashboardPageFragment.newInstance(SPConfig.PLUG_EDIT_PAGE01);
//        targetFragment[1] = DashboardPageFragment.newInstance(SPConfig.PLUG_EDIT_PAGE02);
//        targetFragment[2] = DashboardPageFragment.newInstance(SPConfig.PLUG_EDIT_PAGE03);

        int resId[] = new int[6];
        for(int cnt=0; cnt<targetFragment.length; cnt++){
            resId[cnt] = R.drawable.plug_edit_navi_off;
        }
        resId[0] = R.drawable.plug_edit_navi_on;

//        mViewPager = (ViewPager)view.findViewById(R.id.pager);
//        mPagerAdapter = new SPPagerAdapter(getFragmentManager(), view, targetFragment, resId);
//        mViewPager.setAdapter(mPagerAdapter);
//        mViewPager.setOnPageChangeListener(mPagerAdapter);

        long lastSyncSec = CommonService.loadLastPlugSyncTime(mContext);
        long currentSec = System.currentTimeMillis() / 1000;
        if( currentSec - lastSyncSec > SPConfig.SYNC_INTERVAL){
            mService.requestSelectPlugList(); // 동기화
            CommonService.saveLastPlugSyncTime(mContext, currentSec);
        }

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
    public void onResume() {
        super.onResume();
        fillAdapterData();
        if( mActionMode != null){
            mActionMode.finish();
        }
        mAdapter.setMode(SPConfig.MODE_NORMAL);
    }

    @Override
    public void onStop() {
        super.onStop();

        mBThread.setRunning(false);
    }

    private void fillAdapterData() {
        List<PlugVo> plugVoList = mService.selectDbPlugList();
        if (plugVoList == null) {
            return;
        }
        if (plugVoList.size() > 0) {
            Collections.sort(plugVoList, plugComparator);
            mAdapter.removeAll();
            mAdapter.addAll(plugVoList);
            mAdapter.notifyDataSetChanged();
        }
        for (PlugVo vo : plugVoList) {
            DbLastDataVo dbLastDataVo = mService.selectDbLastData(vo);
            if (dbLastDataVo != null) {
                vo.setIsOn(dbLastDataVo.getOnOff().equalsIgnoreCase(SPConfig.STATUS_ON));
            }
            if (vo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                mBLPlugVoList.add(vo);
            }
        }
        mTvAllCount.setText(String.valueOf(plugVoList.size()));
        int onCount = 0;
        mBLPlugVoList.clear();

        mTvOnCount.setText(String.valueOf(onCount));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBLConnectedResult(boolean isConnected) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                while(true){
                    mService.requestGetBLLastData( mBLPlugVoList);
                    SPUtil.sleep(10 * 1000);
                }
            }
        });
    }

    @Override
    public void onBLScanNonAssociationResult(BluetoothVo bluetoothVo) {

    }

    @Override
    public void onBLAssociationCompleteResult(int deviceId, int uuidHash) {

    }

    @Override
    public void onControlOnOffResult(PlugVo plugVo, boolean isOn) {
        if( plugVo != null) {
            DbLastDataVo dbLastDataVo = mService.selectDbLastData(plugVo);
            if( dbLastDataVo == null){
                dbLastDataVo = new DbLastDataVo(plugVo.getPlugId(), new Date(), 0.0f, 0.0f, SPConfig.STATUS_OFF, SPConfig.STATUS_ON);
            }
            dbLastDataVo.setOnOff(isOn ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);
            mService.updateDbLastData(dbLastDataVo);
        }
        fillAdapterData();
    }

    @Override
    public void onGroupControlOnOffResult(List<PlugVo> plugVoList, boolean isOn) {
        for(int cnt=0; cnt<plugVoList.size(); cnt++){
            PlugVo plugVo = plugVoList.get(cnt);
            DbLastDataVo dbLastDataVo = mService.selectDbLastData(plugVo);
            if( dbLastDataVo == null){
                dbLastDataVo = new DbLastDataVo(plugVo.getPlugId(), new Date(), 0.0f, 0.0f, SPConfig.STATUS_OFF, SPConfig.STATUS_ON);
            }
            dbLastDataVo.setOnOff(isOn ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);
            mService.updateDbLastData(dbLastDataVo);
        }
        fillAdapterData();
    }


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_delete, menu);
            mAdapter.SetOnItemClickListener(null);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            List<PlugVo> checkedVoList = mAdapter.getCheckedItem();
                            List<PlugVo> apPlugVoList = new ArrayList<>();
                            for (int voCnt=0; voCnt<checkedVoList.size(); voCnt++) {
                                PlugVo plugVo = checkedVoList.get(voCnt);
                                String type = plugVo.getNetworkType();
                                if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
                                    checkedVoList.remove(plugVo);
                                    apPlugVoList.add(plugVo);
                                }
                            }
                            if (checkedVoList.size() > 0) {
                                mService.requestDeletePlugList(mAdapter.getCheckedItem());
                            }
                            if (apPlugVoList.size() > 0) {
                                if (mService.deleteDbPlugList(apPlugVoList)) {
                                    mAdapter.removeCheckedItem();
                                    SPUtil.showToast(mContext, "선택한 플러그를 삭제하였습니다.");
                                } else {
                                    SPUtil.showToast(mContext, "AP 플러그를 삭제하지 못했습니다.");
                                }
                            }
                        }
                    });
                    mode.finish(); // Action picked, so close the CAB
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mAdapter.setMode(SPConfig.MODE_NORMAL);
            mAdapter.notifyDataSetChanged();
            mAdapter.SetOnItemClickListener(mEvent);
        }
    };

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        SPUtil.dismissDialog();
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_PLUG_LIST) {
                        PlugAllDataVo plugAllDataVo = new Gson().fromJson(responseVo.getJsonStr(), PlugAllDataVo.class);
                        List<PlugVo> plugVoList = plugAllDataVo.getPlugVoList();
                        List<DbBluetoothVo> dbBluetoothVoList = plugAllDataVo.getBluetoothVoList();
                        if (plugVoList == null || plugVoList.size() < 1) {
                            return;
                        }
                        mService.deleteDbPlugAllInPlace();
                        if (!mService.insertDbPlugList(plugVoList)) {
                            SPUtil.showToast(mContext, "플러그 리스트를 저장하지 못했습니다.");
                            return;
                        }
                        if( dbBluetoothVoList == null){
                            SPUtil.showToast(mContext, "블루투스 플러그 리스트를 저장하지 못했습니다.");
                            return;
                        }
                        if( !mService.updateDbBluetoothList(dbBluetoothVoList)){
                            SPUtil.showToast(mContext, "블루투스 플러그 리스트를 저장하지 못했습니다.");
                        }
                        fillAdapterData();
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_DELETE_PLUG) {
                        List<PlugVo> plugVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<PlugVo>>() {
                        }.getType());
                        if (plugVoList == null) {
                            SPUtil.showToast(mContext, "플러그를 삭제하지 못했습니다.");
                            return;
                        }
                        if (mService.deleteDbPlugList(plugVoList)) {
                            mAdapter.removeCheckedItem();
                            fillAdapterData();
                            SPUtil.showToast(mContext, "선택한 플러그를 삭제하였습니다.");
                        } else {
                            SPUtil.showToast(mContext, "플러그를 삭제하지 못했습니다.");
                        }
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_BLUETOOTH_LIST) {
                        List<DbBluetoothVo> dbBluetoothVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<DbBluetoothVo>>() {
                        }.getType());
                        if( dbBluetoothVoList == null){
                            SPUtil.showToast(mContext, "블루투스 플러그 리스트를 저장하지 못했습니다.");
                            return;
                        }
                        if( !mService.updateDbBluetoothList(dbBluetoothVoList)){
                            SPUtil.showToast(mContext, "블루투스 플러그 리스트를 저장하지 못했습니다.");
                        }
                    }
                } else {
                    SPUtil.showToast(mContext, "플러그 정보를 가져오지 못했습니다.");
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            }
        } else {
            SPUtil.showToast(mContext, "서버 연결에 실패하였습니다.");
        }
    }

    private final static Comparator<PlugVo> plugComparator = new Comparator<PlugVo>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(PlugVo object1,PlugVo object2) {
            return collator.compare(object1.getPlugName(), object2.getPlugName());
        }
    };

    private void handleMessage(Message msg){
        if( mAdapter.getMode() == SPConfig.MODE_NORMAL) {
            fillAdapterData();
        }
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
        private final WeakReference<PlugAllFragment> mFragment;
        public PlugHandler(PlugAllFragment fragment){
            mFragment = new WeakReference<PlugAllFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            PlugAllFragment fragment = mFragment.get();
            if( fragment != null){
                fragment.handleMessage(msg);
            }
        }
    }
}
