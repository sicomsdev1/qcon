package com.sicoms.smartplug.menu.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPEvent;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.common.interfaces.OutCallbacks;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.event.PlaceEvent;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.interfaces.PlaceResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.test.map.MyLocation;
import com.sicoms.smartplug.util.encrypt.AES256Util;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.MediaStoreUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class PlaceMapFragment extends Fragment implements HttpResponseCallbacks, PictureMenuCallbacks, ImageSelectedResultCallbacks, View.OnKeyListener, OutCallbacks {
    private static final String TAG = PlaceMapFragment.class.getSimpleName();
    static final LatLng SEOUL = new LatLng( 37.56, 126.97);

    private Context mContext;
    private static View stView;
    private GoogleMap map;
    private PlaceEvent mEvent;
    private PlaceService mService;
    private static PlaceResultCallbacks mCallbacks;
    private PlaceVo mPlaceVo;

    private EditText mEtPlaceName;
    private RelativeLayout mRlMap;
    private RelativeLayout mRlChangePictureBtn;
    private ImageView mIvPlaceImg;
    private ImageView mIvFinishBtn;

    private String mCurrentImageName;

    private SPEvent mSPEvent;

    public static PlaceMapFragment newInstance(PlaceVo placeVo, PlaceResultCallbacks callbacks) {
        PlaceMapFragment fragment = new PlaceMapFragment();
        Bundle args = new Bundle();
        args.putString(TAG, new Gson().toJson(placeVo));
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }
    public static PlaceMapFragment newInstance(PlaceResultCallbacks callbacks) {
        PlaceMapFragment fragment = new PlaceMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    private void initialize(){
        Bitmap bitmap = SPUtil.getBackgroundImage(mContext);
        if( bitmap != null) {
            stView.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        mSPEvent = new SPEvent();
        Bundle args = getArguments();
        if (args != null)
            mPlaceVo = new Gson().fromJson(getArguments().getString(TAG), PlaceVo.class);
        if (savedInstanceState != null)
            mPlaceVo = new Gson().fromJson(savedInstanceState.getString(TAG), PlaceVo.class);

        mEvent = new PlaceEvent(mContext);
        mEvent.setOnPictureMenuCallbacks(this);
        mService = new PlaceService(mContext);
        mService.setOnHttpResponseCallbacks(this);
        if( mPlaceVo == null){
            mPlaceVo = new PlaceVo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( stView != null){
            ViewGroup parent = (ViewGroup) stView.getParent();
            if( parent != null){
                parent.removeView(stView);
            }
        }
        try {
            stView = inflater.inflate(R.layout.fragment_place_map, container, false);
        } catch( InflateException ie){
            ie.printStackTrace();
        }
        stView.setFocusableInTouchMode(true);
        stView.requestFocus();
        stView.setOnKeyListener(this);

        initialize();

        map = ((MapFragment) ((Activity)mContext).getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (map == null) {
            Toast.makeText(mContext, "지도 연동에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return stView;
        }
        map.setMyLocationEnabled(true);
        LatLng lastPosition;
        List<Double> lastCoordinate = mService.loadLastCoordinate();
        if (lastCoordinate == null) {
            lastPosition = SEOUL;
        } else {
            lastPosition = new LatLng(lastCoordinate.get(0), lastCoordinate.get(1));
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15));

        mEtPlaceName = (EditText) stView.findViewById(R.id.et_place_name);
        mRlMap = (RelativeLayout) stView.findViewById(R.id.rl_map);
        mRlChangePictureBtn = (RelativeLayout) stView.findViewById(R.id.rl_change_picture_btn);
        mIvPlaceImg = (ImageView) stView.findViewById(R.id.iv_place_img);
        mIvFinishBtn = (ImageView) stView.findViewById(R.id.iv_finish_btn);
        mEtPlaceName.setText("");

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String imageName = mPlaceVo.getPlaceImg();
                if( imageName.contains(SPConfig.PLACE_DEFAULT_IMAGE_NAME)){
                    int resId = SPUtil.getDrawableResourceId(mContext, imageName);
                    mIvPlaceImg.setImageResource(resId);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(SPConfig.FILE_PATH + imageName);
                    if (bitmap != null) {
                        mIvPlaceImg.setImageBitmap(bitmap);
                    } else {
                        mIvPlaceImg.setImageResource(R.drawable.dpbg_01);
                    }
                }
            }
        });


        mIvFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( !mPlaceVo.getPlaceId().equalsIgnoreCase("")) {
                    // 권한 확인
                    if (!mPlaceVo.getAuth().equalsIgnoreCase(String.valueOf(SPConfig.MEMBER_MASTER))) {
                        SPUtil.showToast(mContext, "해당 플레이스를 변경할 권한이 없습니다.");
                        return;
                    }
                }

                if( mEtPlaceName.getText().toString().equalsIgnoreCase("")){
                    Toast.makeText(mContext, "장소 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPlaceVo.setPlaceName(mEtPlaceName.getText().toString());
                mPlaceVo.setAuth(String.valueOf(SPConfig.MEMBER_MASTER));

                // 위치 암호화
                try {
                    AES256Util aes = new AES256Util();
                    mPlaceVo.setAddress(aes.encode(mPlaceVo.getAddress()));
                    mPlaceVo.setCoordinate(aes.encode(mPlaceVo.getCoordinate()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }

                if( mPlaceVo.getPlaceId().equalsIgnoreCase("")) {
                    // Add
                    mService.requestInsertPlaceInfo(mPlaceVo);
                } else {
                    // Modify
                    mService.requestUpdatePlaceInfo(mPlaceVo);
                }
                SPUtil.showDialog(mContext);
            }
        });

        setData();

        return stView;
    }

    public void setData(){
        mRlChangePictureBtn.setVisibility(View.VISIBLE);
        mIvFinishBtn.setVisibility(View.VISIBLE);
        mEtPlaceName.setEnabled(true);
        mRlChangePictureBtn.setOnClickListener(mEvent);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                drawMarker(latLng.latitude, latLng.longitude);
            }
        });

        if( mPlaceVo.getPlaceId().equalsIgnoreCase("")){
            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    drawMarker(location.getLatitude(), location.getLongitude());
                }
            };

            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(mContext, locationResult);
        } else {
            String []coordinate = mPlaceVo.getCoordinate().split(",");
            if( coordinate != null && coordinate.length == 2) {
                Double latitude = Double.parseDouble(coordinate[0]);
                Double longitude = Double.parseDouble(coordinate[1]);
                drawMarker(latitude, longitude);
            }

            //mRlMap.setVisibility(View.INVISIBLE);
            mEtPlaceName.setText(mPlaceVo.getPlaceName());
            String imageName = mPlaceVo.getPlaceImg();
            if( imageName.contains(SPConfig.PLACE_DEFAULT_IMAGE_NAME)){
                int resId = SPUtil.getDrawableResourceId(mContext, imageName);
                mIvPlaceImg.setImageResource(resId);
            } else {
                String imagePath = SPConfig.FILE_PATH + imageName;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if( bitmap != null) {
                    mIvPlaceImg.setImageBitmap(bitmap);
                } else {
                    mIvPlaceImg.setImageResource(R.drawable.dpbg_01);
                }
            }

            if( !mPlaceVo.getAuth().equalsIgnoreCase(String.valueOf(SPConfig.MEMBER_MASTER))) {
                mEtPlaceName.setEnabled(false);
                mRlChangePictureBtn.setVisibility(View.INVISIBLE);
                mRlChangePictureBtn.setOnClickListener(null);
                map.setOnMapClickListener(null);
                mIvFinishBtn.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem outItem = menu.findItem(R.id.action_out);
        if( !mPlaceVo.getPlaceId().equalsIgnoreCase("")) {
            outItem.setVisible(true);
        }
        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SPFragment.intentOutFragmentDialog((Activity) mContext, this);
                break;
            case R.id.action_out :
                mService.requestDeletePlaceUser(mPlaceVo);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void drawMarker(final Double latitude, final Double longitude) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //기존 마커 지우기
                map.clear();
                LatLng currentPosition = new LatLng(latitude, longitude);
                mService.saveLastCoordinate(latitude, longitude);

                //currentPosition 위치로 카메라 중심을 옮기고 화면 줌을 조정한다. 줌범위는 2~21, 숫자클수록 확대
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17));
                map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

                //마커 추가
                map.addMarker(new MarkerOptions()
                        .position(currentPosition)
                        .snippet("Lat:" + latitude + "Lng:" + longitude)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title("현재위치"));
            }
        });

        Geocoder coder = new Geocoder(mContext, Locale.KOREA);
        try {
            List<Address> addressList = coder.getFromLocation(latitude, longitude, 1);
            if( addressList != null){
                Address address = addressList.get(0);
                String placeAddress = "";
                for(int cnt=0; cnt<=address.getMaxAddressLineIndex(); cnt++){
                    String addLine = address.getAddressLine(cnt);
                    placeAddress += String.format("%s", addLine);
                }
                String country = address.getCountryName();
                placeAddress = placeAddress.replaceFirst(country + " ", "");
                mPlaceVo.setAddress(placeAddress);
                mPlaceVo.setCoordinate(latitude + "," + longitude);
            }
        }catch (IOException ie){
            ie.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SPConfig.REQUEST_PICTURE:
                    File pictureFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.PLACE_IMAGE_NAME);
                    Uri croppedImage = Uri.fromFile(pictureFile);

                    CropImageIntentBuilder cropImage = new CropImageIntentBuilder(500, 500, croppedImage);
                    cropImage.setOutlineColor(0xFF03A9F4);
                    cropImage.setSourceImage(data.getData());

                    startActivityForResult(cropImage.getIntent(mContext), SPConfig.REQUEST_CROP_PICTURE);

                    break;

                case SPConfig.REQUEST_CROP_PICTURE:
                    // When we are done cropping, display it in the ImageView.
                    File croppedFile = SPUtil.createFile(SPConfig.FILE_PATH, SPConfig.PLACE_IMAGE_NAME);
                    mIvPlaceImg.setImageBitmap(BitmapFactory.decodeFile(croppedFile.getAbsolutePath()));
                    mIvPlaceImg.setTag(croppedFile);
                    if (!mPlaceVo.getPlaceId().equalsIgnoreCase("")) {
                        mCurrentImageName = mPlaceVo.getPlaceImg();
                        mPlaceVo.setPlaceImg(SPConfig.PLACE_IMAGE_NAME + "_" + mPlaceVo.getPlaceId() + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
                    }

                    break;

                case SPConfig.REQUEST_CAMERA:
                    final Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap cameraImgBitmap = extras.getParcelable("data");
                        String cameraImageName = SPConfig.PLACE_IMAGE_NAME + "_" + mPlaceVo.getPlaceId() + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        File cameraFile = SPUtil.saveBitmapImage(cameraImageName, cameraImgBitmap);
                        mIvPlaceImg.setImageBitmap(BitmapFactory.decodeFile(cameraFile.getAbsolutePath()));
                        mIvPlaceImg.setTag(cameraFile);

                        if (!mPlaceVo.getPlaceId().equalsIgnoreCase("")) {
                            mCurrentImageName = mPlaceVo.getPlaceImg();
                            mPlaceVo.setPlaceImg(SPConfig.PLACE_IMAGE_NAME + "_" + mPlaceVo.getPlaceId() + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
                        }
                    }

                    break;
            }
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {

//        result = HttpConfig.HTTP_SUCCESS;
//        value = UUID.randomUUID().toString();

        if (result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                    if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_INSERT_PLACE) {
                        if (resultNum == HttpConfig.HTTP_SUCCESS) {
                            // 응답으로 Place 정보 수신 (Place Id, Place Image Name 서버에서 할당)
                            mPlaceVo = new Gson().fromJson(responseVo.getJsonStr(), PlaceVo.class);
                            if (mPlaceVo == null) {
                                SPUtil.dismissDialog();
                                return;
                            }
                            File imageFile = (File) mIvPlaceImg.getTag();
                            if( imageFile != null && imageFile.exists()){
                                // 사용자 직접 저장한 이미지
                                File saveFile = new File(SPConfig.FILE_PATH, mPlaceVo.getPlaceImg());
                                imageFile.renameTo(saveFile);

                                CommonService service = new CommonService(mContext);
                                service.setOnHttpResponseCallbacks(this);
                                service.requestUploadImage(saveFile);
                            }
                            PlaceService.saveLastPlace(mContext, mPlaceVo);
                            if (mCallbacks != null) {
                                mCallbacks.onAddPlaceResult(mPlaceVo);
                            }
                            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(mContext, "플레이스 요청에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPDATE_PLACE) {
                        if (resultNum == HttpConfig.HTTP_SUCCESS) {
                            mPlaceVo = new Gson().fromJson(responseVo.getJsonStr(), PlaceVo.class);
                            if (mPlaceVo == null) {
                                SPUtil.dismissDialog();
                                return;
                            }
                            if( mCurrentImageName != null &&
                                    !mCurrentImageName.equalsIgnoreCase("") &&
                                    !mCurrentImageName.equalsIgnoreCase(mPlaceVo.getPlaceImg())) { // 이미지 이름이 변경돼있으면 이미지 수정
                                File imageFile = (File) mIvPlaceImg.getTag();
                                if( imageFile != null && imageFile.exists()) {
                                    File moveFile = new File(SPConfig.FILE_PATH, mPlaceVo.getPlaceImg());
                                    imageFile.renameTo(moveFile);

                                    CommonService service = new CommonService(mContext);
                                    service.setOnHttpResponseCallbacks(this);
                                    service.requestUploadImage(moveFile);
                                }
                            }
                            PlaceService.saveLastPlace(mContext, mPlaceVo);

                            if (mCallbacks != null) {
                                mCallbacks.onModPlaceResult(mPlaceVo);
                            }
                            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(mContext, "플레이스 요청에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_OUT_PLACE) {
                        if (resultNum == HttpConfig.HTTP_SUCCESS) {
                            mPlaceVo = new Gson().fromJson(responseVo.getJsonStr(), PlaceVo.class);
                            if (mPlaceVo == null) {
                                SPUtil.dismissDialog();
                                return;
                            }
                            if (mCallbacks != null) {
                                mCallbacks.onOutPlaceResult(mPlaceVo);
                            }
                            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                        } else if( resultNum == -1){
                            SPUtil.showToast(mContext, "플레이스에 한명 이상의 마스터가 있어야 합니다.");
                        } else {
                            SPUtil.showToast(mContext, "플레이스 요청에 실패하였습니다.");
                        }
                    } else if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_UPLOAD_COMMON_IMAGE){
                        if (resultNum != HttpConfig.HTTP_SUCCESS) {
                            SPUtil.showToast(mContext, "클라우드 서버에 이미지를 저장하지 못했습니다.");
                        }
                    }
            } catch (JsonParseException jpe) {
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
                SPFragment.intentPlaceGalleryFragment((Activity)mContext, this);
                break;
        }
    }

    @Override
    public void onImageSelectedResult(String imageName) {
        int resId = SPUtil.getDrawableResourceId(mContext, imageName);
        mIvPlaceImg.setImageResource(resId);
        mPlaceVo.setPlaceImg(imageName);
        mCurrentImageName = mPlaceVo.getPlaceImg();
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
    public void onOutResult() {
        if (((ActionBarActivity) mContext).getSupportFragmentManager().getBackStackEntryCount() > 0) {
            ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
        } else {
            ((Activity)mContext).finish();
        }
    }
}
