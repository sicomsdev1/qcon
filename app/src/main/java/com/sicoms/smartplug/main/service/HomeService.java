package com.sicoms.smartplug.main.service;

import android.app.Activity;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.dao.DbLastDataVoDao;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.dao.DbUserVo;
import com.sicoms.smartplug.dao.DbUserVoDao;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

/**
 * Created by gudnam on 2015. 6. 9..
 */
public class HomeService {
    private Activity mActivity;
    private Fragment mFragment;
    private DBHelper mDBHelper;

    private HttpResponseCallbacks mCallbacks;

    public HomeService(Activity activity){
        mActivity = activity;
        mDBHelper = new DBHelper(mActivity);
    }
    public HomeService(Fragment fragment){
        mActivity = fragment.getActivity();
        mFragment = fragment;
        mDBHelper = new DBHelper(mActivity);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    public void requestUpdateMembershipProfile(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_MEMBERSHIP_PROFILE;
        try {
            String jsonBody = new Gson().toJson(userVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mActivity, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_MEMBERSHIP_PROFILE, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestUpdateMembershipName(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_MEMBERSHIP_NAME;
        try {
            String jsonBody = new Gson().toJson(userVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mActivity, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_MEMBERSHIP_NAME, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
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
    public boolean updateDbUser(UserVo userVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            DbUserVo dbUserVo = new DbUserVo();
            PlaceVo placeVo = PlaceService.loadLastPlace(mActivity);
            dbUserVo.setPlaceId(placeVo.getPlaceId());
            dbUserVo.setUserId(userVo.getUserId());
            dbUserVo.setUserName(userVo.getUserName());
            dbUserVo.setProfileImg(userVo.getUserProfileImg());
            dbUserVo.setAuth(userVo.getAuth());

            theDao.insertOrReplace(dbUserVo);

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

    public DbLastDataVo selectDbLastData(String plugId){
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            DbLastDataVo dbLastDataVo = theDao.queryBuilder().where(DbPlugVoDao.Properties.PlugId.eq(plugId)).unique();

            return dbLastDataVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
