package com.sicoms.smartplug.menu.service;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbGroupUserMappingVo;
import com.sicoms.smartplug.dao.DbGroupUserMappingVoDao;
import com.sicoms.smartplug.dao.DbPlaceSettingVo;
import com.sicoms.smartplug.dao.DbPlaceSettingVoDao;
import com.sicoms.smartplug.dao.DbPlaceVo;
import com.sicoms.smartplug.dao.DbPlaceVoDao;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 8. 25..
 */
public class PlaceSettingService {
    private static final String TAG = PlaceSettingService.class.getSimpleName();

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;

    public PlaceSettingService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    // 비밀번호 변경
    public void requestUpdateBLPassword(PlaceSettingVo settingVo){
        try {
            CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_PLACE_BL_PASSWORD;
            String jsonBody = new Gson().toJson(settingVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_PLACE_BL_PASSWORD, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    // 비밀번호 동기화
    public void requestSelectBLPassword(){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_SELECT_PLACE_BL_PASSWORD;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            RequestParams params = new RequestParams();
            PlaceSettingVo settingVo = new PlaceSettingVo(placeVo.getPlaceId(), SPConfig.PLACE_SETTING_BL_PASSWORD, "");
            String jsonBody = new Gson().toJson(settingVo);
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_SELECT_PLACE_BL_PASSWORD, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /*
     * Local DB Method
     */
    public boolean updateDbBLPassword(PlaceSettingVo settingVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlaceSettingVoDao theDao = daoSession.getDbPlaceSettingVoDao();
            DbPlaceSettingVo theVo = new DbPlaceSettingVo();

            theVo.setPlaceId(settingVo.getPlaceId());
            theVo.setSetId(settingVo.getSetId());
            theVo.setSetVal(settingVo.getSetVal());

            theDao.insertOrReplace(theVo);

            mDBHelper.closeSession();
            return true;
        }catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }
    public PlaceSettingVo selectDbBLPassword(){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlaceSettingVoDao theDao = daoSession.getDbPlaceSettingVoDao();
            DbPlaceSettingVo theDbVo = theDao.queryBuilder().where(DbPlaceSettingVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).unique();

            PlaceSettingVo placeSettingVo = new PlaceSettingVo(theDbVo.getPlaceId(), theDbVo.getSetId(), theDbVo.getSetVal());

            mDBHelper.closeSession();
            return placeSettingVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        mDBHelper.closeSession();
        return null;
    }
}
