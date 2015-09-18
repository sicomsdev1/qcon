package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.common.interfaces.OutCallbacks;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.event.CreateGroupEvent;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.group.service.CreateGroupService;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.CircleImageView;
import com.sicoms.smartplug.util.profile.MediaStoreUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class CreateGroupFragment extends Fragment implements CreateGroupResultCallbacks, HttpResponseCallbacks, PictureMenuCallbacks, ImageSelectedResultCallbacks, View.OnKeyListener, OutCallbacks {

    private static final String TAG = CreateGroupFragment.class.getSimpleName();
    private CharSequence mTitle = "그룹 생성";

    private Context mContext;
    private View mView;

    private CreateGroupService mService;
    private CreateGroupEvent mEvent;
    private GroupVo mGroupVo;

    private CircleImageView mIvGroupProfile;
    private ImageView mIvCameraBtn;
    private EditText mEtGroupName;
    private LinearLayout mLlAddPlugBtn;
    private LinearLayout mLlAddMemberBtn;
    private TextView mTvPlugCount;
    private TextView mTvMemberCount;

    private SPEvent mSPEvent;

    public static CreateGroupFragment newInstance() {
        CreateGroupFragment fragment = new CreateGroupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize(){
        Bitmap bitmap = SPUtil.getBackgroundImage(mContext);
        if( bitmap != null) {
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
        mSPEvent = new SPEvent((Activity)mContext);

        mService = new CreateGroupService(mContext);
        mService.setOnHttpResponseCallbacks(this);
        mService.setOnGroupResultCallbacks(this);
        List<UserVo> voList = new ArrayList<>();
        UserVo superUser = LoginService.loadLastLoginUser(mContext);
        superUser.setAuth(SPConfig.MEMBER_MASTER);
        voList.add(superUser);
        mGroupVo = new GroupVo();
        mGroupVo.setUserVoList(voList);
        mGroupVo.setGroupIconImg(SPConfig.GROUP_DEFAULT_IMAGE_NAME + "_00");
        mService.saveCreateGroup(mGroupVo);

        mEvent = new CreateGroupEvent(this, this);
        mEvent.setGroupVo(mGroupVo);
        mEvent.setOnPictureMenuCallbacks(this);
        mEvent.setOnImageSelectedResultCallbacks(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);
        mView =view;
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(this);

        initialize();

        mIvGroupProfile = (CircleImageView) view.findViewById(R.id.iv_group_icon);
        mIvCameraBtn = (ImageView) view.findViewById(R.id.iv_camera_btn);
        mEtGroupName = (EditText) view.findViewById(R.id.et_group_name);
        mLlAddPlugBtn = (LinearLayout) view.findViewById(R.id.ll_add_plug_btn);
        mLlAddMemberBtn = (LinearLayout) view.findViewById(R.id.ll_add_member_btn);
        mTvPlugCount = (TextView) view.findViewById(R.id.tv_plug_count);
        mTvMemberCount = (TextView) view.findViewById(R.id.tv_member_count);

        mIvCameraBtn.setOnClickListener(mEvent);
        mLlAddPlugBtn.setOnClickListener(mEvent);
        mLlAddMemberBtn.setOnClickListener(mEvent);

        setData();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home :
                SPFragment.intentOutFragmentDialog((Activity) mContext, this);
                break;
            case R.id.action_complete :
                SPUtil.showDialog(mContext);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Load DB
                        GroupVo groupVo = mService.loadCreateGroup();
                        if( groupVo == null){
                            Toast.makeText(mContext, "그룹 생성 오류 발생.", Toast.LENGTH_SHORT).show();
                            SPUtil.dismissDialog();
                            return;
                        }
                        if( groupVo.getUserVoList() != null) {
                            UserVo me = groupVo.getUserVoList().get(0);
                            UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                            // 그룹에 자신이 속해있는지 검사.
                            if (!loginVo.getUserId().equalsIgnoreCase(me.getUserId())) {
                                Toast.makeText(mContext, "그룹 생성 오류 발생.", Toast.LENGTH_SHORT).show();
                                SPUtil.dismissDialog();
                                return;
                            }
                        }
                        groupVo.setGroupName(mEtGroupName.getText().toString());
                        // Group 정보 입력 검사
                        if( groupVo.getGroupName().equalsIgnoreCase("")){
                            Toast.makeText(mContext, "그룹 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            SPUtil.dismissDialog();
                            return;
                        }
                        if( groupVo.getPlugVoList() == null && groupVo.getUserVoList() == null){
                            Toast.makeText(mContext, "플러그와 사용자 1개 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
                            SPUtil.dismissDialog();
                            return;
                        }

                        // Cloud Server 저장
                        mGroupVo = groupVo;

                        // Bluetooth Device 검사. 있을 경우 Mesh Group 생성
                        List<PlugVo> apPlugVoList = new ArrayList<>();
                        //List<PlugVo> blPlugVoList = new ArrayList<>();
                        if( groupVo.getPlugVoList() != null) {
                            for (PlugVo plugVo : groupVo.getPlugVoList()) {
                                String type = plugVo.getNetworkType();
                                if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
                                    apPlugVoList.add(plugVo);
                                }
                            }
                            if (apPlugVoList.size() > 0) {
                                SPUtil.showToast(mContext, "AP 타입의 플러그는 그룹을 생성할 수 없습니다.");
                                SPUtil.dismissDialog();
                                return;
                            }
                        }
                        mService.requestInsertCreateGroup(groupVo);
                    }
                });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
    }

    private void setData(){
        if( mGroupVo != null){
            if( mGroupVo.getPlugVoList() != null) {
                mTvPlugCount.setText(String.valueOf(mGroupVo.getPlugVoList().size()));
            }
            if( mGroupVo.getUserVoList() != null){
                mTvMemberCount.setText(String.valueOf(mGroupVo.getUserVoList().size()));
            }
            String imageName = mGroupVo.getGroupIconImg();
            if( imageName.contains(SPConfig.GROUP_DEFAULT_IMAGE_NAME)){
                int resId = SPUtil.getDrawableResourceId(mContext, imageName);
                mIvGroupProfile.setImageResource(resId);
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(SPConfig.FILE_PATH + imageName);
                if (bitmap != null) {
                    mIvGroupProfile.setImageBitmap(bitmap);
                } else {
                    mIvGroupProfile.setImageResource(R.drawable.dgpbg_00);
                    mGroupVo.setGroupIconImg(SPConfig.GROUP_DEFAULT_IMAGE_NAME + "_00");
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK) {
            mGroupVo.setGroupIconImg(SPConfig.GROUP_IMAGE_NAME);
            mService.saveCreateGroup(mGroupVo);
            switch (requestCode) {
                case SPConfig.REQUEST_PICTURE:
                    File pictureFile = new File(SPConfig.FILE_PATH, SPConfig.GROUP_IMAGE_NAME);
                    Uri croppedImage = Uri.fromFile(pictureFile);

                    CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, croppedImage);
                    cropImage.setOutlineColor(0xFF03A9F4);
                    cropImage.setSourceImage(data.getData());

                    startActivityForResult(cropImage.getIntent(mContext), SPConfig.REQUEST_CROP_PICTURE);

                    break;

                case SPConfig.REQUEST_CROP_PICTURE:
                    // When we are done cropping, display it in the ImageView.
                    File croppedFile = new File(SPConfig.FILE_PATH, SPConfig.GROUP_IMAGE_NAME);
                    mIvGroupProfile.setImageBitmap(BitmapFactory.decodeFile(croppedFile.getAbsolutePath()));
                    mIvGroupProfile.setTag(croppedFile);

                    break;

                case SPConfig.REQUEST_CAMERA:
                    final Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap cameraImgBitmap = extras.getParcelable("data");
                        String cameraImageName = mGroupVo.getGroupIconImg();
                        File cameraFile = SPUtil.saveBitmapImage(cameraImageName, cameraImgBitmap);

                        mIvGroupProfile.setImageBitmap(BitmapFactory.decodeFile(cameraFile.getAbsolutePath()));
                        mIvGroupProfile.setTag(cameraFile);
                    }
                    break;
            }
        }
    }

    @Override
    public void onGroupEditMember(UserVo userVo) {

    }

    @Override
    public void onCompleteCreateBLGroup(int groupId, boolean isCreate) {
        if( isCreate){
            mGroupVo.setGroupId(String.valueOf(groupId));
            mService.requestInsertCreateGroup(mGroupVo);
        } else {
            Toast.makeText(mContext, "Bluetooth 그룹 생성 실패", Toast.LENGTH_SHORT).show();
            SPUtil.dismissDialog();
        }
    }

    @Override
    public void onGroupAddPlugList(List<PlugVo> plugVoList) {
        mGroupVo.setPlugVoList(plugVoList);
        mService.saveCreateGroup(mGroupVo);
        setData();
    }

    @Override
    public void onGroupAddMemberList(List<UserVo> userVoList) {
        mGroupVo.setUserVoList(userVoList);
        mService.saveCreateGroup(mGroupVo);
        setData();
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo response = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(response.getResult());
                if( resultNum == HttpConfig.HTTP_SUCCESS) {
                    if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_INSERT_GROUP){
                        GroupVo groupVo = new Gson().fromJson(response.getJsonStr(), GroupVo.class);
                        if (groupVo == null) {
                            SPUtil.dismissDialog();
                            return;
                        }
                        mGroupVo = groupVo;
                        if (mService.insertDbGroup(groupVo)) {
                            Toast.makeText(mContext, "그룹을 생성하였습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "그룹을 생성하지 못했습니다.", Toast.LENGTH_SHORT).show();
                            SPUtil.dismissDialog();
                            return;
                        }

                        File imageFile = (File) mIvGroupProfile.getTag();
                        if( imageFile != null && imageFile.exists()) {
                            File saveFile = new File(SPConfig.FILE_PATH, groupVo.getGroupIconImg());
                            imageFile.renameTo(saveFile);

                            CommonService service = new CommonService(mContext);
                            service.setOnHttpResponseCallbacks(this);
                            service.requestUploadImage(saveFile);
                        }

                        ((Activity)mContext).finish();
                    } else if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPLOAD_COMMON_IMAGE){

                    }

                } else {
                    Toast.makeText(mContext, "그룹을 생성하지 못했습니다.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onPictureMenuResult(int menu) {
        switch (menu){
            case SPConfig.PICTURE_MENU_ALBUM :
                startActivityForResult(MediaStoreUtils.getPickImageIntent(mContext), SPConfig.REQUEST_PICTURE);
                break;
            case SPConfig.PICTURE_MENU_CAMERA :
                if( SPUtil.isIntentAvailable(mContext, MediaStore.ACTION_IMAGE_CAPTURE)) {
                    startActivityForResult(SPUtil.getIntentDefaultCamera(mContext), SPConfig.REQUEST_CAMERA);
                }
                break;
            case SPConfig.PICTURE_MENU_DEFAULT_IMAGE :
                SPFragment.intentGroupGalleryFragment((Activity) mContext, this);
                break;
        }
    }

    @Override
    public void onImageSelectedResult(String imageName) {
        mGroupVo.setGroupIconImg(imageName);

        int resId = SPUtil.getDrawableResourceId(mContext, imageName);
        mIvGroupProfile.setImageResource(resId);
        mIvGroupProfile.setTag(null);
        mService.saveCreateGroup(mGroupVo);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK){
            if( mSPEvent.isBack()) {
                SPFragment.intentOutFragmentDialog((Activity) mContext, this);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void outOutResult() {
        if( ((ActionBarActivity) mContext).getSupportFragmentManager().getBackStackEntryCount()>0) {
            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
        } else {
            ((Activity)mContext).finish();
        }
    }
}
