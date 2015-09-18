package com.sicoms.smartplug.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.AsyncHttpDownloadBitmap;
import com.sicoms.smartplug.network.http.AsyncHttpUploadFile;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.fragment.PlugAllFragment;

import java.io.File;

/**
 * Created by gudnam on 2015. 7. 3..
 */
public class CommonService {
    private static final String TAG = CommonService.class.getSimpleName();
    private static final String ARG_LAST_MENU_NAME = "last_menu";
    private static final String ARG_LAST_PLUG_MAIN_NAME = "last_plug_main";
    private static final String ARG_MENU = "menu";
    private static final String ARG_LAST_PLUG_SYNC_TIME_NAME = "plug_last_sync_time";
    private static final String ARG_LAST_GROUP_SYNC_TIME_NAME = "group_last_sync_time";
    private static final String ARG_LAST_MEMBER_SYNC_TIME_NAME = "member_last_sync_time";
    private static final String ARG_SYNC_TIME = "sync_time";

    private Context mContext;

    private HttpResponseCallbacks mCallbacks;
    private HttpBitmapResponseCallbacks mBitmapCallbacks;

    public CommonService(Context context){
        mContext = context;
    }
    public CommonService(Context context, HttpResponseCallbacks callbacks){
        mContext = context;
        mCallbacks = callbacks;
    }

    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    public void setOnHttpBitmapResponseCallbacks(HttpBitmapResponseCallbacks callbacks){
        mBitmapCallbacks = callbacks;
    }

    // 이미지 다운로드
    public void requestDownloadImage(ImgFileVo imgFileVo){
        //CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_DOWNLOAD_COMMON_IMAGE;

        try{
            String jsonBody = new Gson().toJson(imgFileVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpDownloadBitmap post = new AsyncHttpDownloadBitmap(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_COMMON_IMAGE_DOWNLOAD, "", params);
            if(mBitmapCallbacks != null){
                post.setOnHttpBitmapResponseCallbacks(mBitmapCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 이미지 업로드
    public void requestUploadImage(File file){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPLOAD_COMMON_IMAGE;

        try{
            RequestParams params = new RequestParams();
            final String contentType = RequestParams.APPLICATION_OCTET_STREAM;
            params.put("imgFile", file, contentType);
            params.setHttpEntityIsRepeatable(true);
            params.setUseJsonStreamer(false);

            AsyncHttpUploadFile post = new AsyncHttpUploadFile(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_COMMON_IMAGE_UPLOAD, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 개별 / 그룹 마지막 메뉴 저장
    public static void saveLastMenu(Context context, String menu){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_MENU_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString(ARG_MENU, menu);
        edit.commit();
    }

    public static String loadLastMenu(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_MENU_NAME, 0);
        String menu = preference.getString(ARG_MENU, PlugAllFragment.class.getSimpleName());

        return menu;
    }

    // 대시보드 / 플러그 리스트 화면 저장
    public static void saveLastPlugMainMenu(Context context, int openclose){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_PLUG_MAIN_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putInt(ARG_MENU, openclose);
        edit.commit();
    }

    public static int loadLastPlugMainMenu(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_PLUG_MAIN_NAME, 0);
        int openclose = preference.getInt(ARG_MENU, SPConfig.PLUG_MAIN_CLOSE);

        return openclose;
    }

    // 플러그 동기화 시간 저장
    public static void saveLastPlugSyncTime(Context context, long sec){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_PLUG_SYNC_TIME_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        edit.putLong(placeVo.getPlaceId() + "." + ARG_SYNC_TIME, sec);
        edit.commit();
    }
    public static long loadLastPlugSyncTime(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_PLUG_SYNC_TIME_NAME, 0);
        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        long lastSyncSec = preference.getLong(placeVo.getPlaceId() + "." + ARG_SYNC_TIME, 0);

        return lastSyncSec;
    }

    // 그룹 동기화 시간 저장
    public static void saveLastGroupSyncTime(Context context, long sec){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_GROUP_SYNC_TIME_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        edit.putLong(placeVo.getPlaceId() + "." + ARG_SYNC_TIME, sec);
        edit.commit();
    }
    public static long loadLastGroupSyncTime(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_GROUP_SYNC_TIME_NAME, 0);
        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        long lastSyncSec = preference.getLong(placeVo.getPlaceId() + "." + ARG_SYNC_TIME, 0);

        return lastSyncSec;
    }

    // 멤버 동기화 시간 저장
    public static void saveLastMemberSyncTime(Context context, long sec){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_MEMBER_SYNC_TIME_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        edit.putLong(placeVo.getPlaceId() + "." + ARG_SYNC_TIME, sec);
        edit.commit();
    }
    public static long loadLastMemberSyncTime(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LAST_MEMBER_SYNC_TIME_NAME, 0);
        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        if( placeVo != null) {
            long lastSyncSec = preference.getLong(placeVo.getPlaceId() + "." + ARG_SYNC_TIME, 0);
            return lastSyncSec;
        }
        return -1;
    }
}
