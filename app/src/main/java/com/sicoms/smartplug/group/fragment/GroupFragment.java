package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.hollowsoft.library.slidingdrawer.SlidingDrawer;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPPagerAdapter;
import com.sicoms.smartplug.dao.DbGroupUserMappingVo;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.event.GroupEvent;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.group.service.GroupAllService;

import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.adapter.PlugAdapter;
import com.sicoms.smartplug.plug.event.PlugAllEvent;
import com.sicoms.smartplug.plug.fragment.DashboardPageFragment;
import com.sicoms.smartplug.plug.interfaces.ControlResultCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class GroupFragment extends Fragment implements HttpResponseCallbacks, EditGroupResultCallbacks, ControlResultCallbacks, CreateGroupResultCallbacks {

    private static String TAG = GroupFragment.class.getSimpleName();

    private Activity mActivity;
    private ActionMode mActionMode;
    private View mView;

    private GroupService mService;
    private GroupAllService mAllService;
    private GroupEvent mGroupEvent;
    private PlugAllEvent mPluMainEvent;
    private GroupVo mGroupVo;

    private TextView mTvOnCount;
    private TextView mTvAllCount;
    private RelativeLayout mRlAllPowerBtn;
    private ActionButton mFabAddDeviceBtn;

    private RecyclerView mRecyclerView;
    private PlugAdapter mAdapter;

    public static GroupFragment newInstance(GroupVo groupVo) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(groupVo));
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize(){
        Bitmap bitmap = SPUtil.getBackgroundImage(mActivity);
        if( bitmap != null) {
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null)
            mGroupVo = new Gson().fromJson(args.getString(TAG), GroupVo.class);
        if (savedInstanceState != null)
            mGroupVo = new Gson().fromJson(savedInstanceState.getString(TAG), GroupVo.class);

        mActivity = getActivity();

        mGroupEvent = new GroupEvent(mActivity, mGroupVo);
        mGroupEvent.setOnEditGroupResultCallbacks(this);
        mGroupEvent.setOnControlResultCallbacks(this);
        mPluMainEvent = new PlugAllEvent(mActivity);
        mPluMainEvent.setOnControlResultCallbacks(this);
        mService = new GroupService(mActivity);
        mService.setOnHttpResponseCallbacks(this);
        mService.setOnGroupResultCallbacks(this);
        mService.saveLastGroupVo(mGroupVo);
        mAllService = new GroupAllService(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_group, container, false);
        mView = view;

        initialize();

        mTvOnCount = (TextView) view.findViewById(R.id.tv_on_count);
        mTvAllCount = (TextView) view.findViewById(R.id.tv_all_count);
        mRlAllPowerBtn = (RelativeLayout) view.findViewById(R.id.rl_all_power_btn);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_plug);

        mRlAllPowerBtn.setOnClickListener(mGroupEvent);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mFabAddDeviceBtn = (ActionButton) view.findViewById(R.id.fab_add_device_btn);

        mAdapter = new PlugAdapter(mActivity);
        mAdapter.SetOnItemClickListener(mPluMainEvent);
        DbGroupUserMappingVo vo = mService.selectDbGroupUserMapping(mGroupVo);
        if( vo == null){
            return view;
        }
        int auth = Integer.parseInt(vo.getAuth());
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
            mFabAddDeviceBtn.setOnClickListener(mGroupEvent);
        } else {
            mFabAddDeviceBtn.setVisibility(View.GONE);
        }

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();

        return view;
    }

    private void fillAdapterData(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mGroupVo = mAllService.selectDbGroup(Long.parseLong(mGroupVo.getGroupId()));
                    ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mGroupVo.getGroupName());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    return;
                }
                mGroupEvent.setGroupVo(mGroupVo);
                mService.saveLastGroupVo(mGroupVo);
                List<PlugVo> plugVoList = mGroupVo.getPlugVoList();
                if (plugVoList == null) {
                    plugVoList = new ArrayList<>();
                }
                mAdapter.removeAll();
                mAdapter.addAll(plugVoList);
                mAdapter.notifyDataSetChanged();

                mTvAllCount.setText(String.valueOf(plugVoList.size()));
                int onCount = 0;
                for (PlugVo plugVo : plugVoList) {
                    if (plugVo.isOn()) {
                        onCount++;
                    }
                }
                mTvOnCount.setText(String.valueOf(onCount));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapterData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home :
                if( ((ActionBarActivity) mActivity).getSupportFragmentManager().getBackStackEntryCount()>0) {
                    ((ActionBarActivity) mActivity).getSupportFragmentManager().popBackStack();
                } else {
                    mActivity.finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCompleteEditPlug() {
        fillAdapterData();
    }

    @Override
    public void onCompleteEditMember() {
    }

    @Override
    public void onControlOnOffResult(PlugVo plugVo, boolean isOn) {
        PlugAllService service = new PlugAllService(mActivity);
        DbLastDataVo dbLastDataVo = service.selectDbLastData(plugVo);
        dbLastDataVo.setOnOff(isOn ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);
        service.updateDbLastData(dbLastDataVo);

        fillAdapterData();
    }

    @Override
    public void onGroupControlOnOffResult(List<PlugVo> plugVoList, boolean isOn) {
        PlugAllService service = new PlugAllService(mActivity);
        for(int cnt=0; cnt<plugVoList.size(); cnt++){
            PlugVo plugVo = plugVoList.get(cnt);
            DbLastDataVo dbLastDataVo = service.selectDbLastData(plugVo);
            dbLastDataVo.setOnOff(isOn ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);
            service.updateDbLastData(dbLastDataVo);
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
                                    continue;
                                }
                                mGroupVo.getPlugVoList().remove(plugVo);
                            }
                            if (checkedVoList.size() > 0) {
                                mService.requestDeleteGroupPlugList(mGroupVo, checkedVoList);
                            }
                            if (apPlugVoList.size() > 0) {
                                SPUtil.showToast(mActivity, "AP 플러그를 삭제하지 못했습니다.");
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
            mAdapter.SetOnItemClickListener(mPluMainEvent);
        }
    };

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
            int resultNum = Integer.parseInt(responseVo.getResult());
            if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_GROUP) {
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    try {
                        GroupVo groupVo = new Gson().fromJson(responseVo.getJsonStr(), GroupVo.class);
                        if (groupVo == null) {
                            return;
                        }
                        if (!mService.updateDbGroup(groupVo)) {
                            Toast.makeText(mActivity, "그룹정보를 변경하지 못했습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        fillAdapterData();
                    } catch (JsonParseException jpe) {
                        jpe.printStackTrace();
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                } else {
                    Toast.makeText(mActivity, "그룹정보를 변경하지 못했습니다", Toast.LENGTH_SHORT).show();
                }
            } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_DELETE_GROUP_PLUG) {
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    try {
                        GroupVo groupVo = new Gson().fromJson(responseVo.getJsonStr(), GroupVo.class);
                        if (groupVo == null) {
                            return;
                        }
                        mGroupVo = groupVo;
                        if (mService.deleteDbGroupPlugList(mGroupVo, mAdapter.getCheckedItem())) {
                            mAdapter.removeCheckedItem();
                            SPUtil.showToast(mActivity, "플러그를 삭제하였습니다.");
                        } else {
                            SPUtil.showToast(mActivity, "플러그를 삭제하지 못했습니다.");
                        }
                        fillAdapterData();
                    } catch (JsonParseException jpe) {
                        jpe.printStackTrace();
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                } else if( resultNum == -1){
                    SPUtil.showToast(mActivity, "플러그 삭제 요청에 실패하였습니다.");
                }
            }
        } else {
            Toast.makeText(mActivity, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
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
        if( !isCreate){
            ((ActionBarActivity) mActivity).getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(mActivity, "Bluetooth 그룹 편집 실패", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
        fillAdapterData();
    }
}
