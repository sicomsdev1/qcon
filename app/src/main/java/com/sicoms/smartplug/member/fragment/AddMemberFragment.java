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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
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
import antistatic.spinnerwheel.OnWheelScrollListener;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class AddMemberFragment extends Fragment implements HttpResponseCallbacks {

    private static final String TAG = AddMemberFragment.class.getSimpleName();

    private CharSequence mTitle = "사용자 추가";

    private Context mContext;
    private View mView;

    private MemberEvent mEvent;
    private MemberService mService;
    private UserVo mUserVo;

    private AbstractWheel mWvAuth;
    private AuthPlaceAdapter mAdapter;

    private EditText mEtMemberId;
    private LinearLayout mRlAuthBtn;
    private TextView mTvCurrentAuth;
    private ImageView mIvFinishBtn;

    public static AddMemberFragment newInstance() {
        AddMemberFragment fragment = new AddMemberFragment();
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
        setHasOptionsMenu(true);

        mContext = getActivity();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);

        mUserVo = new UserVo();
        mEvent = new MemberEvent(mContext, mUserVo);
        mService = new MemberService(mContext);
        mService.setOnHttpResponseCallbacks(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_add_member, container, false);
        mView =view;

        initialize();

        // 권한 확인
        if( !PlaceService.loadLastPlace(mContext).getAuth().equalsIgnoreCase(String.valueOf(SPConfig.MEMBER_MASTER))) {
            SPUtil.showToast(mContext, "잘못된 접근입니다.");
            SPFragment.intentMemberFragment((Activity) mContext);
        }

        mEtMemberId = (EditText) view.findViewById(R.id.et_member_id);
        mRlAuthBtn = (LinearLayout) view.findViewById(R.id.rl_auth_btn);
        mWvAuth = (AbstractWheel) view.findViewById(R.id.wv_auth);
        mIvFinishBtn = (ImageView) view.findViewById(R.id.iv_finish_btn);
        mTvCurrentAuth = (TextView) view.findViewById(R.id.tv_current_auth);

        mRlAuthBtn.setOnClickListener(mEvent);
        mIvFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = mEtMemberId.getText().toString();
                int auth = mWvAuth.getCurrentItem();
                if( userId.equalsIgnoreCase("")) {
                    Toast.makeText(mContext, "추가 할 상대방의 ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if( !SPUtil.isEmail(userId)){
                    Toast.makeText(mContext, "이메일 형식이 틀렸습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mUserVo.setUserId(userId);
                mUserVo.setAuth(auth);
                mService.requestInsertMember(mUserVo);
            }
        });

        mAdapter = new AuthPlaceAdapter(mContext);
        mWvAuth.setViewAdapter(mAdapter);
        mWvAuth.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                int auth = wheel.getCurrentItem();
                if (auth == 0) {
                    mTvCurrentAuth.setText(SPConfig.MEMBER_MASTER_NAME);
                } else if (auth == 2) {
                    mTvCurrentAuth.setText(SPConfig.MEMBER_SETTER_NAME);
                } else {
                    mTvCurrentAuth.setText(SPConfig.MEMBER_USER_NAME);
                }
            }
        });
        mWvAuth.setCurrentItem(1);
        mTvCurrentAuth.setText(SPConfig.MEMBER_USER_NAME);

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
                    ((Activity)mContext).finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_INSERT_USER) {
                    HttpResponseVo response = new Gson().fromJson(value, HttpResponseVo.class);
                    int resultNum = Integer.parseInt(response.getResult());
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        UserVo userVo = new Gson().fromJson(response.getJsonStr(), UserVo.class);
                        mUserVo = userVo;
                        if (userVo != null) {
                            mService.insertDbMember(userVo); // TODO : GCM 버전일 경우 나중에 초대 응답 했을 때 저장
                            Toast.makeText(mContext, userVo.getUserName() + "님에게 초대 메시지를 보냈습니다.", Toast.LENGTH_SHORT).show();
                            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();

                            return;
                        }
                    } else if (resultNum == -1) {
                        SPUtil.showToast(mContext, "해당 ID의 사용자가 없습니다. 다시 한번 확인해 주시기 바랍니다.");
                    } else if (resultNum == -2) {
                        SPUtil.showToast(mContext, "이미 추가되어 있는 사용자입니다.");
                    } else {
                        SPUtil.showToast(mContext, "사용자를 추가하지 못했습니다.");
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
        if( SPConfig.IS_TEST){
            DBHelper dbHelper = new DBHelper(mContext);
            mUserVo.setUserName("TEST User");
            mService.insertDbMember(mUserVo);
            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
        }
    }
}
