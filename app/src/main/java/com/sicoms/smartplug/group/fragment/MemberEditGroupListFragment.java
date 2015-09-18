package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.adapter.MemberEditGroupAdapter;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.group.interfaces.MemberCheckResultCallbacks;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.member.service.MemberService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class MemberEditGroupListFragment extends Fragment implements MemberCheckResultCallbacks, HttpResponseCallbacks, View.OnKeyListener {

    private static final String TAG = MemberEditGroupListFragment.class.getSimpleName();

    private CharSequence mTitle = "사용자 추가";

    private Activity mActivity;
    private static EditGroupResultCallbacks mCallbacks;

    private MemberService mMemberService;
    private GroupService mGroupService;
    private GroupVo mGroupVo;
    private List<UserVo> mSelectedVoList;

    private TextView mTvMemberCount;

    private RecyclerView mRecyclerView;
    private MemberEditGroupAdapter mAdapter;

    private SPEvent mSPEvent;

    public static MemberEditGroupListFragment newInstance(GroupVo groupVo, EditGroupResultCallbacks callbacks) {
        MemberEditGroupListFragment fragment = new MemberEditGroupListFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(groupVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if( args != null){
            mGroupVo = new Gson().fromJson(args.getString(TAG), GroupVo.class);
        }
        if( savedInstanceState != null){
            mGroupVo = new Gson().fromJson(savedInstanceState.getString(TAG), GroupVo.class);
        }

        View view = inflater.inflate(R.layout.fragment_member_add_group_list, container, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(this);

        mActivity = getActivity();
        ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);
        mSPEvent = new SPEvent(mActivity);
        mSelectedVoList = new ArrayList<>();
        mMemberService = new MemberService(mActivity);
        mGroupService = new GroupService(mActivity);
        mMemberService.setOnHttpResponseCallbacks(this);
        mGroupService.setOnHttpResponseCallbacks(this);

        mTvMemberCount = (TextView) view.findViewById(R.id.tv_member_count);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_member);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new MemberEditGroupAdapter(getActivity(), this);

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();

        return view;
    }

    private void fillAdapterData(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<UserVo> userVoList = mMemberService.selectDbMemberList();
                if (userVoList == null) {
                    userVoList = new ArrayList<>();
                }

                List<UserVo> selectedUserVoList = mGroupVo.getUserVoList();
                if (selectedUserVoList == null) {
                    selectedUserVoList = new ArrayList<>();
                }

                List<UserVo> selectedVoList = new ArrayList<>();
                for (int userCnt=0; userCnt<userVoList.size(); userCnt++) {
                    for (int selectedCnt=0; selectedCnt<selectedUserVoList.size(); selectedCnt++) {
                        UserVo userVo = userVoList.get(userCnt);
                        UserVo selectedVo = selectedUserVoList.get(selectedCnt);
                        if (userVo.getUserId().equalsIgnoreCase(selectedVo.getUserId())) {
                            userVo.setIsCheck(true);
                            selectedVoList.add(0, userVo);
                        }
                    }
                }

                mAdapter.removeAll();
                mAdapter.addAll(userVoList);
                mAdapter.notifyDataSetChanged();

                mTvMemberCount.setText(String.valueOf(selectedVoList.size()));
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
                getFragmentManager().popBackStack();
                break;
            case R.id.action_complete :
                mGroupVo.setUserVoList(mSelectedVoList);
                mGroupService.requestInsertGroupUserMapping(mGroupVo);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedMember(UserVo memberVo) {
        mSelectedVoList.add(memberVo);
        mTvMemberCount.setText(String.valueOf(mSelectedVoList.size()));
    }

    @Override
    public void onUnCheckedMember(UserVo memberVo) {
        mSelectedVoList.remove(memberVo);
        mTvMemberCount.setText(String.valueOf(mSelectedVoList.size()));
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
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_USER_LIST) {
                        List<UserVo> voList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<UserVo>>() {
                        }.getType());
                        if (voList == null) {
                            return;
                        }
                        if (mMemberService.insertDbMemberList(voList)) {
                            fillAdapterData();
                        } else {
                            Toast.makeText(mActivity, "동기화에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_INSERT_GROUP_USER) {
                        mGroupVo = new Gson().fromJson(responseVo.getJsonStr(), GroupVo.class);
                        if (mGroupVo == null) {
                            SPUtil.showToast(mActivity, "그룹에 추가하지 못했습니다.");
                            return;
                        }

                        if (mGroupService.insertDbGroupUserMapping(mGroupVo, mGroupVo.getUserVoList())) {
                            mGroupService.saveLastGroupVo(mGroupVo);
                            if( mCallbacks != null) {
                                mCallbacks.onCompleteEditMember();
                            }
                            ((ActionBarActivity) mActivity).getSupportFragmentManager().popBackStack();
                            SPUtil.dismissDialog();
                        }
                    }
                } else {
                    Toast.makeText(mActivity, "서버 요청에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK){
            if( mSPEvent.isBack()) {
                getFragmentManager().popBackStack();
            }
//            mTitle = mGroupVo.getGroupName();
//            ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mTitle = mGroupVo.getGroupName();
        ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);
    }
}
