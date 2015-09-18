package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.adapter.AuthGroupAdapter;
import com.sicoms.smartplug.group.event.GroupMemberEvent;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.List;

import antistatic.spinnerwheel.AbstractWheel;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class MemberEditAuthFragment extends Fragment implements HttpResponseCallbacks, View.OnKeyListener {

    private static final String TAG = MemberEditAuthFragment.class.getSimpleName();
    private CharSequence mTitle;

    private Activity mActivity;
    private View mView;

    private GroupMemberEvent mEvent;
    private GroupService mService;
    private UserVo mUserVo;

    private AbstractWheel mWvAuth;
    private AuthGroupAdapter mAdapter;

    private TextView mTvMemberName;
    private TextView mTvAuth;
    private RelativeLayout mRlAuthBtn;
    private ImageView mIvFinishBtn;
    private ImageView mIvArrowBtn;

    private SPEvent mSPEvent;

    public static MemberEditAuthFragment newInstance(UserVo vo) {
        MemberEditAuthFragment fragment = new MemberEditAuthFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(vo));
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
            mUserVo = new Gson().fromJson(getArguments().getString(TAG), UserVo.class);
        if (savedInstanceState != null)
            mUserVo = new Gson().fromJson(savedInstanceState.getString(TAG), UserVo.class);
        mActivity = getActivity();
        mTitle = mUserVo.getUserName();
        ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(mTitle);
        mSPEvent = new SPEvent(mActivity);

        mEvent = new GroupMemberEvent(mActivity, mUserVo);
        mEvent.setOnHttpResponseCallbacks(this);
        mService = new GroupService(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_edit_member, container, false);
        mView = view;
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(this);
        initialize();

        mTvMemberName = (TextView) view.findViewById(R.id.tv_member_email);
        mTvAuth = (TextView) view.findViewById(R.id.tv_auth);
        mWvAuth = (AbstractWheel) view.findViewById(R.id.wv_auth);
        mRlAuthBtn = (RelativeLayout) view.findViewById(R.id.rl_auth_btn);
        mIvFinishBtn = (ImageView) view.findViewById(R.id.iv_finish_btn);
        mIvFinishBtn.setVisibility(View.INVISIBLE);
        mIvArrowBtn = (ImageView) view.findViewById(R.id.iv_arrow_btn);
        mIvArrowBtn.setVisibility(View.INVISIBLE);

        if( isMaster()) {
            mIvFinishBtn.setVisibility(View.VISIBLE);
            mIvArrowBtn.setVisibility(View.VISIBLE);
            mRlAuthBtn.setOnClickListener(mEvent);
            mIvFinishBtn.setOnClickListener(mEvent);
        }

        mAdapter = new AuthGroupAdapter(mActivity);
        mWvAuth.setViewAdapter(mAdapter);
        mWvAuth.setCurrentItem(1);

        mEvent.setWheelAuth(mWvAuth);

        fillAdapterData();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item1 = menu.findItem(R.id.action_group_menu);
        if( item1 != null){
            item1.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                mActivity.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillAdapterData(){
        mTvMemberName.setText(mUserVo.getUserName());
        if( mUserVo.getAuth() == SPConfig.MEMBER_MASTER) {
            mWvAuth.setCurrentItem(0);
            mTvAuth.setText(SPConfig.MEMBER_MASTER_NAME);
        } else {
            mWvAuth.setCurrentItem(1);
            mTvAuth.setText(SPConfig.MEMBER_USER_NAME);
        }
    }

    private boolean isMaster(){
        GroupService service = new GroupService(mActivity);
        GroupVo groupVo = service.loadLastGroup();
        UserVo loginVo = LoginService.loadLastLoginUser(mActivity);
        List<UserVo> userVoList = groupVo.getUserVoList();
        for(int cnt=0; cnt<userVoList.size(); cnt++){
            UserVo userVo = userVoList.get(cnt);
            if( userVo.getUserId().equalsIgnoreCase(loginVo.getUserId())){
                if( userVo.getAuth() == SPConfig.MEMBER_MASTER){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_GROUP_USER){
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        GroupVo groupVo = new Gson().fromJson(responseVo.getJsonStr(), GroupVo.class);
                        if( groupVo == null){
                            SPUtil.dismissDialog();
                            SPUtil.showToast(mActivity, "그룹 수정하지 못했습니다.");
                            return;
                        }
                        if( mService.updateDbGroupUserMapping(null, groupVo, groupVo.getUserVoList())){
                            mService.saveLastGroupVo(groupVo);
                            SPUtil.showToast(mActivity, "그룹을 수정하였습니다.");
                            mActivity.finish();
                        }
                    } else {
                        SPUtil.showToast(mActivity, "그룹을 수정하지 못했습니다.");
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
            Toast.makeText(mActivity, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK){
            if( mSPEvent.isBack()) {
                mActivity.finish();
            }
            return true;
        } else {
            return false;
        }
    }
}
