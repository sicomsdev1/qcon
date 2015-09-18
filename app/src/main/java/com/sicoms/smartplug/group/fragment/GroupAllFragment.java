package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.adapter.GroupAdapter;
import com.sicoms.smartplug.group.event.GroupAllEvent;
import com.sicoms.smartplug.group.service.GroupAllService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.util.BlurEffect;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupAllFragment extends Fragment implements HttpResponseCallbacks {

    private static final String TAG = GroupAllFragment.class.getSimpleName();

    private Context mContext;
    private CharSequence mTitle = "그룹";
    private View mView;

    private GroupAllEvent mEvent;
    private GroupAllService mService;
    private List<GroupVo> mGroupVoList;

    private TextView mTvOnCount;
    private TextView mTvAllCount;
    private ImageView mIvGroupAddBtn;
    private RecyclerView mRecyclerView;
    private GroupAdapter mAdapter;

    private ActionMode mActionMode;

    public static GroupAllFragment newInstance() {
        GroupAllFragment fragment = new GroupAllFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize(){
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
        try {
            ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
        } catch (NullPointerException npe){
            npe.printStackTrace();
            android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }
        CommonService.saveLastMenu(mContext, TAG);

        mEvent = new GroupAllEvent(mContext);
        mService = new GroupAllService(mContext);
        mService.setOnHttpResponseCallbacks(this);
        mEvent.setService(mService);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_group_all, container, false);
        mView = view;
        initialize();

        mTvOnCount = (TextView) view.findViewById(R.id.tv_on_count);
        mTvAllCount = (TextView) view.findViewById(R.id.tv_all_count);
        mIvGroupAddBtn = (ImageView) view.findViewById(R.id.iv_group_add_btn);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_group);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new GroupAdapter(mContext);
        mAdapter.SetOnItemClickListener(mEvent);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemLongClickListener(new GroupAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                if (mActionMode != null) {
                    return;
                }

                mActionMode = ((Activity)mContext).startActionMode(mActionModeCallback);
                view.setSelected(true);

                mAdapter.setMode(SPConfig.MODE_CHECK);
                mAdapter.notifyDataSetChanged();
            }
        });

        int auth = 1;
        try {
            auth = Integer.parseInt(PlaceService.loadLastPlace(mContext).getAuth());
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        if(auth == SPConfig.MEMBER_MASTER || auth == SPConfig.MEMBER_SETTER) {
            mIvGroupAddBtn.setOnClickListener(mEvent);
        } else {
            mIvGroupAddBtn.setVisibility(View.INVISIBLE);
        }

        long lastSyncSec = CommonService.loadLastGroupSyncTime(mContext);
        long currentSec = System.currentTimeMillis() / 1000;
        if( currentSec - lastSyncSec > SPConfig.SYNC_INTERVAL){
            mService.requestSelectGroupList(); // 동기화
            CommonService.saveLastGroupSyncTime(mContext, currentSec);
        }

        return view;
    }

    private List<PlugVo> getBLPlugVoList(){
        PlugAllService service = new PlugAllService(mContext);
        List<PlugVo> plugVoList = service.selectDbPlugList();
        List<PlugVo> blPlugVoList = new ArrayList<>();

        for(int voCnt=0; voCnt<plugVoList.size(); voCnt++){
            PlugVo plugVo = plugVoList.get(voCnt);
            String type = plugVo.getNetworkType();
            if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
                blPlugVoList.add(plugVo);
            }
        }
        return blPlugVoList;
    }

    private void fillAdapterData(){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGroupVoList = mService.selectDbGroupList();
                if (mGroupVoList == null) {
                    return;
                }
                Collections.sort(mGroupVoList, groupComparator);

                mAdapter.removeAll();
                mAdapter.addAll(mGroupVoList);
                mAdapter.notifyDataSetChanged();

                mTvAllCount.setText(String.valueOf(mGroupVoList.size()));
                int onCount = 0;
                for (GroupVo groupVo : mGroupVoList) {
                    if (groupVo.isOn()) {
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
        if( mActionMode != null) {
            mActionMode.finish();
        }
        mAdapter.setMode(SPConfig.MODE_NORMAL);
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        SPUtil.dismissDialog();
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo response = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(response.getResult());
                    if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_GROUP_LIST) {
                        if( resultNum == HttpConfig.HTTP_SUCCESS) {
                            final List<GroupVo> groupVoList = new Gson().fromJson(response.getJsonStr(), new TypeToken<List<GroupVo>>() {
                            }.getType());
                            if (groupVoList == null) {
                                return;
                            }

                            if (mService.updateDbGroupList(groupVoList)) {
                                fillAdapterData();
                            } else {
                                SPUtil.showToast(mContext, "그룹정보 동기화에 실패하였습니다.");
                            }
                        } else {
                            SPUtil.showToast(mContext, "그룹정보 요청에 실패하였습니다.");
                        }
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_DELETE_GROUP_LIST) {
                        if( resultNum == HttpConfig.HTTP_SUCCESS) {
                            List<GroupVo> groupVoList = new Gson().fromJson(response.getJsonStr(), new TypeToken<List<GroupVo>>() {
                            }.getType());
                            if (groupVoList != null) {
                                mService.deleteDbGroupList(groupVoList);
                                mAdapter.removeCheckedItem();
                                fillAdapterData();
                            }
                        } else if( resultNum == -1){
                            SPUtil.showToast(mContext, "그룹에는 1명 이상의 마스터 권한의 사용자가 있어야 합니다.");
                        } else {
                            SPUtil.showToast(mContext, "그룹정보 요청에 실패하였습니다.");
                        }
                    }

            } catch (JsonParseException jpe) {
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
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
                    mService.requestDeleteGroupList(mAdapter.getCheckedItem());
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

    private final static Comparator<GroupVo> groupComparator = new Comparator<GroupVo>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(GroupVo object1,GroupVo object2) {
            return collator.compare(object1.getGroupName(), object2.getGroupName());
        }
    };
}