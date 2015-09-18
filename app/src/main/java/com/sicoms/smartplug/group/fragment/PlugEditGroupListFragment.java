package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.group.adapter.PlugEditGroupAdapter;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.group.interfaces.PlugCheckResultCallbacks;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlugEditGroupListFragment extends Fragment implements PlugCheckResultCallbacks, HttpResponseCallbacks {

    private static String TAG = PlugEditGroupListFragment.class.getSimpleName();

    private CharSequence mTitle = "플러그 추가";

    private Activity mActivity;
    private static EditGroupResultCallbacks mCallbacks;
    private GroupVo mGroupVo;
    private GroupService mGroupService;
    private PlugAllService mAllService;

    private List<PlugVo> mSelectedPlugVoList;

    private TextView mTvPlugCount;

    private RecyclerView mRecyclerView;
    private PlugEditGroupAdapter mAdapter;

    public static PlugEditGroupListFragment newInstance(GroupVo groupVo, EditGroupResultCallbacks callbacks) {
        PlugEditGroupListFragment fragment = new PlugEditGroupListFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(groupVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_complete);
        if( item != null) {
            item.setVisible(true);
        }
        MenuItem item2 = menu.findItem(R.id.action_group_menu);
        if( item2 != null){
            item2.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if( args != null){
            mGroupVo = new Gson().fromJson(args.getString(TAG), GroupVo.class);
        }
        if( savedInstanceState != null){
            mGroupVo = new Gson().fromJson(savedInstanceState.getString(TAG), GroupVo.class);
        }

        View view = inflater.inflate(R.layout.fragment_plug_add_group_list, container, false);

        mActivity = getActivity();
        ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);
        mGroupService = new GroupService(mActivity);
        mGroupService.setOnHttpResponseCallbacks(this);
        mAllService = new PlugAllService(mActivity);
        mAllService.setOnHttpResponseCallbacks(this);

        mSelectedPlugVoList = new ArrayList<>();

        mTvPlugCount = (TextView) view.findViewById(R.id.tv_plug_count);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_plug);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new PlugEditGroupAdapter(mActivity, this);

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();
        //mAllService.requestSelectPlugList();

        return view;
    }

    private void fillAdapterData(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<PlugVo> plugVoList = new PlugAllService(mActivity).selectDbPlugList();
                if (plugVoList == null) {
                    return;
                }
                List<PlugVo> selectedPlugVoList = mGroupVo.getPlugVoList();
                if (selectedPlugVoList == null) {
                    selectedPlugVoList = new ArrayList<>();
                }

                for (PlugVo plugVo : plugVoList) {
                    for (PlugVo selectedVo : selectedPlugVoList) {
                        if (plugVo.getPlugId().equalsIgnoreCase(selectedVo.getPlugId())) {
                            plugVo.setIsCheck(true);
                        }
                    }
                }

                mAdapter.removeAll();
                mAdapter.addAll(plugVoList);
                mAdapter.notifyDataSetChanged();

                mTvPlugCount.setText(String.valueOf(selectedPlugVoList.size()));
            }
        });
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
            case R.id.action_complete :
                SPUtil.showDialog(mActivity);
                mGroupService.setOnGroupResultCallbacks(mCallbacks);
                final List<PlugVo> plugVoList = mSelectedPlugVoList;
                if( plugVoList == null){
                    return false;
                }
                mGroupVo.setPlugVoList(plugVoList);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<PlugVo> apPlugVoList = new ArrayList<>();
                        //List<PlugVo> blPlugVoList = new ArrayList<>();
                        if (plugVoList != null) {
                            for (PlugVo plugVo : plugVoList) {
                                String type = plugVo.getNetworkType();
                                if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
                                    apPlugVoList.add(plugVo);
                                }
//                                else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
//                                    blPlugVoList.add(plugVo);
//                                }
                            }
                            if (apPlugVoList.size() > 0) {
                                SPUtil.showToast(mActivity, "AP 타입의 플러그는 그룹을 생성할 수 없습니다.");
                                SPUtil.dismissDialog();
                                return;
                            }
//                            if (blPlugVoList.size() > 0) {
//                                if (blPlugVoList.size() != plugVoList.size()) {
//                                    SPUtil.showToast(mActivity, "Bluetooth 타입의 플러그는 같은 타입끼리만 그룹이 가능합니다.");
//                                    SPUtil.dismissDialog();
//                                    return;
//                                }
//                                if (!mGroupService.editMeshGroup(mGroupVo, blPlugVoList)) {
//                                    SPUtil.showToast(mActivity, "그룹을 수정하지 못했습니다.");
//                                    SPUtil.dismissDialog();
//                                    return;
//                                }
//                            }
                        }
                        mGroupService.requestInsertGroupPlugMapping(mGroupVo);
                    }
                });
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedPlug(PlugVo plugVo) {
        mSelectedPlugVoList.add(plugVo);
        mTvPlugCount.setText(String.valueOf(mSelectedPlugVoList.size()));
    }

    @Override
    public void onUnCheckedPlug(PlugVo plugVo) {
        mSelectedPlugVoList.remove(plugVo);
        mTvPlugCount.setText(String.valueOf(mSelectedPlugVoList.size()));
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_PLUG_LIST) {
                        List<PlugVo> plugVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<PlugVo>>() {
                        }.getType());
                        if (plugVoList == null) {
                            SPUtil.dismissDialog();
                            SPUtil.showToast(mActivity, "플러그 리스트를 저장하지 못했습니다.");
                            return;
                        }
                        if (plugVoList.size() > 0) {
                            if (!mAllService.insertDbPlugList(plugVoList)) {
                                SPUtil.dismissDialog();
                                SPUtil.showToast(mActivity, "플러그 리스트를 저장하지 못했습니다.");
                                return;
                            }
                            mCallbacks.onCompleteEditPlug();
                        }

                    } else if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_INSERT_GROUP_PLUG){
                        mGroupVo = new Gson().fromJson(responseVo.getJsonStr(), GroupVo.class);
                        if( mGroupVo == null){
                            SPUtil.dismissDialog();
                            SPUtil.showToast(mActivity, "플러그를 추가하 못했습니다.");
                            return;
                        }
                        if( !mGroupService.insertDbGroupPlugMapping(mGroupVo, mGroupVo.getPlugVoList())){
                            SPUtil.showToast(mActivity, "플러그를 추가하지 못했습니다.");
                        }
                        SPUtil.dismissDialog();
                        ((ActionBarActivity) mActivity).getSupportFragmentManager().popBackStack();
                        mCallbacks.onCompleteEditPlug();
                    }
                } else {
                    SPUtil.showToast(mActivity, "서버 요청에 실패하였습니다.");
                    SPUtil.dismissDialog();
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        } else {
            SPUtil.showToast(mActivity, "서버 연결에 실패하였습니다.");
        }
    }
}
