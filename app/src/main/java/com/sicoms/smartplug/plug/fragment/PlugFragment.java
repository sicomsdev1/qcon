package com.sicoms.smartplug.plug.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.main.service.RealtimeService;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.event.PlugEvent;
import com.sicoms.smartplug.plug.interfaces.ControlResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.PlugDBService;
import com.sicoms.smartplug.plug.service.PlugHttpService;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.CircleImageView;
import com.sicoms.smartplug.util.profile.MediaStoreUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlugFragment extends Fragment implements HttpResponseCallbacks, EditNameFinishCallbacks, PictureMenuCallbacks, ImageSelectedResultCallbacks, ControlResultCallbacks {

    private static String TAG = PlugFragment.class.getSimpleName();
    private CharSequence mTitle = "플러그";

    private Context mContext;

    private PlugEvent mEvent;
    private PlugDBService mDBService;
    private PlugHttpService mHttpService;
    private PlugVo mPlugVo;

    private CircleImageView mIvPlugIcon;
    private ImageView mIvSetCameraBtn;
    private ImageView mIvSetLedBtn;
    private ImageView mIvPlugNameEditBtn;
    private RelativeLayout mRlSetScheduleBtn;
    private RelativeLayout mRlSetCutoffBtn;
    private TextView mTvPlugName;
    private TextView mTvRealtimePower;
    private TextView mTvRealtimeWon;
    private TextView mTvForecastPower;
    private TextView mTvForecastWon;

    public static PlugFragment newInstance(PlugVo plugVo) {
        PlugFragment fragment = new PlugFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(plugVo));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
            mPlugVo = new Gson().fromJson(getArguments().getString(TAG), PlugVo.class);
        if (savedInstanceState != null)
            mPlugVo = new Gson().fromJson(savedInstanceState.getString(TAG), PlugVo.class);

        mContext = getActivity();
        mEvent = new PlugEvent(mContext, mPlugVo);
        mEvent.setOnControlResultCallbacks(this);
        mEvent.setEditNameFinishListener(this);
        mEvent.setOnPictureMenuCallbacks(this);
        mEvent.setOnImageSelectedResultCallbacks(this);
        mDBService = new PlugDBService(mContext);
        mHttpService = new PlugHttpService(mContext, this);

        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mPlugVo.getPlugName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_plug, container, false);

        mIvPlugIcon = (CircleImageView) view.findViewById(R.id.iv_plug_icon);
        mIvSetCameraBtn = (ImageView) view.findViewById(R.id.iv_set_camera_btn);
        mIvSetLedBtn = (ImageView) view.findViewById(R.id.iv_set_led_btn);
        mIvPlugNameEditBtn = (ImageView) view.findViewById(R.id.iv_plug_name_edit_btn);
        mRlSetScheduleBtn = (RelativeLayout) view.findViewById(R.id.rl_set_schedule_btn);
        mRlSetCutoffBtn = (RelativeLayout) view.findViewById(R.id.rl_set_cutoff_btn);
        mTvPlugName = (TextView) view.findViewById(R.id.tv_plug_name);
        mTvRealtimePower = (TextView) view.findViewById(R.id.tv_realtime_power);
        mTvRealtimeWon = (TextView) view.findViewById(R.id.tv_realtime_won);
        mTvForecastPower = (TextView) view.findViewById(R.id.tv_forecast_power);
        mTvForecastWon = (TextView) view.findViewById(R.id.tv_forecast_won);

        mIvSetLedBtn.setOnClickListener(mEvent);
        int auth = 1;
        try {
            auth = Integer.parseInt(PlaceService.loadLastPlace(mContext).getAuth());
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        if(auth == SPConfig.MEMBER_MASTER || auth == SPConfig.MEMBER_SETTER) {
            mIvSetCameraBtn.setOnClickListener(mEvent);
            mIvPlugNameEditBtn.setOnClickListener(mEvent);
            mRlSetScheduleBtn.setOnClickListener(mEvent);
            mRlSetCutoffBtn.setOnClickListener(mEvent);
        } else {
            mIvSetCameraBtn.setVisibility(View.GONE);
            mIvPlugNameEditBtn.setVisibility(View.GONE);
            mRlSetScheduleBtn.setVisibility(View.GONE);
            mRlSetCutoffBtn.setVisibility(View.GONE);
        }

        String imageName = mPlugVo.getPlugIconImg();
        if( imageName.contains(SPConfig.PLUG_DEFAULT_IMAGE_NAME)){
            int resId = SPUtil.getDrawableResourceId(mContext, imageName);
            mIvPlugIcon.setImageResource(resId);
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(SPConfig.FILE_PATH + imageName);
            if (bitmap != null) {
                mIvPlugIcon.setImageBitmap(bitmap);
            } else {
                mIvPlugIcon.setImageResource(R.drawable.dppbg_00);
            }
        }
        mTvPlugName.setText(mPlugVo.getPlugName());

        DbLastDataVo lastDataVo = mDBService.selectDbLastData(mPlugVo.getPlugId());
        if( lastDataVo == null){
            lastDataVo = new DbLastDataVo(mPlugVo.getPlugId(), new Date(), 0.0f, 0.0f, SPConfig.STATUS_OFF, SPConfig.STATUS_ON);
            mDBService.updateDbLastStatusDataList(Arrays.asList(lastDataVo));
        }

        String isOn = lastDataVo.getLedOnOff();
        mIvSetLedBtn.setSelected(isOn.equalsIgnoreCase(SPConfig.STATUS_ON));
        if( mIvSetLedBtn.isSelected()){
            mIvSetLedBtn.setImageResource(R.drawable.icon_mood_on);
        } else {
            mIvSetLedBtn.setImageResource(R.drawable.icon_mood_off);
        }

        float wh = lastDataVo.getWh();
        float forecastPower = SPUtil.getForecastPower(wh);

        mTvRealtimePower.setText(String.format("%,d", (int) wh));
        mTvRealtimeWon.setText(SPUtil.getConvertPowerToCharge(wh, 200));
        mTvForecastPower.setText(String.format("%,d", (int) forecastPower));
        mTvForecastWon.setText(SPUtil.getConvertPowerToCharge(forecastPower, 200));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTitle = mPlugVo.getPlugName();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK) {
            CommonService commonService = new CommonService(mContext, this);

            switch (requestCode) {
                case SPConfig.REQUEST_PICTURE:
                    String imageName = SPConfig.PLUG_IMAGE_NAME + "_" + mPlugVo.getPlugId() + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                    File croppedImageFile = SPUtil.createFile(SPConfig.FILE_PATH, imageName);
                    mPlugVo.setPlugIconImg(imageName);

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
                    File imageFile = new File(SPConfig.FILE_PATH, mPlugVo.getPlugIconImg());
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    mIvPlugIcon.setImageBitmap(bitmap);
                    mPlugVo.setPlugIconImg(imageFile.getName());

                    String type = mPlugVo.getNetworkType();
                    if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                        mDBService.updateDbDevice(mPlugVo);
                    } else {
                        commonService.requestUploadImage(imageFile);
                    }

                    break;

                case SPConfig.REQUEST_CAMERA:
                    final Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap cameraImgBitmap = extras.getParcelable("data");
                        String cameraImageName = SPConfig.PLUG_IMAGE_NAME + "_" + mPlugVo.getPlugId() + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        mPlugVo.setPlugIconImg(cameraImageName);
                        File cameraFile = SPUtil.saveBitmapImage(cameraImageName, cameraImgBitmap);

                        mIvPlugIcon.setImageBitmap(cameraImgBitmap);

                        String type1 = mPlugVo.getNetworkType();
                        if( type1.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                            mDBService.updateDbDevice(mPlugVo);
                        } else {
                            commonService.requestUploadImage(cameraFile);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if (resultNum == HttpConfig.HTTP_SUCCESS) {
                    if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_PLUG) {
                        PlugVo plugVo = new Gson().fromJson(responseVo.getJsonStr(), PlugVo.class);
                        if (plugVo == null) {
                            Toast.makeText(mContext, "플러그를 수정하지 못했습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (mDBService.updateDbDevice(plugVo)) {
                            Toast.makeText(mContext, "플러그를 수정하였습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "플러그를 수정하지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPLOAD_COMMON_IMAGE) {
                        mHttpService.requestUpdateDevice(mPlugVo);
                    }
                } else {
                    Toast.makeText(mContext, "플러그를 요청에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        if( SPConfig.IS_TEST){
            if( mDBService.updateDbDevice(mPlugVo)) {
                File imageFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.PLUG_IMAGE_NAME);
                File moveFile = new File(SPConfig.FILE_PATH, mPlugVo.getPlugIconImg());
                imageFile.renameTo(moveFile);

                Toast.makeText(mContext, "플러그를 수정하였습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "플러그를 수정하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onEditNameFinish(String name) {
        mTvPlugName.setText(name);
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
                SPFragment.intentPlugGalleryFragment((Activity) mContext, this);
                break;
        }
    }

    @Override
    public void onImageSelectedResult(String imageName) {
        mPlugVo.setPlugIconImg(imageName);

        int resId = SPUtil.getDrawableResourceId(mContext, imageName);
        mIvPlugIcon.setImageResource(resId);

        String type = mPlugVo.getNetworkType();
        if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
            mDBService.updateDbDevice(mPlugVo);
        } else {
            mHttpService.requestUpdateDevice(mPlugVo);
        }
    }

    @Override
    public void onControlOnOffResult(PlugVo plugVo, boolean isOn) {
        if( plugVo != null) {
            PlugAllService service = new PlugAllService(mContext);
            DbLastDataVo dbLastDataVo = service.selectDbLastData(plugVo);
            if( dbLastDataVo == null){
                dbLastDataVo = new DbLastDataVo(plugVo.getPlugId(), new Date(), 0.0f, 0.0f, SPConfig.STATUS_OFF, SPConfig.STATUS_ON);
            }
            dbLastDataVo.setLedOnOff(isOn ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);
            service.updateDbLastData(dbLastDataVo);

            if( isOn) {
                mIvSetLedBtn.setImageResource(R.drawable.icon_mood_on);
            } else {
                mIvSetLedBtn.setImageResource(R.drawable.icon_mood_off);
            }
        }
    }

    @Override
    public void onGroupControlOnOffResult(List<PlugVo> plugVoList, boolean isOn) {

    }
}
