package com.sicoms.smartplug.menu.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbPlaceVo;
import com.sicoms.smartplug.dao.DbPlugVo;
import com.sicoms.smartplug.dao.DbUserVo;
import com.sicoms.smartplug.dao.DbPlaceVoDao;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.dao.DbUserVoDao;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.encrypt.AES256Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 5..
 */
public class PlaceService {
    private static final String TAG = PlaceService.class.getSimpleName();

    public static final String ARG_PLACE_NAME = "place";
    public static final String ARG_PLACE_LAST_NAME = "place_last";
    public static final String ARG_PLACE_LAST_COORDINATE_LATITUDE_NAME = "place_last_coordinate_latitude";
    public static final String ARG_PLACE_LAST_COORDINATE_LONGITUDE_NAME = "place_last_coordinate_longitude";

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;

    public PlaceService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }

    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
    /*
     * Cloud Server Method
     */
    // 플레이스 리스트 가져오기
    public void requestSelectPlaceList(UserVo userVo){
        try {
            CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_SELECT_PLACE_LIST;
            String jsonBody = new Gson().toJson(userVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_SELECT_PLACE_LIST, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 플레이스 리스트 추가
    public void requestInsertPlaceInfo(PlaceVo placeVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_PLACE;

        try{
            String jsonBody = new Gson().toJson(placeVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_PLACE, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 플레이스 수정
    public void requestUpdatePlaceInfo(PlaceVo vo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_PLACE;

        try{
            String jsonBody = new Gson().toJson(vo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_PLACE_, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 플레이스 나가기
    public void requestDeletePlaceUser(PlaceVo placeVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_OUT_PLACE;

        try{
            String jsonBody = new Gson().toJson(placeVo);

            RequestParams params = new RequestParams();
            params.put("placeId", placeVo.getPlaceId());
            params.put("jsonStr", jsonBody);

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_OUT_PLACE, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 유저 정보 비교
    public void requestCheckUserInfo(String placeId){

    }

    // 그룹 정보 비교
    public void requestCheckGroupInfo(String placeId){

    }
    //////////////

    /*
     * Local DB Method
     */
    public boolean updateDbPlaceList(List<PlaceVo> placeVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlaceVoDao theDao = daoSession.getDbPlaceVoDao();
            theDao.deleteAll();
            List<DbPlaceVo> dbPlaceVoList = new ArrayList<>();

            AES256Util aes = new AES256Util();
            for(PlaceVo vo : placeVoList){
                String address = aes.decode(vo.getAddress());
                String coordinate = aes.decode(vo.getCoordinate());
                dbPlaceVoList.add(new DbPlaceVo(vo.getPlaceId(), vo.getPlaceName(), vo.getPlaceImg(), address, coordinate, vo.getAuth(), vo.getPlugCount(), vo.getMemberCount()));
            }

            theDao.insertOrReplaceInTx(dbPlaceVoList);

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
    public List<PlaceVo> selectDbPlaceList(){

        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            ArrayList<PlaceVo> voList = new ArrayList<>();
            PlaceVo lastPlaceVo = loadLastPlace(mContext);

            DbPlaceVoDao dbPlaceVoDao = daoSession.getDbPlaceVoDao();
            List<DbPlaceVo> dbPlaceVoList = dbPlaceVoDao.queryBuilder().list();

            if (dbPlaceVoList != null) {
                for (DbPlaceVo vo : dbPlaceVoList) {
                    String placeId = vo.getPlaceId();
                    String placeName = vo.getPlaceName();
                    String address = vo.getAddress();
                    String coordinate = vo.getCoordinate();
                    String placeImg = vo.getPlaceImg();
                    String auth = vo.getAuth();
                    int plugCount = vo.getPlugCount();
                    int memberCount = vo.getMemberCount();
                    boolean isHere = false;

                    if( lastPlaceVo != null) {
                        if (placeId.equalsIgnoreCase(lastPlaceVo.getPlaceId())) {
                            isHere = true;
                        }
                    }
                    voList.add(new PlaceVo(placeId, placeName, address, coordinate, placeImg, auth, isHere, plugCount, memberCount));
                }

                mDBHelper.closeSession();
                return voList;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public boolean updateDbPlace(PlaceVo placeVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlaceVoDao theDao = daoSession.getDbPlaceVoDao();
            DbPlaceVo dbPlaceVo = new DbPlaceVo();

            dbPlaceVo.setPlaceId(placeVo.getPlaceId());
            dbPlaceVo.setPlaceName(placeVo.getPlaceName());
            dbPlaceVo.setAddress(placeVo.getAddress());
            dbPlaceVo.setCoordinate(placeVo.getCoordinate());
            dbPlaceVo.setPlaceImg(placeVo.getPlaceImg());
            dbPlaceVo.setAuth(placeVo.getAuth());
            dbPlaceVo.setPlugCount(placeVo.getPlugCount());
            dbPlaceVo.setMemberCount(placeVo.getMemberCount());

            theDao.insertOrReplace(dbPlaceVo);

            mDBHelper.closeSession();
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean removeDbPlace(PlaceVo placeVo){
        try{
            String placeId = placeVo.getPlaceId();

            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlaceVoDao theDao = daoSession.getDbPlaceVoDao();
            DbPlaceVo dbPlaceVo = new DbPlaceVo();
            dbPlaceVo.setPlaceId(placeVo.getPlaceId());
            dbPlaceVo.setPlaceName(placeVo.getPlaceName());
            dbPlaceVo.setAddress(placeVo.getAddress());
            dbPlaceVo.setCoordinate(placeVo.getAddress());
            dbPlaceVo.setPlaceImg(placeVo.getPlaceImg());
            dbPlaceVo.setAuth(placeVo.getAuth());
            theDao.delete(dbPlaceVo);

            DbUserVoDao theUserDao = daoSession.getDbUserVoDao();
            List<DbUserVo> dbUserVoList = theUserDao.queryBuilder().where(DbUserVoDao.Properties.PlaceId.eq(placeId)).list();
            theUserDao.deleteInTx(dbUserVoList);

            DbPlugVoDao thePlugDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> dbPlugVoList = thePlugDao.queryBuilder().where(DbPlugVoDao.Properties.PlaceId.eq(placeId)).list();
            thePlugDao.deleteInTx(dbPlugVoList);

            mDBHelper.closeSession();
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    /////////////////

    /*
     * Shared Memory Method
     */
    public static void saveLastPlace(Context context, PlaceVo placeVo){
        UserVo loginVo = LoginService.loadLastLoginUser(context);
        if( loginVo.getUserId().equalsIgnoreCase("")){
            return;
        }
        SharedPreferences preference = context.getSharedPreferences(ARG_PLACE_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();
        if( placeVo == null){
            edit.putString(loginVo.getUserId() + "." + ARG_PLACE_LAST_NAME, null);
        } else {
            edit.putString(loginVo.getUserId() + "." + ARG_PLACE_LAST_NAME, new Gson().toJson(placeVo));
        }
        edit.commit();
    }

    public static PlaceVo loadLastPlace(Context context){
        UserVo loginVo = LoginService.loadLastLoginUser(context);
        if( loginVo.getUserId().equalsIgnoreCase("")){
            return null;
        }
        SharedPreferences preference = context.getSharedPreferences(ARG_PLACE_NAME, 0);
        PlaceVo vo = new Gson().fromJson(preference.getString(loginVo.getUserId() + "." + ARG_PLACE_LAST_NAME, ""), PlaceVo.class);

        return vo;
    }

    public static void removeLastPlace(Context context){
        UserVo loginVo = LoginService.loadLastLoginUser(context);
        if( loginVo.getUserId().equalsIgnoreCase("")){
            return;
        }
        SharedPreferences preference = context.getSharedPreferences(ARG_PLACE_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();
        edit.remove(loginVo.getUserId() + "." + ARG_PLACE_LAST_NAME);
        edit.commit();
    }

    public void saveLastCoordinate(Double latitude, Double longitude){
        String lati = String.valueOf(latitude);
        String longi = String.valueOf(longitude);
        SharedPreferences preference = mContext.getSharedPreferences(ARG_PLACE_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();
        edit.putString(ARG_PLACE_LAST_COORDINATE_LATITUDE_NAME, lati);
        edit.putString(ARG_PLACE_LAST_COORDINATE_LONGITUDE_NAME, longi);
        edit.commit();
    }

    public List<Double> loadLastCoordinate(){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_PLACE_NAME, 0);
        String latitude = preference.getString(ARG_PLACE_LAST_COORDINATE_LATITUDE_NAME, "");
        String longitude = preference.getString(ARG_PLACE_LAST_COORDINATE_LONGITUDE_NAME, "");
        List<Double> coordinate = new ArrayList<>();
        if( latitude.equalsIgnoreCase("") || longitude.equalsIgnoreCase("")){
            return null;
        }
        coordinate.add(Double.parseDouble(latitude));
        coordinate.add(Double.parseDouble(longitude));
        return coordinate;
    }
}
