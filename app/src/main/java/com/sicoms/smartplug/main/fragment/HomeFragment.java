package com.sicoms.smartplug.main.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.main.event.MainEvent;
import com.sicoms.smartplug.main.service.HomeService;
import com.sicoms.smartplug.member.service.MemberService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.BlurEffect;
import com.sicoms.smartplug.util.SPUtil;

import java.util.List;

public class HomeFragment extends Fragment implements HttpResponseCallbacks {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private CharSequence mTitle = "Home";

    private final int MENU_CAMERA = 5;
    private final int MENU_ALBUM = 6;

    private Context mContext;
    private View mView;
    private PlaceVo mPlaceVo;
    private UserVo mUserVo;

    private MainEvent mEvent;
    private HomeService mService;
    private CommonService mCommonService;

    private MemberService mMemberService;
    private PlaceSettingService mPlaceSettingService;

    private RelativeLayout mRlMenuGroupMember;
    private RelativeLayout mRlMenuSmartPlug;
    private RelativeLayout mRlMenuSmartSocket;
    private RelativeLayout mRlMenuSmartBulbs;
    private RelativeLayout mRlMenuSmartSwitch;
    private SlidingDrawer mSlidingDrawer;
    private LinearLayout mLlDashBottom;
    private ImageView mIvDrawerHandle;

