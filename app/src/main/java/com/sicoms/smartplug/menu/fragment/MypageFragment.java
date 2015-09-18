package com.sicoms.smartplug.menu.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CropImageIntentBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.event.MypageEvent;
import com.sicoms.smartplug.menu.service.MypageService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.CircleImageView;
import com.sicoms.smartplug.util.profile.MediaStoreUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class MypageFragment extends Fragment implements PictureMenuCallbacks, EditNameFinishCallbacks, HttpResponseCallbacks, HttpBitmapResponseCallbacks, View.OnKeyListener {

    private static final String TAG = MypageFragment.class.getSimpleName();

    private final int MENU_CAMERA = 5;
    private final int MENU_ALBUM = 6;

    private Context mContext;
    private View mView;

    private MypageEvent mEvent;
    private MypageService mService;
    private SPEvent mSPEvent;
    private UserVo mUserVo;

    private CircleImageView mIvProfile;
    private ImageView mIvMemberRank;
    private ImageView mIvCameraBtn;
    private ImageView mIvNicknameBtn;
    private TextView mTvNickname;
    private RelativeLayout mRlChangePasswordBtn;
    private RelativeLayout mRlLogoutBtn;
    private RelativeLayout mRlEditPassword;
    private TextView mTvLoginId;

    private ImageView mIvFinishBtn;

    private int mMenuStatus = 0;
    private boolean isChanged;

    public static MypageFragment newInstance() {
        MypageFragment fragment = new MypageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_mypage_list, container, false);
        mView = view;
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(this);

        isChanged = false;
        mContext = getActivity();
        mUserVo = LoginService.loadLastLoginUser(mContext);
        mEvent = new MypageEvent(mContext, mUserVo);
        mEvent.setPictureMenuCallbacks(this);
        mEvent.setEditNameFinishCallbacks(this);
        mService = new MypageService(mContext);
        mSPEvent = new SPEvent((Activity) mContext);

        mIvProfile = (CircleImageView) view.findViewById(R.id.iv_profile);
        mIvMemberRank = (ImageView) view.findViewById(R.id.iv_member_rank);
        mIvMemberRank.setVisibility(View.INVISIBLE);
        mIvCameraBtn = (ImageView) view.findViewById(R.id.iv_camera_btn);
        mIvNicknameBtn = (ImageView) view.findViewById(R.id.iv_nickname_btn);
        mTvNickname = (TextView) view.findViewById(R.id.tv_nickname);
        mRlChangePasswordBtn = (RelativeLayout) view.findViewById(R.id.rl_change_password_btn);
        mRlLogoutBtn = (RelativeLayout) view.findViewById(R.id.rl_logout_btn);
        mRlEditPassword = (RelativeLayout) view.findViewById(R.id.rl_edit_password);
        mRlEditPassword.setVisibility(View.GONE);
        mTvLoginId = (TextView) view.findViewById(R.id.tv_login_id);
        mIvFinishBtn = (ImageView) view.findViewById(R.id.iv_finish_btn);

        mTvNickname.setText(mUserVo.getUserName());
        mTvLoginId.setText("(" + mUserVo.getUserId() + ")");

        mIvCameraBtn.setOnClickListener(mEvent);
        mIvNicknameBtn.setOnClickListener(mEvent);
        mRlChangePasswordBtn.setOnClickListener(mEvent);
        mRlLogoutBtn.setOnClickListener(mEvent);
        mIvFinishBtn.setOnClickListener(mEvent);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if( mMenuStatus == MENU_CAMERA || mMenuStatus == MENU_ALBUM){
            mMenuStatus = 0;
            return;
        }

        setProfile();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if( isChanged) {
                    SPActivity.intentMainActivity((Activity) mContext);
                } else {
                    ((Activity) mContext).finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setProfile(){
        String imagePath = SPConfig.FILE_PATH + mUserVo.getUserProfileImg();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if( bitmap != null) {
            mIvProfile.setImageBitmap(bitmap);
        } else {
            mIvProfile.setImageResource(R.drawable.profile_default);
            if( SPUtil.isNetwork(mContext)) {
                CommonService service = new CommonService(mContext);
                service.setOnHttpBitmapResponseCallbacks(this);
                service.requestDownloadImage(new ImgFileVo(mUserVo.getUserProfileImg()));
            }
        }

        int auth = 1;
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        if( placeVo != null){
            try {
                auth = Integer.parseInt(placeVo.getAuth());
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        }

        if(auth == SPConfig.MEMBER_MASTER){
            mIvMemberRank.setVisibility(View.VISIBLE);
        } else {
            mIvMemberRank.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK){
            CommonService commonService = new CommonService(mContext, this);

            switch (requestCode){
                case SPConfig.REQUEST_PICTURE :
                    String imageName = SPConfig.USER_IMAGE_NAME + "_" + mUserVo.getUserId().replace("@", "_") + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                    File croppedFile = new File(SPConfig.FILE_PATH, imageName);
                    mUserVo.setUserProfileImg(imageName);

                    // When the user is done picking a picture, let's start the CropImage Activity,
                    // setting the output image file and size to 200x200 pixels square.
                    Uri croppedImage = Uri.fromFile(croppedFile);

                    CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, croppedImage);
                    cropImage.setOutlineColor(0xFF03A9F4);
                    cropImage.setSourceImage(data.getData());

                    startActivityForResult(cropImage.getIntent(mContext), SPConfig.REQUEST_CROP_PICTURE);
                    break;

                case SPConfig.REQUEST_CROP_PICTURE :
                    // When we are done cropping, display it in the ImageView.
                    File imageFile = new File(SPConfig.FILE_PATH, mUserVo.getUserProfileImg());
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    mIvProfile.setImageBitmap(bitmap);
                    mUserVo.setUserProfileImg(imageFile.getName());

                    commonService.requestUploadImage(imageFile);
                    SPUtil.showDialog(mContext);
                    break;

                case SPConfig.REQUEST_CAMERA :
                    final Bundle extras = data.getExtras();
                    if( extras != null){
                        Bitmap cameraImgBitmap = extras.getParcelable("data");
                        String cameraImageName = SPConfig.USER_IMAGE_NAME + "_" + mUserVo.getUserId().replace("@", "_") + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        File cameraFile = SPUtil.saveBitmapImage(cameraImageName, cameraImgBitmap);

                        mIvProfile.setImageBitmap(cameraImgBitmap);
                        mUserVo.setUserProfileImg(cameraFile.getName());

                        commonService.requestUploadImage(cameraFile);
                        SPUtil.showDialog(mContext);
                    }

                    break;
            }
        }
    }

    @Override
    public void onEditNameFinish(String name) {
        mUserVo.setUserName(name);
        mTvNickname.setText(name);
        LoginService.saveLastLoginUser(mContext, mUserVo);
        isChanged = true;
    }

    @Override
    public void onPictureMenuResult(int menu) {
        switch (menu){
            case SPConfig.PICTURE_MENU_ALBUM :
                startActivityForResult(MediaStoreUtils.getPickImageIntent(mContext), SPConfig.REQUEST_PICTURE);
                mMenuStatus = MENU_ALBUM;
                break;
            case SPConfig.PICTURE_MENU_CAMERA :
                if( SPUtil.isIntentAvailable(mContext, MediaStore.ACTION_IMAGE_CAPTURE)) {
                    startActivityForResult(SPUtil.getIntentDefaultCamera(mContext), SPConfig.REQUEST_CAMERA);
                    mMenuStatus = MENU_CAMERA;
                }
                break;
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        SPUtil.dismissDialog();
        if( result == HttpConfig.HTTP_SUCCESS){
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_MEMBERSHIP_PROFILE) {
                    if (resultNum != HttpConfig.HTTP_SUCCESS) {
                        SPUtil.showToast(mContext, "프로필 이미지를 수정하지 못했습니다.");
                    }
                    isChanged = true;
                } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPLOAD_COMMON_IMAGE) {
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        LoginService.saveLastLoginUser(mContext, mUserVo);
                        mService.requestUpdateMembershipProfile(mUserVo);
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

    @Override
    public void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap) {
        if (result == HttpConfig.HTTP_SUCCESS) {
            SPUtil.saveBitmapImage(fileName, bitmap);
            setProfile();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK){
            if( mSPEvent.isBack()) {
                if (isChanged) {
                    SPActivity.intentMainActivity((Activity) mContext);
                } else {
                    ((Activity) mContext).finish();
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
