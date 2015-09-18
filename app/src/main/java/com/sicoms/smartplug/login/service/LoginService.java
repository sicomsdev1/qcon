package com.sicoms.smartplug.login.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbPhoneVo;
import com.sicoms.smartplug.dao.DbUserVo;
import com.sicoms.smartplug.dao.DbPhoneVoDao;
import com.sicoms.smartplug.dao.DbUserVoDao;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.gcm.GcmService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 5..
 */
public class LoginService {

    private static final String ARG_LOGIN_NAME = "login";
    private static final String ARG_LOGIN_LAST_USER = "login_last_user";
    private static final String ARG_GCM_ID = "login_gcm_id";

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;

    public LoginService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }

    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    public void requestInsertMembership(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_MEMBERSHIP_INSERT;
        try {
            String jsonBody = new Gson().toJson(userVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_MEMBERSHIP_INSERT, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Login
    public void requestLoginMessage(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_AUTH_LOGIN;
        try {
            String userId = userVo.getUserId();
            String password = userVo.getPassword();
            String gcmId = userVo.getGcmId();

            RequestParams params = new RequestParams();
            params.add("userId", userId);
            params.add("password", password);
            params.add("gcmId", gcmId);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_AUTH_LOGIN, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if( mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }

            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Logout
    public void requestLogout(){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_AUTH_LOGOUT;
        try {
            RequestParams params = new RequestParams();
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_AUTH_LOGOUT, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if( mCallbacks != null) {
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
    public UserVo selectDbPhone(){

        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            UserVo lastUserVo = loadLastLoginUser(mContext);
            if( lastUserVo == null){
                return null;
            }

            DbPhoneVoDao dbPhoneVoDao = daoSession.getDbPhoneVoDao();
            List<DbPhoneVo> dbPhoneVoList = dbPhoneVoDao.queryBuilder().list();

            if (dbPhoneVoList != null) {
                for (DbPhoneVo vo : dbPhoneVoList) {
                    String userId = vo.getUserId();

                    if (userId.equalsIgnoreCase(lastUserVo.getUserId())) {
                        return lastUserVo;
                    }
                }

                mDBHelper.closeSession();
                return lastUserVo;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public boolean insertDbLoginUser(UserVo userVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> dbUserVoList = new ArrayList<>();
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            DbUserVo dbUserVo = new DbUserVo();
            dbUserVo.setPlaceId(placeVo.getPlaceId());
            dbUserVo.setUserId(userVo.getUserId());
            dbUserVo.setUserName(userVo.getUserName());
            dbUserVo.setProfileImg(userVo.getUserProfileImg());
            dbUserVo.setAuth(Integer.parseInt(placeVo.getAuth()));
            dbUserVoList.add(dbUserVo);

            theDao.insertOrReplaceInTx(dbUserVoList);

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

    /*
     * Memory DB Method
     */
    public static void saveLastLoginUser(Context context, UserVo vo){
        SharedPreferences preference = context.getSharedPreferences(ARG_LOGIN_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString(ARG_LOGIN_LAST_USER, new Gson().toJson(vo));
        edit.commit();
    }

    public static UserVo loadLastLoginUser(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LOGIN_NAME, 0);
        UserVo userVo = new Gson().fromJson(preference.getString(ARG_LOGIN_LAST_USER, ""), UserVo.class);
        if( userVo == null){
            userVo = new UserVo();
        }
        return userVo;
    }

    private void saveLastGCMId(Context context, String gcmId){
        SharedPreferences preference = context.getSharedPreferences(ARG_LOGIN_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString(ARG_GCM_ID, gcmId);
        edit.commit();
    }

    private String loadLastGCMId(Context context){
        SharedPreferences preference = context.getSharedPreferences(ARG_LOGIN_NAME, 0);
        String gcmId = preference.getString(ARG_GCM_ID, "");

        return gcmId;
    }

    /*
     * Business Logic
     */
    public String getGCMId(Context context){
        String gcmId = loadLastGCMId(context);
        if( gcmId.equalsIgnoreCase("")) {
            GcmService gcmService = new GcmService(mContext);
            gcmId = gcmService.registGCM();
            if (gcmId == null) {
                gcmId = "";
            }
            saveLastGCMId(context, gcmId);
        }
        return gcmId;
    }
}
