package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.group.adapter.PlugAddGroupAdapter;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.group.interfaces.PlugCheckResultCallbacks;
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
public class PlugAddGroupListFragment extends Fragment implements PlugCheckResultCallbacks, HttpResponseCallbacks {

    private static String TAG = PlugAddGroupListFragment.class.getSimpleName();

    private CharSequence mTitle = "플러그 추가";

    private Context mContext;
    private View mView;

    private static CreateGroupResultCallbacks mCallbacks;
    private GroupVo mGroupVo;
    private PlugAllService mService;

    private List<PlugVo> mSelectedPlugVoList;

    private TextView mTvPlugCount;

    private RecyclerView mRecyclerView;
    private PlugAddGroupAdapter mAdapter;


    public static PlugAddGroupListFragment newInstance(GroupVo groupVo, CreateGroupResultCallbacks callbacks) {
        PlugAddGroupListFragment fragment = new PlugAddGroupListFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(groupVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    private void initialize(){
        Bitmap bitmap = SPUtil.getBackgroundImage(mContext);
        if( bitmap != null) {
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
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
        mView = view;

        mContext = getActivity();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
        initialize();

        mService = new PlugAllService(mContext);
        mService.setOnHttpResponseCallbacks(this);

        SPUtil.hideKeyboard(mContext, view);

        mSelectedPlugVoList = new ArrayList<>();
        mTvPlugCount = (TextView) view.findViewById(R.id.tv_plug_count);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_plug);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new PlugAddGroupAdapter(mContext, this);

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();
        //mService.requestSelectPlugList();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_complete);
        if( item != null) {
            item.setVisible(false);
        }
        MenuItem item2 = menu.findItem(R.id.action_group_menu);
        if( item2 != null){
            item2.setVisible(false);
        }
    }

    private void fillAdapterData(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<PlugVo> plugVoList = new PlugAllService(mContext).selectDbPlugList();
                if (plugVoList == null) {
                    return;
                }
                List<PlugVo> selectedPlugVoList = mGroupVo.getPlugVoList();
                if (selectedPlugVoList == null) {
                    selectedPlugVoList = new ArrayList<>();
                }

                mSelectedPlugVoList = new ArrayList<>();
                for( int pcnt=0; pcnt<plugVoList.size(); pcnt++){
                    PlugVo plugVo = plugVoList.get(pcnt);
                    for( int scnt=0; scnt<selectedPlugVoList.size(); scnt++){
                        PlugVo selectedVo = selectedPlugVoList.get(scnt);
                        if (plugVo.getPlugId().equalsIgnoreCase(selectedVo.getPlugId())) {
                            plugVo.setIsCheck(true);
                            mSelectedPlugVoList.add(plugVo);
                        }
                    }
                }

                mAdapter.removeAll();
                mAdapter.addAll(plugVoList);
                mAdapter.notifyDataSetChanged();

                mTvPlugCount.setText(String.valueOf(mSelectedPlugVoList.size()));
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
                if( ((ActionBarActivity) mContext).getSupportFragmentManager().getBackStackEntryCount()>0) {
                    ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                } else {
                    ((Activity)mContext).finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedPlug(PlugVo plugVo) {
        mSelectedPlugVoList.add(plugVo);
        mTvPlugCount.setText(String.valueOf(mSelectedPlugVoList.size()));
        mCallbacks.onGroupAddPlugList(mSelectedPlugVoList);
    }

    @Override
    public void onUnCheckedPlug(PlugVo plugVo) {
        mSelectedPlugVoList.remove(plugVo);
        mTvPlugCount.setText(String.valueOf(mSelectedPlugVoList.size()));
        mCallbacks.onGroupAddPlugList(mSelectedPlugVoList);
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
                            return;
                        }
                        if (plugVoList.size() > 0) {
                            if (!mService.insertDbPlugList(plugVoList)) {
                                SPUtil.showToast(mContext, "플러그 리스트를 저장하지 못했습니다.");
                                return;
                            }
                            fillAdapterData();
                        }

                    }
                } else {
                    SPUtil.showToast(mContext, "플러그 리스트를 저장하지 못했습니다.");
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        } else {
            SPUtil.showToast(mContext, "서버 연결에 실패하였습니다.");
        }
    }
}
