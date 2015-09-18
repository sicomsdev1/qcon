package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.adapter.GroupMemberAdapter;
import com.sicoms.smartplug.group.event.GroupEvent;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.CircleImageView;
import com.sicoms.smartplug.util.profile.MediaStoreUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class GroupNavigationDrawerFragment extends Fragment implements HttpBitmapResponseCallbacks, EditNameFinishCallbacks, HttpResponseCallbacks, EditGroupResultCallbacks, PictureMenuCallbacks, ImageSelectedResultCallbacks {

	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	private Context mContext;
	private View mView;

	private GroupService mService;
	private GroupVo mGroupVo;
	private GroupEvent mEvent;

	private RecyclerView mRecyclerView;
	private GroupMemberAdapter mAdapter;

	private CircleImageView mIvGroupIcon;
	private TextView mTvGroupName;
	private ImageView mIvEditGroupNameBtn;
	private RelativeLayout mRlAddMemberBtn;
	private RelativeLayout mRlGroupOutBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mService = new GroupService(mContext);
		mService.setOnHttpResponseCallbacks(this);
		mGroupVo = mService.loadLastGroup();
		mEvent = new GroupEvent(mContext, mGroupVo);
		mEvent.setFragment(this);
		mEvent.setOnEditNameFinishCallbacks(this);
		mEvent.setOnEditGroupResultCallbacks(this);
		mEvent.setOnPictureMenuCallbacks(this);
		mEvent.setOnImageSelectedResultCallbacks(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		mView = view;

		mIvGroupIcon = (CircleImageView) view.findViewById(R.id.iv_group_icon);
		mTvGroupName = (TextView) view.findViewById(R.id.tv_group_name);
		mIvEditGroupNameBtn = (ImageView) view.findViewById(R.id.iv_edit_group_name_btn);
		mRlAddMemberBtn = (RelativeLayout) view.findViewById(R.id.rl_add_member_btn);
		mRlGroupOutBtn = (RelativeLayout) view.findViewById(R.id.rl_group_out_btn);
		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_group_member);

		mAdapter = new GroupMemberAdapter(mContext);
		mAdapter.SetOnItemClickListener(mEvent);

		mIvGroupIcon.setOnClickListener(mEvent);
		mRlAddMemberBtn.setOnClickListener(mEvent);
		mRlGroupOutBtn.setOnClickListener(mEvent);
		mIvEditGroupNameBtn.setOnClickListener(mEvent);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
		mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
		mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		mRecyclerView.getItemAnimator().setAddDuration(1000);
		mRecyclerView.getItemAnimator().setChangeDuration(1000);
		mRecyclerView.getItemAnimator().setMoveDuration(1000);
		mRecyclerView.getItemAnimator().setRemoveDuration(1000);

		mRecyclerView.setAdapter(mAdapter);

		if (isAdded()) {
			fillAdapterData();
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		fillAdapterData();

		mView.setFocusableInTouchMode(false);
		mView.setOnKeyListener(null);
	}

	private void fillAdapterData() {
		mGroupVo = mService.loadLastGroup();
		mTvGroupName.setText(mGroupVo.getGroupName());
		setProfile();
		List<UserVo> userVoList = mGroupVo.getUserVoList();
		if (userVoList == null) {
			return;
		}
		mAdapter.removeAll();
		mAdapter.addAll(userVoList);
		mAdapter.notifyDataSetChanged();
	}

	private void setProfile(){
		String imageName = mGroupVo.getGroupIconImg();
		if( imageName.contains(SPConfig.GROUP_DEFAULT_IMAGE_NAME)){
			int resId = SPUtil.getDrawableResourceId(mContext, imageName);
			mIvGroupIcon.setImageResource(resId);
		} else {
			String imagePath = SPConfig.FILE_PATH + imageName;
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			if (bitmap != null) {
				mIvGroupIcon.setImageBitmap(bitmap);
			} else {
				mIvGroupIcon.setImageResource(R.drawable.dgpbg_00);
				if (SPUtil.isNetwork(mContext)) {
					CommonService service = new CommonService(mContext);
					service.setOnHttpBitmapResponseCallbacks(this);
					service.requestDownloadImage(new ImgFileVo(mGroupVo.getGroupIconImg()));
				}
			}
		}
	}

	@Override
	public void onEditNameFinish(String name) {
		fillAdapterData();
		SPActivity.intentGroupActivity((Activity)mContext, mGroupVo);
		((Activity)mContext).finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if( resultCode == Activity.RESULT_OK) {
			CommonService commonService = new CommonService(mContext, this);

			switch (requestCode) {
				case SPConfig.REQUEST_PICTURE:
					String imageName = SPConfig.GROUP_IMAGE_NAME + "_" + mGroupVo.getGroupId().replace("@", "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
					File croppedImageFile = new File(SPConfig.FILE_PATH, imageName);
					mGroupVo.setGroupIconImg(imageName);
					mService.saveLastGroupVo(mGroupVo);

					// When the user is done picking a picture, let's start the CropImage Activity,
					// setting the output image file and size to 200x200 pixels square.
					Uri croppedImage = Uri.fromFile(croppedImageFile);

					CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, croppedImage);
					cropImage.setOutlineColor(0xFF03A9F4);
					cropImage.setSourceImage(data.getData());
					startActivityForResult(cropImage.getIntent(mContext), SPConfig.REQUEST_CROP_PICTURE);

					break;

				case SPConfig.REQUEST_CROP_PICTURE:
					// When we are done cropping, display it in the ImageView.
					File imageFile = new File(SPConfig.FILE_PATH, mGroupVo.getGroupIconImg());
					Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
					if (bitmap == null) {
						SPUtil.showToast(mContext, "이미지 저장에 실패하였습니다.");
						return;
					}
					mIvGroupIcon.setImageBitmap(bitmap);

					commonService.requestUploadImage(imageFile);
					mService.saveLastGroupVo(mGroupVo);

					break;

				case SPConfig.REQUEST_CAMERA:
					final Bundle extras = data.getExtras();
					if (extras != null) {
						Bitmap cameraImgBitmap = extras.getParcelable("data");
						String cameraImageName = SPConfig.GROUP_IMAGE_NAME + "_" + mGroupVo.getGroupId().replace("@", "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
						mGroupVo.setGroupIconImg(cameraImageName);
						File cameraFile = SPUtil.saveBitmapImage(cameraImageName, cameraImgBitmap);

						mIvGroupIcon.setImageBitmap(cameraImgBitmap);

						commonService.requestUploadImage(cameraFile);

						mService.saveLastGroupVo(mGroupVo);
					}
					break;
			}
		}
	}

	@Override
	public void onHttpResponseResultStatus(int type, int result, String value) {
		if( result == HttpConfig.HTTP_SUCCESS){
			try {
				HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
				int resultNum = Integer.parseInt(responseVo.getResult());
				if (resultNum == HttpConfig.HTTP_SUCCESS) {
					if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPLOAD_COMMON_IMAGE) {
						mService.requestUpdateGroup(mGroupVo);
					} else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_GROUP) {
						mGroupVo = new Gson().fromJson(responseVo.getJsonStr(), GroupVo.class);
						if (mGroupVo == null) {
							Toast.makeText(mContext, "그룹 이미지를 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
							return;
						}
						if (mService.updateDbGroup(mGroupVo)) {
							mService.saveLastGroupVo(mGroupVo);
							Toast.makeText(mContext, "그룹 이미지를 변경하였습니다.", Toast.LENGTH_SHORT).show();
							SPUtil.dismissDialog();
							fillAdapterData();
						} else {
							Toast.makeText(mContext, "이름을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					SPUtil.showToast(mContext, "그룹 이미지 변경에 실패하였습니다.");
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
		if( result == HttpConfig.HTTP_SUCCESS) {
			SPUtil.saveBitmapImage(fileName, bitmap);
			setProfile();
		}
	}

	@Override
	public void onCompleteEditPlug() {

	}

	@Override
	public void onCompleteEditMember() {
		fillAdapterData();
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
				SPFragment.intentGroupGalleryFragment((Activity)mContext, this);
				break;
		}
	}

	@Override
	public void onImageSelectedResult(String imageName) {
		mGroupVo.setGroupIconImg(imageName);

		int resId = SPUtil.getDrawableResourceId(mContext, imageName);
		mIvGroupIcon.setImageResource(resId);
		mService.requestUpdateGroup(mGroupVo);
	}
}
