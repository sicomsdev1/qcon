package com.sicoms.smartplug.member.service;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbUserVo;
import com.sicoms.smartplug.dao.DbUserVoDao;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserPlaceMappingVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 16..
 */
public class MemberService {
    private Context mContext;
    private HttpResponseCallbacks mCallbacks;
    private DBHelper mDBHelper;

    public MemberService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    public void requestSelectMemberList(){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_SELECT_USER_LIST;
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try {
            String jsonBody = new Gson().toJson(placeVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_SELECT_USER_LIST, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestInsertMember(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_USER;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            UserPlaceMappingVo userPlaceMappingVo = new UserPlaceMappingVo(userVo, placeVo);
            String jsonBody = new Gson().toJson(userPlaceMappingVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_USER, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestUpdateMember(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_USER;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            UserPlaceMappingVo userPlaceMappingVo = new UserPlaceMappingVo(userVo, placeVo);
            String jsonBody = new Gson().toJson(userPlaceMappingVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_USER, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestDeleteMember(List<UserVo> userVoList){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_DELETE_USER;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(userVoList);
            RequestParams params = new RequestParams();
            params.add("placeId", placeVo.getPlaceId());
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_DELETE_USER, "", params);
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
    public List<UserVo> selectDbMemberList(){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = theDao.queryBuilder().where(DbUserVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();
            if (theDbVoList == null) {
                return null;
            }
            List<UserVo> userVoList = new ArrayList<>();
            List<UserVo> masterUserVoList = new ArrayList<>();
            for (DbUserVo vo : theDbVoList) {
                UserVo userVo = new UserVo(vo.getUserId(), vo.getUserName(), vo.getAuth(), vo.getProfileImg(), true);
                if( vo.getAuth() == SPConfig.MEMBER_MASTER){
                    masterUserVoList.add(userVo);
                } else {
                    userVoList.add(userVo);
                }
            }
            for( UserVo userVo : masterUserVoList){
                userVoList.add(0, userVo);
            }
            mDBHelper.closeSession();
            return userVoList;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public List<UserVo> selectDbMemberList(PlaceVo placeVo){
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = theDao.queryBuilder().where(DbUserVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();
            if (theDbVoList == null) {
                return null;
            }
            List<UserVo> userVoList = new ArrayList<>();
            List<UserVo> masterUserVoList = new ArrayList<>();
            for (DbUserVo vo : theDbVoList) {
                UserVo userVo = new UserVo(vo.getUserId(), vo.getUserName(), vo.getAuth(), vo.getProfileImg(), true);
                if( vo.getAuth() == SPConfig.MEMBER_MASTER){
                    masterUserVoList.add(userVo);
                } else {
                    userVoList.add(userVo);
                }
            }
            for( UserVo userVo : masterUserVoList){
                userVoList.add(0, userVo);
            }
            mDBHelper.closeSession();
            return userVoList;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public boolean insertDbMemberList(List<UserVo> userVoList){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        UserVo loginVo = LoginService.loadLastLoginUser(mContext);
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = theDao.queryBuilder()
                    .where(DbUserVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();
            theDao.deleteInTx(theDbVoList);
            theDbVoList = new ArrayList<>();

            for(UserVo userVo : userVoList){
                DbUserVo dbUserVo = new DbUserVo();
                dbUserVo.setPlaceId(placeVo.getPlaceId());
                dbUserVo.setUserId(userVo.getUserId());
                dbUserVo.setUserName(userVo.getUserName());
                dbUserVo.setProfileImg(userVo.getUserProfileImg());
                dbUserVo.setAuth(userVo.getAuth());
                theDbVoList.add(dbUserVo);

                if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){
                    placeVo.setAuth(String.valueOf(userVo.getAuth()));
                    PlaceService.saveLastPlace(mContext, placeVo);
                }
            }

            theDao.insertOrReplaceInTx(theDbVoList);

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

    public boolean insertDbMemberList(List<UserVo> userVoList, PlaceVo placeVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = theDao.queryBuilder()
                    .where(DbUserVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();
            theDao.deleteInTx(theDbVoList);
            theDbVoList = new ArrayList<>();

            for(UserVo userVo : userVoList){
                DbUserVo dbUserVo = new DbUserVo();
                dbUserVo.setPlaceId(placeVo.getPlaceId());
                dbUserVo.setUserId(userVo.getUserId());
                dbUserVo.setUserName(userVo.getUserName());
                dbUserVo.setProfileImg(userVo.getUserProfileImg());
                dbUserVo.setAuth(userVo.getAuth());
                theDbVoList.add(dbUserVo);
            }

            theDao.insertOrReplaceInTx(theDbVoList);

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

    public boolean insertDbMember(UserVo userVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            DbUserVo dbUserVo = new DbUserVo();
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
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

    public boolean deleteDbMember(UserVo userVo){
        try{
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            DbUserVo theDbVo = theDao.queryBuilder().where(DbUserVoDao.Properties.PlaceId.eq(placeVo.getPlaceId()))
                                                    .where(DbUserVoDao.Properties.UserId.eq(userVo.getUserId()))
                                                    .unique();

            theDao.delete(theDbVo);
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

    public boolean deleteDbMemberList(List<UserVo> userVoList){
        try{
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = new ArrayList<>();

            for(int voCnt=0; voCnt<userVoList.size(); voCnt++){
                UserVo userVo = userVoList.get(voCnt);
                theDbVoList.add(new DbUserVo(placeVo.getPlaceId(), userVo.getUserId(), userVo.getUserName(), userVo.getUserProfileImg(), userVo.getAuth()));
            }

            theDao.deleteInTx(theDbVoList);
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

    public boolean deleteDbMemberList(List<UserVo> userVoList, PlaceVo placeVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = new ArrayList<>();

            for(int voCnt=0; voCnt<userVoList.size(); voCnt++){
                UserVo userVo = userVoList.get(voCnt);
                theDbVoList.add(new DbUserVo(placeVo.getPlaceId(), userVo.getUserId(), userVo.getUserName(), userVo.getUserProfileImg(), userVo.getAuth()));
            }

            theDao.deleteInTx(theDbVoList);
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

    public boolean deleteDbMemberAllInPlace(){
        try{
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DaoSession daoSession = mDBHelper.getSession(true);
            DbUserVoDao theDao = daoSession.getDbUserVoDao();
            List<DbUserVo> theDbVoList = theDao.queryBuilder().where(DbUserVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();

            theDao.deleteInTx(theDbVoList);
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
}