    private int mMenuStatus = 0;


    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize(){
        mPlaceVo = PlaceService.loadLastPlace(mContext);
        Bitmap bitmap = SPUtil.getBackgroundImage(mContext);
        if (bitmap != null) {
            bitmap = BlurEffect.blur(mContext, bitmap, SPConfig.BLUR_RADIUS);
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            // 이미지 다운로드
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            if( placeVo == null){
                return;
            }
            String placeImgPath = placeVo.getPlaceImg();
            ImgFileVo imgFileVo = new ImgFileVo(placeImgPath);
            mCommonService.requestDownloadImage(imgFileVo);
        }

        PlaceSettingVo settingVo = mPlaceSettingService.selectDbBLPassword();
        if( settingVo != null) {
            SPConfig.CURRENT_PLACE_BL_PASSWORD = settingVo.getSetVal();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        if( mTitle == null){
            mTitle = "Home";
        }
        try {
            ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
        } catch (NullPointerException npe){
            npe.printStackTrace();
            android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }

        mUserVo = LoginService.loadLastLoginUser(mContext);

        mEvent = new MainEvent(this, mUserVo);
        mService = new HomeService(this);
        mService.setOnHttpResponseCallbacks(this);
        mCommonService = new CommonService(mContext);
        mMemberService = new MemberService(mContext);
        mMemberService.setOnHttpResponseCallbacks(this);
        mPlaceSettingService = new PlaceSettingService(mContext);
        mPlaceSettingService.setOnHttpResponseCallbacks(this);
//        DBHelper dbHelper = new DBHelper(mContext);
//        dbHelper.reCreateAllTables();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mView = view;

        if( mUserVo == null) {
            return null; // Error
        }
        mRlMenuGroupMember = (RelativeLayout) view.findViewById(R.id.rl_menu_group_member);
        mRlMenuSmartPlug = (RelativeLayout) view.findViewById(R.id.rl_menu_smart_plug);
        mRlMenuSmartSocket = (RelativeLayout) view.findViewById(R.id.rl_menu_smart_socket);
        mRlMenuSmartBulbs = (RelativeLayout) view.findViewById(R.id.rl_menu_smart_bulbs);
        mRlMenuSmartSwitch = (RelativeLayout) view.findViewById(R.id.rl_menu_smart_switch);
        mSlidingDrawer = (SlidingDrawer) view.findViewById(R.id.sliding_drawer);
        mLlDashBottom = (LinearLayout) view.findViewById(R.id.ll_dash_bottom);
        mIvDrawerHandle = (ImageView) view.findViewById(R.id.iv_drawer_handle);

        mRlMenuGroupMember.setOnClickListener(mEvent);
        mRlMenuSmartPlug.setOnClickListener(mEvent);
        mRlMenuSmartSocket.setOnClickListener(mEvent);
        mRlMenuSmartBulbs.setOnClickListener(mEvent);
        mRlMenuSmartSwitch.setOnClickListener(mEvent);

        mSlidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                CommonService.saveLastPlugMainMenu(mContext, SPConfig.PLUG_MAIN_OPEN);
                mLlDashBottom.setVisibility(View.INVISIBLE);
                mIvDrawerHandle.setImageResource(R.drawable.pull_down_arrow);
            }
        });
        mSlidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                CommonService.saveLastPlugMainMenu(mContext, SPConfig.PLUG_MAIN_CLOSE);
                mLlDashBottom.setVisibility(View.VISIBLE);
                mIvDrawerHandle.setImageResource(R.drawable.pull_up_arrow);
            }
        });
        if( CommonService.loadLastPlugMainMenu(mContext) == SPConfig.PLUG_MAIN_OPEN){
            mSlidingDrawer.open();
            mLlDashBottom.setVisibility(View.INVISIBLE);
        } else {
            mSlidingDrawer.close();
            mLlDashBottom.setVisibility(View.VISIBLE);
        }

        long lastSyncSec = CommonService.loadLastMemberSyncTime(mContext);
        if( lastSyncSec != -1){ // 플레이스가 없으면 -1
            long currentSec = System.currentTimeMillis() / 1000;
            if( currentSec - lastSyncSec > SPConfig.SYNC_INTERVAL){
                mMemberService.requestSelectMemberList(); // 동기화
                CommonService.saveLastMemberSyncTime(mContext, currentSec);
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if( mMenuStatus == MENU_CAMERA || mMenuStatus == MENU_ALBUM){
            mMenuStatus = 0;
            return;
        }

        initialize();

        if( mPlaceVo == null || mPlaceVo.getPlaceId().equalsIgnoreCase("")) {
            if (!SPConfig.IS_SKIP) {
                SPUtil.showToast(mContext, "플레이스를 등록하세요.");
                SPActivity.intentPlaceActivity((Activity) mContext);
                return;
            }
        }
        if( mView != null && mPlaceVo != null) {
            SPConfig.IS_SKIP = false;
            mTitle = mPlaceVo.getPlaceName();
            ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
            // SPUtil.setBackgroundForLinear(mContext, mView, mPlaceVo.getPlaceImg()); // TODO
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS){
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if(CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_USER_LIST) {
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        mMemberService.deleteDbMemberAllInPlace();
                        List<UserVo> userVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<UserVo>>() {
                        }.getType());
                        if (userVoList == null || userVoList.size() < 1) {
                            SPUtil.dismissDialog();
                            return;
                        }
                        if ( !mMemberService.insertDbMemberList(userVoList)) {
                            SPUtil.showToast(mContext, "멤버 정보를 저장하지 못했습니다.");
                        }
                    } else {
                        SPUtil.showToast(mContext, "멤버 정보를 가져오는데 실패하였습니다.");
                    }
                    mPlaceSettingService.requestSelectBLPassword();
                } else if(CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_PLACE_BL_PASSWORD) {
                    if( resultNum == HttpConfig.HTTP_SUCCESS) {
                        PlaceSettingVo settingVo = new Gson().fromJson(responseVo.getJsonStr(), PlaceSettingVo.class);
                        PlaceSettingService service = new PlaceSettingService(mContext);
                        service.updateDbBLPassword(settingVo);
                        if( MainActivity.stBluetoothManager.isConnected()) {
                            MainActivity.stBluetoothManager.setSecurity(settingVo.getSetVal(), false);
                        }
                        SPConfig.CURRENT_PLACE_BL_PASSWORD = settingVo.getSetVal();
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
}