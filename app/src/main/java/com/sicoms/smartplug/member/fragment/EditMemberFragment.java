package com.sicoms.smartplug.member.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.member.adapter.AuthPlaceAdapter;
import com.sicoms.smartplug.member.event.MemberEvent;
import com.sicoms.smartplug.member.service.MemberService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.BlurEffect;
import com.sicoms.smartplug.util.SPUtil;

import antistatic.spinnerwheel.AbstractWheel;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class EditMemberFragment extends Fragment implements HttpResponseCallbacks {

    private static final String TAG = EditMemberFragment.class.getSimpleName();
    private CharSequence mTitle;

    private Context mContext;
    private View mView;

    private MemberEvent mEvent;
    private MemberService mService;
    private UserVo mUserVo;

    private AbstractWheel mWvAuth;
    private AuthPlaceAdapter mAdapter;

    private TextView mTvMemberEmail;
    private TextView mTvAuth;
    private RelativeLayout mRlAuthBtn;
    private ImageView mIvFinishBtn;
    private ImageView mIvArrowBtn;

    public static EditMemberFragment newInstance(UserVo vo) {
        EditMemberFragment fragment = new EditMemberFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(vo));
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
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null)
            mUserVo = new Gson().fromJson(getArguments().getString(TAG), UserVo.class);
        if (savedInstanceState != null)
            mUserVo = new Gson().fromJson(savedInstanceState.getString(TAG), UserVo.class);
        mContext = getActivity();
        mTitle = mUserVo.getUserName();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);

        mEvent = new MemberEvent(mContext, mUserVo);
        mEvent.setOnHttpResponseCallbacks(this);
        mService = new MemberService(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_edit_member, container, false);
        mView = view;
        initialize();

        mTvMemberEmail = (TextView) view.findViewById(R.id.tv_member_email);
        mTvAuth = (TextView) view.findViewById(R.id.tv_auth);
        mWvAuth = (AbstractWheel) view.findViewById(R.id.wv_auth);
        mRlAuthBtn = (RelativeLayout) view.findViewById(R.id.rl_auth_btn);
        mIvFinishBtn = (ImageView) view.findViewById(R.id.iv_finish_btn);
        mIvFinishBtn.setVisibility(View.INVISIBLE);
        mIvArrowBtn = (ImageView) view.findViewById(R.id.iv_arrow_btn);
        mIvArrowBtn.setVisibility(View.INVISIBLE);

        int auth = 1;
        try {
            auth = Integer.parseInt(PlaceService.loadLastPlace(mContext).getAuth());
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
            auth = 1;
        }
        String loginId = LoginService.loadLastLoginUser(mContext).getUserId();
        if( auth == SPConfig.MEMBER_MASTER && !loginId.equalsIgnoreCase(mUserVo.getUserId())) { // 마스터 권한 혹은 수정하려는 ID가 자신이 아니어야 함
            mIvFinishBtn.setVisibility(View.VISIBLE);
            mIvArrowBtn.setVisibility(View.VISIBLE);
            mRlAuthBtn.setOnClickListener(mEvent);
            mIvFinishBtn.setOnClickListener(mEvent);
        }

        mAdapter = new AuthPlaceAdapter(mContext);
        mWvAuth.setViewAdapter(mAdapter);
        mWvAuth.setCurrentItem(1);

        mEvent.setWheelAuth(mWvAuth);

        fillAdapterData();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_sync);
        item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                if( ((ActionBarActivity) mContext).getSupportFragmentManager().getBackStackEntryCount()>0) {
                    ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                } else {
                    ((Activity) mContext).finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillAdapterData(){
        mTvMemberEmail.setText(mUserVo.getUserId());
        if( mUserVo.getAuth() == SPConfig.MEMBER_MASTER) {
            mWvAuth.setCurrentItem(0);
            mTvAuth.setText(SPConfig.MEMBER_MASTER_NAME);
        } else if( mUserVo.getAuth() == SPConfig.MEMBER_SETTER) {
            mWvAuth.setCurrentItem(2);
            mTvAuth.setText(SPConfig.MEMBER_SETTER_NAME);
        } else {
            mWvAuth.setCurrentItem(1);
            mTvAuth.setText(SPConfig.MEMBER_USER_NAME);
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_USER){
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        UserVo userVo = new Gson().fromJson(responseVo.getJsonStr(), UserVo.class);
                        if( userVo == null){
                            SPUtil.dismissDialog();
                            SPUtil.showToast(mContext, "사용자를 수정하지 못했습니다.");
                            return;
                        }
                        if( mService.insertDbMember(userVo)){
                            fillAdapterData();
                            SPUtil.showToast(mContext, "사용자를 수정하였습니다.");
                            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                        }
                    } else {
                        SPUtil.showToast(mContext, "사용자를 수정하지 못했습니다.");
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
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
    }
}
